package com.sap.cloud.sdk.demo.in260.remote;

import cds.gen.goal.Goal101;
import cds.gen.goal.Goal101_;
import com.sap.cds.ql.CQL;
import com.sap.cds.ql.Predicate;
import com.sap.cds.ql.cqn.CqnPredicate;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.ql.cqn.Modifier;
import com.sap.cds.services.cds.CdsReadEventContext;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.ServiceName;
import org.springframework.stereotype.Component;

import static com.sap.cloud.sdk.demo.in260.utility.Helper.DEMO_ID;

@Component
@ServiceName(cds.gen.goal.Goal_.CDS_NAME)
public class GoalServiceFilter implements EventHandler
{
    @Before( entity = Goal101_.CDS_NAME)
    public void beforeRemoteGoal(CdsReadEventContext ctx) {
        CqnSelect cqn = ctx.getCqn();
        CqnSelect enhancedSelect = CQL.copy(cqn, new Modifier() {
            @Override
            public CqnPredicate where(Predicate where) {
                return where.and(CQL.get(Goal101.NAME).startsWith(DEMO_ID));
            }
        });

        ctx.setCqn(enhancedSelect);
    }
}
