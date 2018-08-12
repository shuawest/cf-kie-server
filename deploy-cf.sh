#!/bin/bash

mvn clean install -DskipTests=true

cf delete cf-kie-server

cf push --no-start

# Postgres
# cf create-service elephantsql turtle cf-kie-server-db
# cf bind-service cf-kie-server cf-kie-server-db

# MySQL
cf create-service p-mysql 512mb cf-kie-server-db
cf bind-service cf-kie-server cf-kie-server-db

cf start cf-kie-server

cf ssh -N -T -L 5000:localhost:5000 cf-kie-server

cf logs cf-kie-server


