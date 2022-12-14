#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

if ! command -v jq &> /dev/null
then
    echo "This script requires the 'jq' utility to be installed. Exiting..."
    exit 1
fi

ENVIRONMENT_NAME="governing-streams"
CLUSTER_NAME="default"

# Login
confluent login --save

# Create the active environment
confluent environment create $ENVIRONMENT_NAME
ENVIRONMENT_ID=$(confluent environment list | grep $ENVIRONMENT_NAME | sed s/\*// | xargs | cut -d' ' -f 1)
echo "SETTING ACTIVE ENVIRONMENT TO $ENVIRONMENT_NAME $ENVIRONMENT_ID"
confluent env use $ENVIRONMENT_ID

# Create and use the Kafka cluster
confluent kafka cluster create $CLUSTER_NAME --cloud gcp --region us-central1 --type basic
CLUSTER_ID=$(confluent kafka cluster list | grep $CLUSTER_NAME | sed s/\*// | xargs | cut -d' ' -f 1)
echo "SETTING ACTIVE CLUSTER TO $CLUSTER_NAME $CLUSTER_ID"
confluent kafka cluster use $CLUSTER_ID

# Create a folder for API Keys
mkdir -p creds

# Create an API Key for the Kafka Cluster
echo "CREATING API KEY FOR $CLUSTER_NAME $CLUSTER_ID"
confluent api-key create --resource $CLUSTER_ID --description $CLUSTER_NAME-key -o json > creds/$CLUSTER_NAME-key.json

# Enable the schema registry
confluent schema-registry cluster enable --cloud gcp --geo us

# Create an API Key for the Schema Registry
SR_ID=$(confluent sr cluster describe | grep "Cluster ID" | tr -s ' ' | cut -d'|' -f 3 | xargs)
echo "CREATING API KEY FOR SCHEMA REGISTRY $SR_ID"
confluent api-key create --resource $SR_ID --description $CLUSTER_NAME-sr-key -o json > creds/$CLUSTER_NAME-sr-key.json

echo "Your cluster is ready. Your credentials are stored in the ./creds directory."