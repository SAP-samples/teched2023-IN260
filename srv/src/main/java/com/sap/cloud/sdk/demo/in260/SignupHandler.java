package com.sap.cloud.sdk.demo.in260;

import cds.gen.signupservice.SignUpContext;
import cds.gen.signupservice.SignupService_;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cloud.sdk.cloudplatform.resilience.ResilienceConfiguration;
import com.sap.cloud.sdk.cloudplatform.resilience.ResilienceConfiguration.CircuitBreakerConfiguration;
import com.sap.cloud.sdk.cloudplatform.resilience.ResilienceConfiguration.RateLimiterConfiguration;
import com.sap.cloud.sdk.cloudplatform.resilience.ResilienceConfiguration.RetryConfiguration;
import com.sap.cloud.sdk.cloudplatform.resilience.ResilienceConfiguration.TimeLimiterConfiguration;
import com.sap.cloud.sdk.cloudplatform.resilience.ResilienceDecorator;
import com.sap.cloud.sdk.demo.in260.remote.GoalServiceHandler;
import com.sap.cloud.sdk.demo.in260.remote.RegistrationServiceHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ServiceName(SignupService_.CDS_NAME)
public class SignupHandler implements EventHandler
{
    @Autowired
    private RegistrationServiceHandler registrationService;

    @Autowired
    private GoalServiceHandler goalService;

    @On
    public void signUp(SignUpContext context)
    {
        String session;
        if (context.getSession() != null) {
            session = context.getSession();
        } else {
            session = "Opening Keynote";
        }

        register(session);

        updateSFSF(session);

        context.setResult("Yay, we successfully signed you up for the session: " + session + ".\n"
                + "Also, we created an entry in your 'Learning and Growth' section in SAP SuccessFactors to reflect your efforts.");
    }

    private void register(String session) {
        // sign up for the event and the session
        registrationService.signUpForTechEd();

        registrationService.signUpForSession(session);
    }

    private void updateSFSF(String session) {
        // create a goal and related tasks in SFSF
        var timeout = TimeLimiterConfiguration.of(Duration.ofSeconds(2));
        var retry = RetryConfiguration.of(3, Duration.ofSeconds(1));
        var rateLimit = RateLimiterConfiguration.of(
                Duration.ofSeconds(1),
                Duration.ofSeconds(30),
                10);
        var circuitBreaker = CircuitBreakerConfiguration.of()
                .failureRateThreshold(50)
                .waitDuration(Duration.ofSeconds(10));
        var config = ResilienceConfiguration.of(SignupHandler.class)
                .timeLimiterConfiguration(timeout)
                .retryConfiguration(retry)
                .rateLimiterConfiguration(rateLimit)
                .circuitBreakerConfiguration(circuitBreaker);

        var goal = ResilienceDecorator.executeSupplier(goalService::getLearningGoal, config);

        if ( goal == null ) {
            goal = goalService.createGoal();
        }

        goalService.createTask(goal, session);
    }
}
