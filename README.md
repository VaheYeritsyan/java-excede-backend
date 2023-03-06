
## CatStreet backend application

This project is built using Spring Boot, which makes it easy to set up and run locally. The following instructions will guide you through the setup process.

### Prerequisites

Before you start, you need to have the following software installed on your machine:

-   Java 11
-   Maven 3.4.0 or higher

### Installation

1.  Build the project using Maven:

Copy code

`mvn clean install`

This will download all the necessary dependencies and build the project.

### Running the Application

1.  After the project is built, start the application by running the following command:

Copy code

`mvn spring-boot:run`

This will start the application on port 8081.

2.  Open your web browser and navigate to `http://localhost:8081` to see the application running.

3.  You can also access the Swagger UI by navigating to `http://localhost:8081/swagger-ui.html`. This is a helpful tool for exploring the API and its endpoints.


### Testing the Application

To run the unit tests, use the following command:

bashCopy code

`mvn test`

This will execute all the unit tests in the project.

### Continuous Integration/Continuous Deployment (CI/CD) for Development Environment

This project is set up for CI/CD using GitHub CI/CD. Whenever a push is made to the `master` branch, the CI/CD pipeline will be triggered, and the latest version of the code will be deployed to a server hosted at `http://catst-api-dev.excede.com.au:8081`.

