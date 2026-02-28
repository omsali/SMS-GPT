# AI over SMS: Distributed Offline Assistant 🚀

A highly concurrent, event-driven microservices platform that democratizes AI access via SMS. This system decouples high-throughput SMS ingestion from compute-intensive LLM inference, ensuring reliable, rate-limited, and observable offline AI interactions.

## 🏗 Architecture & Flow

The system uses an asynchronous, event-driven architecture to prevent timeouts and dropped messages during long-running LLM inferences.

`User SMS` ➡️ `Twilio` ➡️ `Kong API Gateway` ➡️ `Java Spring Boot` ➡️ `Kafka (Redpanda)` ➡️ `Python AI Worker`

1. **Ingestion & Protection:** Twilio webhooks hit the **Kong API Gateway**, which enforces rate limits (5 msgs/min) and prevents spam.
2. **Event Sourcing:** The **Spring Boot** service receives the validated request and immediately acknowledges Twilio, placing the workload onto a **Kafka** topic.
3. **Processing & Caching:** A **Python Worker** consumes the event, generates the AI response, and caches multi-part messages in **Redis** using a custom pagination state machine.
4. **Observability:** The entire lifecycle is monitored via **Prometheus/Grafana** (metrics) and **Tempo** (distributed tracing via OpenTelemetry).

## ✨ Key Features

* **Event-Driven Decoupling:** Uses Redpanda (Kafka) to separate high-concurrency SMS ingestion from blocking LLM inference tasks.
* **Smart Pagination State Machine:** Utilizes Redis to cache generated responses, eliminating redundant inference calls for multi-part messages (e.g., replying "MORE") and reducing operational LLM costs by ~90%.
* **Enterprise API Gateway:** Deploys Kong API Gateway in DB-less mode for centralized rate-limiting (HTTP 429) and secure webhook routing.
* **Full Observability Suite:** Containerized integration of Prometheus, Grafana, and Tempo for deep visibility into container metrics, API limits, and distributed request tracing.

## 💻 Tech Stack

* **Languages:** Java 17 (Spring Boot 3), Python 3
* **Message Broker:** Redpanda (Kafka-compatible)
* **Database/Cache:** PostgreSQL, Redis
* **Infrastructure:** Kong API Gateway, Docker, Docker Compose
* **Observability:** Prometheus, Grafana, Grafana Tempo, Micrometer, OpenTelemetry (OTLP)
* **External APIs:** Twilio SMS API, OpenAI / LLM API

---

## 🚀 Local Setup & Installation

### 1. Prerequisites
* Docker & Docker Compose
* Ngrok (for exposing the local gateway to Twilio)
* A Twilio Account with an active phone number

### 2. Environment Variables
Create a `.env` file inside the `infra/` directory with the following secrets:

```properties
# Twilio Secrets
TWILIO_ACCOUNT_SID=your_account_sid
TWILIO_AUTH_TOKEN=your_auth_token
TWILIO_PHONE_NUMBER=+1234567890

# Database Secrets
POSTGRES_USER=sms_user
POSTGRES_PASSWORD=sms_password
POSTGRES_DB=sms_db
