#!/bin/sh

mvn clean spring-boot:run -Dspring.config.location=src/main/resources/application.pgsql.properties

