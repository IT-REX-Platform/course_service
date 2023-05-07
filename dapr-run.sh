#!/bin/bash

# get properties from application.properties
while IFS='=' read -r key value; do
    if [ "$key" = "dapr.appId" ]; then
        app_id="${value//$'\r'}" # remove carriage return
    elif [ "$key" = "dapr.port" ]; then
        dapr_http_port="${value//$'\r'}"
    elif [ "$key" = "server.port" ]; then
        app_port="${value//$'\r'}"
    fi
done < src/main/resources/application.properties

command="./gradlew bootRun"

# if cli arguments are provided, add them to the command
args="$@"
if [ -n "$args" ]; then
    command="$command --args=\"$args\""
fi

# start dapr
dapr run --app-id "$app_id" --app-port "$app_port" --dapr-http-port "$dapr_http_port" -- $command