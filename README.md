# User Service

Spring Boot 3.1 microservice for Data Hub 3.0. It is running on Java 17.

User Service (as the name implies) handles user related activity. This includes: 
* RAS
  * login and logout
  * passport retrieval and validation
* Support requests
  * user submission
  * support team ticket assignment and updates
* User info
  * information retrieval
  * updating user info via user registration or user dashboard

# Install and Run

## Maven

### Cloud

If running a cloud configuration locally, AWS CLI needs to be installed and configured.

There are a few environment variable that need to be set in AWS Secrets Manager:
* dbuser
    * Open Search hostname / url
* password
    * database password for dbuser
* host
    * hostname of database
* port
    * database port
* dbname
    * database name
* supportEmail
    * email address used to send support related emails to users and support staff
* EmailQueue
    * SQS queue where email requests are being sent
* rasurl
  * hostname of the RAS system
* rasclientid
  * ras provided data hub client ID
* rasclientsecret
  * ras provided data hub client secret
* rasauth
  * post ras auth url to redirect users back to our system when the ras login code is null
* redirecturl
  * post ras auth url to redirect users back to our system

In a specific instance, the only environment variable that needs to be set is:
* spring_profiles_active
    * This should be set to '{environment}'
        * The current environments are dev, test, prod

Once the environment variables are set:
```
mvn clean install 
```
Once all classes are generated, you can run the application with maven or via the application context.
```
mvn spring-boot:run
```

## Endpoint

The base endpoint for this service is:
```
{{hostname}}/api/user/v1/
```
