# Crypto Wallet Platform

Backend for cryptocurrency portfolio management featuring real-time tracking, weighted performance analysis, and historical audits.

## 🚀 Getting Started

### Prerequisites
* **Java 21**
* **Maven**
* **Docker & Docker Compose**

### Setup
1. **API Configuration**: Obtain a free API key from [CoinCap](https://coincap.io/api) and replace the placeholder `REPLACE_ME` in `application.properties` file:
2. **Start Database**: `docker-compose up -d`
3. **Database Migrations**: `mvn liquibase:update`
4. **Run Application**: `mvn spring-boot:run`
5. **Explore API**: Navigate to `http://localhost:8080/swagger-ui/index.html` after startup to view and test endpoints.

---

## 🛠 Assumptions

### Technical
* **Timezone**: All data stored and processed in **UTC**.

### Functional
* **Wallets**: Strict 1:1 relationship; a user can only have **one** wallet.
* **Purchase Flow**: Assumes the customer views the current price via the balance/sync data before initiating a buy order; the transaction is executed at the latest price stored in the system.
* **Price Fallback**: Uses the next most recent price or purchase price if history is missing.

---

## 🔐 Security & User Onboarding

Authentication is stateless via **JWT**. Authorities are mapped from permissions:
* **`user:read`**: View balance/performance.
* **`user:write`**: Buy assets/create wallet.

### How to Access Protected Endpoints:

1. **Register**: Send a `POST` request to `/api/v1/auth/register`.
2. **Login**: Send a `POST` request to `/api/v1/auth/login` to receive a Bearer Token.
3. **Authenticate**: Include the token in the header of subsequent requests:  
   `Authorization: Bearer <your_jwt_token>`

---

## 🧪 CI/CD & Quality Control

This project uses **GitHub Actions** for robust continuous integration. Every push to `master` or `develop` triggers the following automated pipeline:

* **Regression Testing**: Full suite of unit and integration tests (using Embedded Redis).
* **Static Analysis**: **CodeQL** and **SonarCloud** for code quality and security hotspots.
* **Security Scanning**: **Trivy** scans on the filesystem and the generated Docker image.
* **Packaging**: Automatic JAR packaging and artifact archiving.

---

## 📈 Monitoring & Actuator

The application exposes operational data via Spring Boot Actuator at `http://localhost:8080/actuator`.

| Endpoint | Description |
| :--- | :--- |
| `/actuator/health` | Check if the application and database are UP. |
| `/actuator/metrics` | View generic JVM and application metrics. |
| `/actuator/prometheus` | Scrape point for Prometheus monitoring. |

* **Prometheus**: Accessible at `http://localhost:9090/`
* **Grafana**: Accessible at `http://localhost:3000` to visualize metrics.