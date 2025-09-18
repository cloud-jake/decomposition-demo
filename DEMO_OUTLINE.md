# Demo: From Monolith to Microservices

## A Practical Journey with the CardDemo Application

**Objective:** To demonstrate a phased, low-risk approach to modernizing a mainframe COBOL application by incrementally decomposing it into Java microservices using the Strangler Fig pattern.

---

### Part 1: The "Before" - Understanding the COBOL Monolith (5 mins)

*   **Goal:** Establish the starting point and its challenges.
*   **Narrative:** "Let's start by looking at our application, CardDemo. It's a classic, robust mainframe credit card management system. It's served the business well for years, but making changes is becoming slow and difficult."
*   **Demo Actions:**
    1.  **Show the Application:** Briefly show the CardDemo CICS screens (Signon, Main Menu, Admin Menu). Navigate to the "Credit Card List" (`CCLI`) to show it's a real, working application.
    2.  **Explain the Architecture:**
        *   Display a simple diagram showing a single "CardDemo Monolith" box.
        *   Reference the main `README.md` to explain the stack: COBOL programs running under CICS, using VSAM files for core data, and JCL for batch processing.
        *   Mention the tight coupling: "As you can see from the `Application Inventory` in the `README.md`, we have dozens of programs, screens, and batch jobs. They are all tightly interwoven. A change to one part, like `COCRDLIC` for listing cards, requires a full regression test of the entire system."
    3.  **Highlight the Challenges:**
        *   **Slow to Change:** "Adding a new feature requires navigating complex COBOL code and mainframe build processes." (Briefly show the `samples/proc/BLDCIDB2.prc` file as an example of a complex build procedure).
        *   **Technology Silos:** "It's difficult to integrate with modern cloud services or expose functionality via APIs."
        *   **Skills Gap:** "Finding developers with the skills to maintain and enhance this COBOL/CICS stack is becoming harder."

---

### Part 2: The Strategy - Decomposing by Business Capability (5 mins)

*   **Goal:** Introduce the "Strangler Fig" pattern and identify service boundaries within CardDemo.
*   **Narrative:** "A 'big bang' rewrite is too risky. Instead, we'll use the Strangler Fig pattern to incrementally carve off pieces of the monolith and replace them with new microservices, slowly strangling the old application until it's gone."
*   **Demo Actions:**
    1.  **Introduce the Strangler Fig Pattern:** Show a diagram illustrating how a proxy/facade redirects calls one-by-one from an old monolith to new microservices.
    2.  **Identify Service Boundaries in CardDemo:**
        *   Go back to the `README.md` file's `User Functions` and `Admin Functions` sections.
        *   **Narrative:** "The key is to decompose by business capability, not technical layers. Looking at CardDemo, we can easily identify logical domains that can become our future microservices."
        *   List them out:
            *   Customer Management
            *   Account Management
            *   Card Management
            *   **Transaction Type Management (Admin)**
            *   **Credit Card Authorization**
            *   Reporting & Statements

---

### Part 3: First Step - Extracting an "Admin" Microservice (10 mins)

*   **Goal:** Demonstrate the first, low-risk extraction of a self-contained function.
*   **Narrative:** "Let's start with something simple and low-risk: **Transaction Type Management**. It's an administrative function, already uses a relational database (DB2), and has a clear boundary."
*   **Demo Actions:**
    1.  **Show the "As-Is":**
        *   In the CardDemo Admin Menu, go to options 5 (`CTLI`) and 6 (`CTTU`). Show how an admin can list, add, or update transaction types.
        *   Briefly show the `app/app-transaction-type-db2/README.md` to explain that this function uses COBOL programs (`COTRTLIC`, `COTRTUPC`) with embedded SQL to talk to a DB2 table.
    2.  **Introduce the "To-Be" Java Microservice:**
        *   **Narrative:** "We've created a new Java Spring Boot microservice called `transaction-type-service`."
        *   Show the Java code (keep it high-level):
            *   A `TransactionTypeController.java` with REST endpoints (`@GetMapping`, `@PostMapping`).
            *   A `TransactionTypeRepository.java` (JPA) that connects to the **exact same DB2 table**. This is a key intermediate step—we change the application logic first and deal with the data later.
    3.  **Show the New Service in Action:**
        *   Use a tool like Postman or `curl` to hit the new service's API (`GET /api/transaction-types`). Show the data returned is the same as what's in the CICS screen.
        *   Add a new transaction type via the API (`POST /api/transaction-types`).
        *   Go back to the CICS screen (`CTLI`), refresh it, and show the new type appearing. **This is the "Aha!" moment**, proving both systems are working on the same data.
    4.  **The "Strangle":**
        *   **Narrative:** "The final step is to redirect users. We can now build a modern web-based admin portal that uses this new microservice. Once that's done, we can disable options 5 and 6 in the CICS Admin Menu. We have successfully carved off our first piece of the monolith."

