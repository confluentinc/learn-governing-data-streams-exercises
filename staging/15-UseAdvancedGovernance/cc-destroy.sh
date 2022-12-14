#!/bin/bash
IFS=$'\n\t'

ENVIRONMENT_NAME="governing-streams"

# Login
confluent login --save

# Set the active environment
ENVIRONMENT_ID=$(confluent environment list | grep $ENVIRONMENT_NAME | sed s/\*// | xargs | cut -d' ' -f 1)
echo "SETTING ACTIVE ENVIRONMENT TO $ENVIRONMENT_NAME $ENVIRONMENT_ID"
confluent env use $ENVIRONMENT_ID

# Delete the Environment
echo "DELETING ENVIRONMENT $ENVIRONMENT"
confluent environment delete $ENVIRONMENT_ID