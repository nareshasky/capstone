📦 E-Commerce Order Management System – Microservices Architecture

====================================================================


A Spring Boot–based E-Commerce Order Management System built using microservices architecture, featuring centralized configuration, service discovery, API Gateway routing, and JWT-based authentication.

🧩 Components
Service	Description
API Gateway	Entry point for all client requests
Config Server	Centralized external configuration
Eureka Server	Service discovery and registration
Auth Service	Authentication & JWT token generation
Order Service	Order creation and management
Product Service	Product catalog management


🧰 Tech Stack

Java 21
Spring Boot 3.2.x
Spring Cloud 2023.x
Spring Cloud Gateway
Spring Cloud Config
Eureka Server
Spring Security + JWT
Spring Data JPA
MySQL
OpenFeign
Resilience4j
Swagger / OpenAPI

📂 Project Structure
ecommerce/
│
├── api-gateway/
├── config-server/
├── eureka-server/
├── auth-service/
├── order-service/
├── product-service/
└── README.md

🔄 Request Flow

Client sends request → API Gateway
Gateway validates JWT token
Gateway routes request to respective microservice
Services discover each other via Eureka
Configurations loaded dynamically from Config Server

🔐 Security Model

JWT-based authentication
Token issued by Auth Service
Token validated at API Gateway
User context passed via headers to downstream services

⚙️ Configuration Management

All services fetch configuration from Config Server, backed by a Git repository.

Example Configuration
spring:
    datasource:
        url: jdbc:mysql://localhost:3306/orderdb
        username: root
        password: root

🚀 How to Run the Application
1️⃣ Start Infrastructure Services
# Config Server
cd config-server
mvn spring-boot:run

# Eureka Server
cd eureka-server
mvn spring-boot:run

2️⃣ Start Core Services
# Auth Service
cd auth-service
mvn spring-boot:run

# Product Service
cd product-service
mvn spring-boot:run

# Order Service
cd order-service
mvn spring-boot:run

3️⃣ Start API Gateway
cd api-gateway
mvn spring-boot:run

🌐 Service Ports (Default)
Service	Port
Config Server	8083
Eureka Server	8761
API Gateway	8084
Auth Service	8085
Order Service	8082
Product Service	8081

🧪 API Documentation (Swagger)

Each service exposes Swagger UI via API Gateway:

http://localhost:8084/webjars/swagger-ui/index.html


🛠️ Resilience & Observability

Resilience4j – Circuit breaker, retry
Spring Boot Actuator – Health checks & metrics
Zipkin – Distributed tracing


ELK Stack – Centralized logging

🧩 Future Enhancements
🔐 HashiCorp Vault for secrets
📊 Prometheus + Grafana monitoring
🔁 Kafka / RabbitMQ for async messaging
🐳 Docker & Kubernetes deployment


👨‍💻 Author

Arva Naresh
Java | Spring Boot | Microservices
