# User Service

Spring Boot 3.1 microservice for Canopy. It is running on Java 17.

User Service handles user-related activity. This includes:
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

There are a few environment variables that need to be set in AWS Secrets Manager:
* dbuser
    * database username
* password
    * database password for dbuser
* host
    * hostname of database
* port
    * database port
* dbname
    * database name
* supportEmail
    * email address used to send support-related emails to users and support staff
* EmailQueue
    * SQS queue where email requests are being sent
* redirecturl
  * post-auth URL to redirect users back to the application after Keycloak login

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
