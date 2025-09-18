# Demo Setup and Runbook

This document provides the consolidated steps to set up and run the full monolith-to-microservice demo environment for the `CardDemo` application.

The setup involves two major parts:
1.  **The Mainframe Monolith**: The original `CardDemo` COBOL application running on CICS with its databases (VSAM, DB2, IMS) and messaging (MQ).
2.  **The New Microservices**: The new Java-based services (`transaction-type-service` and `authorization-service`) that will incrementally replace the monolith's functionality.

---

## Part 1: Setting up the Mainframe Monolith (CardDemo)

The core of the demo is the existing mainframe application. The main `README.md` provides a detailed guide for this. Here is a high-level summary of the steps involved:

1.  **Prerequisites**: Ensure you have a mainframe environment with CICS, VSAM, JCL, and file transfer capabilities. For the full demo scenario, you will also need **DB2, IMS DB, and MQ** installed and configured, as these are used by the functions we will be extracting.

2.  **Prepare Datasets and Upload Artifacts**:
    *   Follow the instructions in the `README.md` under **"Create Mainframe Datasets"** to create the necessary PDS and PS files for source code, JCL, and data.
    *   Upload the source code (COBOL, BMS, JCL, etc.) and the sample EBCDIC data from the repository to the corresponding datasets on the mainframe.

3.  **Initialize the Environment**: This is a critical step where you run a sequence of JCL jobs to set up the databases and files. The `README.md` file's **"Initialize the Environment"** section lists the jobs to run in order. This includes jobs like:
    *   `ACCTFILE`, `CARDFILE`, `CUSTFILE`: To load the core VSAM files.
    *   `CREADB21`: To create the DB2 database and tables for the "Transaction Type Management" feature.
    *   `TRANEXTR`: To extract data from DB2 for use by other parts of the system.
    *   You will also need to set up the IMS database as described in `app/app-authorization-ims-db2-mq/README.md`.

4.  **Compile Programs**: Compile all the COBOL and Assembler programs using your site's standard procedures. The various `app/.../README.md` files note any special compilation requirements (e.g., using DB2 or IMS precompilers).

5.  **Configure CICS**: Define all the necessary CICS resources (programs, transactions, mapsets, files, DB2 connections, etc.) as specified in the **"Configure CICS Resources"** section of the main `README.md` and the extension READMEs.

6.  **Access the Application**: Once setup is complete, you can log in to the CICS application using transaction `CC00` to verify that the monolith is running correctly.

> **For detailed, step-by-step instructions on setting up the monolith and its optional modules, please refer to these files:**
> *   `README.md`
> *   `app/app-transaction-type-db2/README.md`
> *   `app/app-authorization-ims-db2-mq/README.md`

---

## Part 2: Setting up the Java Microservices

The next step is to run the new Java services that will interact with the mainframe environment.

### A. Transaction Type Service

This Spring Boot service provides a REST API for the "Transaction Type Management" function.

1.  **Prerequisites**: A Java Development Kit (JDK) and Maven must be installed on the machine where you will run the service.
2.  **Configuration**: This service needs to connect to the **same DB2 database** that the CICS application uses. You will need to configure the `application.properties` or `application.yml` file in the service's source code with your DB2 connection details. The configuration would look something like this:
    ```properties
    # DB2 Database Connection
    spring.datasource.url=jdbc:db2://<your-db2-host>:<port>/<database>
    spring.datasource.username=<your-username>
    spring.datasource.password=<your-password>
    spring.datasource.driver-class-name=com.ibm.db2.jcc.DB2Driver

    # JPA/Hibernate settings
    spring.jpa.hibernate.ddl-auto=validate
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.DB2Dialect
    ```
3.  **Run the Service**: Navigate to the root directory of the `transaction-type-service` and run it using Maven:
    ```bash
    mvn spring-boot:run
    ```

### B. Authorization Service

This service listens to an MQ queue to process credit card authorizations.

1.  **Prerequisites**: A JDK and Maven.
2.  **Configuration**: This service needs connection details for both MQ and the new "Account Data API" on the monolith. Your `application.properties` or `application.yml` file will need entries for:
    *   **MQ Server**: Host, port, queue manager, and the specific request/reply queue names.
    *   **Monolith API Endpoint**: The URL for the read-only REST API you exposed from CICS to get account data from the VSAM file.
3.  **Run the Service**: Navigate to the root directory of the `authorization-service` and run it:
    ```bash
    mvn spring-boot:run
    ```

---

## Part 3: Executing the Demo Flow

Once both the monolith and the microservices are running, you can follow the narrative in `DEMO_OUTLINE.md`.

1.  **Show the Monolith**: Use a 3270 emulator to log in to CICS (`CC00`) and navigate to the Admin Menu (`CA00`) to show the existing "Transaction Type List" screen (`CTLI`).
2.  **Use the Microservice**: Use a tool like Postman or `curl` to call the `GET /api/transaction-types` endpoint on your running `transaction-type-service`. The data returned should match what you see on the CICS screen.
3.  **Demonstrate Coexistence**:
    *   Use `POST /api/transaction-types` to add a new transaction type.
    *   Go back to the CICS screen (`CTLI`), refresh it, and show that the new type now appears. This is the key moment that proves both systems are working on the same data.
4.  **Demonstrate the Strangle**: Follow a similar pattern for the `authorization-service`, sending a message to the MQ queue and observing the logs of the Java service as it processes the request and calls back to the monolith for data.