
Bulk Processing Transaction Service


A demo Bulk Transaction Processing Service

Bulk Transaction Processing Service using Spring Boot
The Bulk Transaction Processing Service comprehensive web application developed using Spring Boot, aimed at handling a batch of transactions from clients and processes
them by calling an existing downstream Transaction-Service.
The Transaction-Service is already available and exposes endpoints for individual
debit/credit transactions. Bulk Transaction Processing service  orchestrate bulk requests, handle failures
gracefully, and provide secure access to clients.
This README provides an overview of the system, installation instructions and other details about the project.


Table of Contents
Features
Technologies Used
Getting Started
Prerequisites Requirement
Installation
API Endpoints
Documentation


Features
The Bulk Transaction Processing Service is equipped with robust reporting features that can provide valuable insights into submission of bulk transactions , view status of bulk transactions and other relevant metrics.

Bulk Transaction Management: Store and manage bulk transactions, including their transaction identity number, fromAccount, toAccount, TranasactionAmount and other relevant details.
Client Access and Security: Control client access levels and permissions to ensure data security and confidentiality.


Technologies Used
The Bulk Transaction Processing Service leverages modern technologies to deliver a robust and efficient experience:

Java: The core programming language for developing the application logic.
Spring Boot: A powerful framework for building robust and scalable applications.
Spring Data R2DBC: Enables reactive, non-blocking access to relational databases — similar to JPA, but built on R2DBC instead of JDBC.
Spring Webflux: Facilitates the creation of web APIs and interfaces.
Spring Security: Ensures secure authentication and authorization within the application, guarding against unauthorized access and protecting sensitive data.
JSON Web Token (JWT): Enables stateless authentication by securely transmitting user information as JSON objects, facilitating secure communication between the client and server.
PostgreSQL: A popular relational database management system used for storing and retrieving structured data efficiently.
Maven: Manages project dependencies and provides a structured build process.
Circuit Breaker: a resilience pattern used in distributed systems and microservices to prevent cascading failures
Git: Version control for collaborative development.


Getting Started
Prerequisites Requirement
Before getting started, ensure you have the following components installed:

This project was built using JDK 17, you would need JDK 17 installed on you local machine.
Java Development Kit (JDK 17)
Maven


Build and Run the Application:
Execute the following command to build and run the application:

mvn clean package install
mvn spring-boot:run
Installation
Clone the Repository:

git clone https://github.com/Greatnex1/paystream
cd Bulk Processing Transaction Service
Configure the Database:

Modify the src/main/resources/application.properties file to include your database connection details:


spring.r2dbc.username=your_database_username
spring.r2dbc.password=your_database_password
spring.r2dbc.url=your_database_r2dbc_url
spring.flyway.user=your_database_username
spring.flyway.password=your_database_password
spring.flyway.enabled=false
spring.sql.init.mode=always
spring.sql.init.platform=postgres
spring.sql.init.schema-locations=sql-location-url


Token Requirements:

Modify the src/main/resources/application.properties file to include your JWT token:

transaction.processing.app.SecretKey=your_JWT_token
API Endpoints
The Bulk Transaction Processing Service offers the following API endpoints:
Client login  :POST /api/v1/authenticate
Bulk Transaction Request Submission: POST /api/v1/submit/bulk-transactions
View Bulk Transaction Status: GET /api/v1/bulk-transactions/status

