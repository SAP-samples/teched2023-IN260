package com.sap.cloud.sdk.demo.in260.remote;

import cloudsdk.gen.registrationservice.Event;
import cloudsdk.gen.registrationservice.EventRegistrationApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;

@Component
@RestController
@Slf4j
public class RegistrationServiceHandler {

    private Destination getDestination() {
        return DestinationAccessor.getDestination("Registration-Service");
    }

    public void signUpForTechEd() {
        var event = getTechEdEvent();
        var api = new EventRegistrationApi(getDestination());
        api.registerForEvent(event.getId());
    }

    public void signUpForSession(String sessionName) {
        var event = getTechEdEvent();

        var api = new EventRegistrationApi(getDestination());
        var session = api.getSessions(event.getId())
                .stream()
                .filter(s -> s.getTitle().equalsIgnoreCase(sessionName))
                .findFirst()
                .orElseThrow();

        api.registerForSession(event.getId(), session.getId());
    }

    @GetMapping( path = "/rest/v1/getTechEdEvent", produces = "application/json")
    public Event getTechEdEvent() {
        var api = new EventRegistrationApi(getDestination());

        List<Event> events = api.getEvents();

        return events
                .stream()
                .filter(e -> e.getName().equals("TechEd 2023"))
                .findFirst()
                .orElseThrow();
    }
}
