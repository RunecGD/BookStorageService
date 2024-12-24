book-storage-service
A project for managing a book storage service using Spring Boot, Kafka, and MySQL.
Prerequisites
Before you can build and run this project, ensure you have the following software installed on your machine:

Java Development Kit (JDK)

Version: 21 or higher
Apache Maven

Version: 3.8.5 or higher
Apache Kafka

Version: 3.4.0 or higher
MySQL Server

Version: 8.0 or higher
Docker (optional)

For running Kafka and MySQL via Docker Compose
Configuration
MySQL Database:

Create a database named book_storage_db.
Set the following credentials:
Username: root
Password: password
Update the application.properties file with your MySQL configuration:

spring.datasource.url=jdbc:mysql://localhost:3306/book_storage_db
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.generate-ddl=true
server.port=8081
Kafka:

Start Kafka and Zookeeper using Docker Compose or manually.
Default Kafka broker URL: localhost:9092
Update the application.properties file with your Kafka configuration:

spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
jwt.secretKey=+K2d33f/4VafYLVyb4UfmK1n36ObMX8pL4JxNaAZKOc=
Build and Run
1. Building the Project
To build the project, run the following command in the root directory:

mvn clean install
This will compile the source code, run tests, and package the application into a JAR file located in the target directory.

2. Start Zookeeper and Kafka
Run the application using the following command: In one terminal bin/zookeeper-server-start.sh config/zookeeper.properties In another terminal bin/kafka-server-start.sh config/server.properties

3. Running the Project
Run the application using the following command:

java -jar target/project-name-0.0.1-SNAPSHOT.jar 
Alternatively, you can run the application directly using Maven:

mvn spring-boot:run
Troubleshooting
Database Connection Issues:

Ensure MySQL is running and accessible on the configured port.
Verify credentials in the application.properties file.
Kafka Connection Issues:

Check that Kafka and Zookeeper are running.
Verify the Kafka broker URL.
Build Failures:

Ensure you have the correct JDK and Maven versions.
