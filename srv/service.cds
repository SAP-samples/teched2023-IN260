using { managed } from '@sap/cds/common';
using { Goal.Goal_101, Goal.GoalTask_101 } from './external/Goal';

@path: 'SignupService'
service SignupService {
    action signUp(session: String) returns String;
}

@path: 'GoalService'
service GoalService {
    entity Goal as projection on Goal_101 {
            id,
            name as title,
            metric as description,
        }
}

extend Goal_101 with {
  weight: Double;
}

extend GoalTask_101 with {
  description: String;
  done: Double;
}
