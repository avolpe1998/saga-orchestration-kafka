# SAGA Orchestration with Kafka

## 📖 Overview
This monorepo demonstrates the implementation of the **SAGA Pattern** using an **Orchestrator** approach to manage distributed transactions. 

The system consists of three primary microservices:
- **Orders Service** (`order-service` running on port `8081`)
- **Inventory Service** (`inventory-service` running on port `8082`)
- **Payment Service** (`payment-service` running on port `8083`)

### Key Support Components
- **Apache Kafka**: Used as the message broker for event-driven asynchronous communication and coordinating the saga workflow between microservices.
- **Wiremock**: Mocks the external banking API requests. It is configured to simulate realistic scenarios, such as **refusing payment requests if the amount exceeds 100 dollars**.
- **PostgreSQL**: The central database server, automatically initialized with three isolated databases (`orders_db`, `inventory_db`, `payment_db`)—one for each microservice.

---

## 🚀 Getting Started

### Prerequisites
- **Java 17+** (or compatible JDK)
- **Docker** and **Docker Compose**
- Database Tool (e.g., DBeaver) or CLI for database inspection

### 1. Start Infrastructure (Docker)
The `docker/` folder contains the configurations needed to spin up the supporting architecture. It includes `docker-compose.yml` to run Postgres, Kafka, and Wiremock, as well as an `init-dbs.sql` script to set up the separate databases.

To start the infrastructure, run:
```bash
cd docker
docker-compose up -d
```

(Note: If you previously ran the Choreography version of this project, you may need to run docker-compose down -v first to clear Kafka's memory of old message types).

### 2. Run the Microservices
You can run the microservices directly from your preferred IDE, or use the provided Maven wrappers. Open three separate terminal windows and run:

**Order Service:**
```bash
cd order-service
./mvnw spring-boot:run
```

**Inventory Service:**
```bash
cd inventory-service
./mvnw spring-boot:run
```

**Payment Service:**
```bash
cd payment-service
./mvnw spring-boot:run
```

---

## 📂 Project Structure & Useful Scripts

The project includes helpful scripts under the `order-service/scripts/` folder for interacting with the application.

### `order-service/scripts/curl/`
This folder contains executable terminal commands to easily trigger tests and verify the state of your application:
- **`place_order`**: Executes an HTTP POST request to the API to initialize a new saga flow by placing an order.
- **`check_orders`**: Runs a direct query inside the Postgres Docker container to print all current orders.
- **`check_products`**: Runs a direct query inside the Postgres Docker container to check the current inventory availability.
- **`check_payments`**: Runs a direct query inside the Postgres Docker container to print all current payments.

### `order-service/scripts/sql/`
Provides useful data seeding scripts:
- **`add_product.sql`**: A raw SQL script to insert initial products (e.g., adding stock for a "MacBook Pro") directly into the inventory database. You can execute this queries using any visual Database Tool (like DBeaver or DataGrip).
- **`docker-command`**: Provides the direct terminal equivalent to run the SQL insert query right into the Docker database container without needing an external GUI client.
