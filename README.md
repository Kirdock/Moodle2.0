# Moodle2.0
[![Build Status](https://travis-ci.com/Kirdock/Moodle2.0.svg?branch=master)](https://travis-ci.com/github/Kirdock/Moodle2.0)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Kirdock_Moodle2.0&metric=bugs)](https://sonarcloud.io/dashboard?id=Kirdock_Moodle2.0)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=Kirdock_Moodle2.0&metric=code_smells)](https://sonarcloud.io/dashboard?id=Kirdock_Moodle2.0)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Kirdock_Moodle2.0&metric=alert_status)](https://sonarcloud.io/dashboard?id=Kirdock_Moodle2.0)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=Kirdock_Moodle2.0&metric=sqale_index)](https://sonarcloud.io/dashboard?id=Kirdock_Moodle2.0)
__Install:__
- make sure the following is installed and in system variable PATH: maven, java (jdk 1.8), node
- if you run Maven from console, add the "PathToJavaJDK" to system variable "JAVA_HOME"
- run "mvn clean install"

__How to run:__
- run "mvn --projects backend spring-boot:run" in root directory
- Access http://localhost:8098

__Local Database Configuration__

For local development configure the following environment variables
in your IDE 

- SPRING_DATASOURCE_URL = jdbc:h2:file:./data/demo
- SPRING_DATASOURCE_DRIVER-CLASS-NAME = org.h2.Driver
- SPRING_DATASOURCE_PASSWORD = sa 
- SPRING_DATASOURCE_USERNAME = password
- SPRING_JPA_DATABASE-PLATFORM = org.hibernate.dialect.H2Dialect
- SPRING_H2_CONSOLE_ENABLED = true

Access H2 Console 
- http://localhost:8098/h2-console
# Heroku
https://moodlev2.herokuapp.com/