---

### Part 4: Next Step - Extracting a Core "Business" Microservice (10 mins)

*   **Goal:** Tackle a more complex, core business function that interacts with other parts of the monolith.
*   **Narrative:** "Now for a bigger challenge: **Credit Card Authorization**. This is a core business process that is already triggered by an external system via MQ, making it a perfect candidate for extraction."
*   **Demo Actions:**
    1.  **Show the "As-Is":**
        *   Use the architecture diagram from `app/app-authorization-ims-db2-mq/README.md`.
        *   **Narrative:** "Currently, an authorization request comes in on an MQ queue, which triggers a CICS COBOL program (`COPAUA0C`). This program reads VSAM files for account data, applies business rules, and writes to IMS and DB2."
    2.  **Introduce the "To-Be" Java Microservice:**
        *   **Narrative:** "We've built the `authorization-service`. It will take over the responsibility from the CICS program."
        *   Show the Java code:
            *   A JMS Listener (`@JmsListener` in Spring) that listens to the **same MQ request queue**.
            *   **The Critical Pattern - The Anti-Corruption Layer:** "But how does our new Java service get the account data from the VSAM file on the mainframe? We don't want to replicate the data yet. Instead, we expose a simple, read-only REST API *from the monolith*."
            *   Explain that a small CICS COBOL program can be written to read a VSAM record and return it as JSON. This acts as a clean API facade or "Anti-Corruption Layer", preventing the new service from needing to know about the complexities of VSAM.
            *   Show the Java service code calling this new "Account Data API" on the mainframe.
    3.  **Show the New Flow in Action:**
        *   Simulate sending an authorization request to the MQ queue.
        *   Show logs from the new Java `authorization-service` as it picks up the message.
        *   Show a log entry where it calls the "Account Data API" on the mainframe.
        *   Show the service sending a response back to the reply queue.
    4.  **The "Strangle":**
        *   **Narrative:** "To complete the strangulation, we simply disable the CICS trigger that starts the old `COPAUA0C` program. The new Java service is now live and handling all authorizations. We've reduced the monolith's responsibility without a risky 'big bang' deployment."

---

### Part 5: The Path Forward & Conclusion (5 mins)

*   **Goal:** Summarize the benefits and show the future roadmap.
*   **Narrative:** "We've successfully extracted two services. We can now continue this pattern for the remaining capabilities."
*   **Demo Actions:**
    1.  **Show a "Future Architecture" Diagram:** Display a diagram with the shrunken `CardDemo Monolith` surrounded by the new `Transaction Type Service`, `Authorization Service`, and future services like `Account Service`, `Card Service`, etc.
    2.  **Summarize the Benefits:**
        *   **Reduced Risk:** We made changes incrementally without taking the core system offline.
        *   **Increased Agility:** The new Java services can be developed, tested, and deployed independently and much faster than the monolith.
        *   **Modern Technology:** We can now leverage the entire cloud-native ecosystem (containers, serverless, modern databases, CI/CD pipelines) for these new services.
        *   **Unlocking Data:** By creating APIs, we've made valuable business data accessible to other parts of the organization.
    3.  **Call to Action:** "This is a proven, pragmatic path to mainframe modernization. By starting small and delivering value incrementally, you can transform your legacy systems while minimizing business disruption."

---