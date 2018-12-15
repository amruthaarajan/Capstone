# Capstone project for ML using SOA

## Pre requisites
* JDK 8
* Spring Boot
* Spring Initializr

## Getting Started
1. Go to src/main/resources/application.properties and update the email id and password. This is required to send a email. This is the sender email and password
* spring.mail.username=
* spring.mail.password=
2. Open terminal and run the project using command ./gradlew bootRun
3. Dont close the terminal or powershell window
4. Now Go to  http://localhost:8080/

## Troubleshooting
In some versions of gradle while you run ./gradlew bootRun, it gets stuck at Executing xx%. The application is still started successfully. So you can ignore the message
