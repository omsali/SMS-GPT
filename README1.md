# 🌾 AI over SMS (Rural Assistant)

> A distributed system connecting offline users to Generative AI via standard SMS protocols.

![Java](https://img.shields.io/badge/Java-Spring%20Boot-green)
![Python](https://img.shields.io/badge/Python-FastAPI-blue)
![Kafka](https://img.shields.io/badge/Kafka-Event%20Streaming-black)
![Redis](https://img.shields.io/badge/Redis-Caching-red)
![Docker](https://img.shields.io/badge/Docker-Containerization-blue)

## 💡 The Problem
Billions of people in rural areas lack consistent internet access (4G/5G/Wi-Fi), cutting them off from modern AI tools.
**AI over SMS** bridges this digital divide by allowing users to interact with Large Language Models (LLMs) using basic 2G cellular text messaging.

## 🏗️ System Architecture
This project implements a **Event-Driven Microservices Architecture**:

1.  **Ingestion Service (Java/Spring Boot):**
    * Receives SMS webhooks from Twilio.
    * Handles **State Management** (Redis) to paginate long AI responses into 160-character chunks.
    * Produces events to Kafka topics (`questions.in`).
2.  **AI Worker (Python):**
    * Consumes Kafka events.
    * Processes natural language using LangChain/LLMs.
    * Produces answers back to Kafka (`answers.out`).
3.  **Delivery Service (Java):**
    * Consumes AI answers.
    * Splits messages into SMS-safe chunks.
    * Delivers response via Twilio API.

## 🚀 Key Features
* **Offline AI Access:** Works without internet on the user's side.
* **Smart Pagination:** Handles long AI responses (1000+ chars) by splitting them into pages (`[1/3]`, `[2/3]`).
* **Stateful Conversations:** Users can reply **"MORE"** to retrieve the next part of the answer from Redis cache (reducing LLM costs).
* **Scalable:** Decoupled Java and Python services via Kafka allow independent scaling.

## 🛠️ Tech Stack
* **Backend:** Java 17, Spring Boot 3
* **AI Engine:** Python 3.9, LangChain
* **Messaging:** Apache Kafka (Redpanda)
* **Cache:** Redis
* **Infrastructure:** Docker & Docker Compose
* **Gateway:** Twilio Programmable SMS

## 📸 Demo
*(Link to your video demo or screenshots here)*

## 🔮 Future Improvements
* **RAG (Retrieval-Augmented Generation):** Integrating a Vector DB to answer specific questions from agricultural PDF manuals.
* **Voice Support:** Adding Twilio Voice to support illiterate users.
