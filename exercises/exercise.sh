#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

EXERCISE_DIR=./
SOLUTIONS_DIR=../solutions
STAGING_DIR=../staging

function help() {
    echo "Usage:"
    echo "  exercises.sh <Command> <Exercise Filter>"
    echo "  Commands:"
    echo "    stage - Setup the exercise."
    echo "    solve - Solve the exercise."
    echo "  Exercise Filter: A portion of the name of the exercise. Eg. The Exercise Number. If multiple matches are found, the first one will be chosen."
}

function stage() {
    EXERCISE_FILTER=$1
    MATCHED_EXERCISES=($(ls $STAGING_DIR | grep ".*$EXERCISE_FILTER.*"))
    EXERCISE=${MATCHED_EXERCISES[0]}

    echo "STAGING $EXERCISE"

    cp -r $STAGING_DIR/$EXERCISE/ $EXERCISE_DIR
}

function solve() {
    EXERCISE_FILTER=$1
    FILE_FILTER=${2:-""}
    MATCHED_EXERCISES=($(ls $SOLUTIONS_DIR | grep ".*$EXERCISE_FILTER.*"))
    EXERCISE=${MATCHED_EXERCISES[0]}
    SOLUTION=$SOLUTIONS_DIR/$EXERCISE

    if [ -z $FILE_FILTER ]; then
        echo "SOLVING $EXERCISE"
    
        cp -r $SOLUTION/ $EXERCISE_DIR
    else
        WORKING_DIR=$(pwd)
        cd $SOLUTION
        MATCHED_FILES=($(find . -iname "*$FILE_FILTER*"))
        cd $WORKING_DIR

        if [ -z ${MATCHED_FILES:-""} ]; then
            echo "FILE NOT FOUND: $FILE_FILTER"
            exit 1
        fi

        FILE_PATH=${MATCHED_FILES[0]}

        echo "COPYING $FILE_PATH FROM $EXERCISE"

        cp $SOLUTION/$FILE_PATH $EXERCISE_DIR/$FILE_PATH
    fi

}

COMMAND=${1:-"help"}
EXERCISE_FILTER=${2:-""}
FILE_FILTER=${3:-""}

if [ -z $EXERCISE_FILTER ]; then
    echo "MISSING EXERCISE ID"
    help
    exit 1
fi


## Determine which command is being requested, and execute it.
if [ "$COMMAND" = "stage" ]; then
    stage $EXERCISE_FILTER
elif [ "$COMMAND" = "solve" ]; then
    solve $EXERCISE_FILTER $FILE_FILTER
elif [ "$COMMAND" = "help" ]; then
    help
else
    echo "INVALID COMMAND: $COMMAND"
    help
    exit 1
fi