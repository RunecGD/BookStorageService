# book-storage-service

A brief description of your project, e.g., "A Spring Boot application for managing events with Kafka and MySQL integration."

---

## Prerequisites

Before you can build and run this project, ensure you have the following software installed on your machine:

1. **Java Development Kit (JDK)**
   - Version: 21 or higher
   - [Download JDK](https://adoptopenjdk.net/)

2. **Apache Maven**
   - Version: 3.8.5 or higher
   - [Download Maven](https://maven.apache.org/download.cgi)

3. **Apache Kafka**
   - Version: 3.4.0 or higher
   - [Download Kafka](https://kafka.apache.org/downloads)

4. **MySQL Server**
   - Version: 8.0 or higher
   - [Download MySQL](https://dev.mysql.com/downloads/mysql/)

---

## Configuration

1. **MySQL Database**:
   - Create a database named `book_storage_db`.
   - Set the following credentials:
     - **Username:** `root`
     - **Password:** `password`
   - Update the `application.properties` file with your MySQL configuration:

     ```properties
spring.datasource.url=jdbc:mysql://localhost:3306/book_storage_db
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.generate-ddl=true
server.port=8081
     ```

2. **Kafka**:
   - Start Kafka and Zookeeper using Docker Compose or manually.
   - Default Kafka broker URL: `localhost:9092`

   Update the `application.properties` file with your Kafka configuration:

   ```spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
jwt.secretKey=+K2d33f/4VafYLVyb4UfmK1n36ObMX8pL4JxNaAZKOc=
   ```

---

## Build and Run

### 1. Building the Project

To build the project, run the following command in the root directory:

```bash
mvn clean install
```

This will compile the source code, run tests, and package the application into a JAR file located in the `target` directory.

### 2. Running the Project

Run the application using the following command:

```bash
java -jar target/project-name-0.0.1-SNAPSHOT.jar
```

Alternatively, you can run the application directly using Maven:

```bash
mvn spring-boot:run
```

### 3.  Start Zookeeper and Kafka
Run the application using the following command:

``` In one terminal
bin/zookeeper-server-start.sh config/zookeeper.properties
In another terminal 
bin/kafka-server-start.sh config/server.properties

```


---


## Troubleshooting

1. **Database Connection Issues**:
   - Ensure MySQL is running and accessible on the configured port.
   - Verify credentials in the `application.properties` file.

2. **Kafka Connection Issues**:
   - Check that Kafka and Zookeeper are running.
   - Verify the Kafka broker URL.

3. **Build Failures**:
   - Ensure you have the correct JDK and Maven versions.

---
