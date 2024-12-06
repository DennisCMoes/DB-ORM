# DB-ORM projectDB-ORM Project

## Overview

This project is a Java-based Object-Relational Mapping (ORM) framework designed to simplify database interactions. It includes a JavaFX application for managing tasks and a library for ORM functionalities.

## Project Structure

### Prerequisites

- Java 21
- Maven 3.8.5
- PostgreSQL 16.2

## Setup

1. Clone the repository
2. Configure the database: Ensure PostgreSQL is running and accessible. Update the database connection details in the configuration files if necessary.
3. Build the project:

```bash
./mvnw clean install
```

4. Run the JavaFX application:

```bash
./mvnw -pl app javafx:run
```

## Project Modules

### app

This module contains the JavaFX application for managing tasks.

- Main.java: Entry point for the JavaFX application.
- controllers: Contains controllers for handling UI interactions.
- resources: Contains FXML files for defining the UI layout.

### lib

This module contains the core ORM functionalities.

- EntityManager.java: Manages entity lifecycle and database operations.
- OrmManager.java: Initializes and configures the ORM framework.
- EntityMapper.java: Maps database results to entity objects.
- SQLGenerator.java: Generates SQL queries for CRUD operations.
- DatabaseUtil.java: Utility class for database connections and queries.
- ReflectionUtil.java: Utility class for reflection-based operations.
- annotations: Contains custom annotations for ORM mappings.

## Running Tests

To run the tests, use the following command:

```bash
./mvnw test
```
