## ASSIGNMENT-1

#Text Summarizer

A simple AI-powered Text Summarizer built using **React.js**, **Spring Boot**, **Docker**, and **Hugging Face Inference API**.

## Project Overview

This application allows users to enter large text content and generate a concise summary using an AI model hosted on Hugging Face.

### Tech Stack

* Frontend: React.js
* Backend: Spring Boot (Java 17)
* AI Model: Hugging Face (facebook/bart-large-cnn)
* Containerization: Docker
* Version Control: Git & GitHub

---

## Project Architecture

```text
User
  ↓
React Frontend (Port 3000)
  ↓ HTTP Request
Spring Boot Backend (Port 8080)
  ↓ API Call
Hugging Face Inference API
  ↓
Generated Summary
  ↓
React Frontend
```

---

## Features

* Enter text to summarize
* AI-generated summaries
* REST API integration
* Dockerized backend service
* Easy deployment and execution

---

## Backend API

### Summarize Text

**Endpoint**

```http
POST /api/summarize
```

**Request Body**

```text
Artificial Intelligence is transforming industries...
```

**Response**

```json
[
  {
    "summary_text": "Artificial Intelligence is transforming industries..."
  }
]
```

---

## Running the Frontend

Navigate to the frontend folder:

```bash
cd summarizer-frontend
```

Install dependencies:

```bash
npm install
```

Start React application:

```bash
npm start
```

Frontend will run on:

```text
http://localhost:3000
```

---

## Running the Backend

Navigate to backend folder:

```bash
cd summarizer-backend
```

Build project:

```bash
mvn clean package
```

Run application:

```bash
mvn spring-boot:run
```

Backend will run on:

```text
http://localhost:8080
```

---

## Docker Setup

### Build Docker Image

```bash
docker build -t madhav3129/ai-summarizer:v1 .
```

### Verify Image

```bash
docker images
```

### Run Container

```bash
docker run -d -p 8080:8080 --name ai-summarizer madhav3129/ai-summarizer:v1
```

### Check Running Containers

```bash
docker ps
```

### View Logs

```bash
docker logs ai-summarizer
```

### Stop Container

```bash
docker stop ai-summarizer
```

### Remove Container

```bash
docker rm ai-summarizer
```

---

## Docker Hub

### Push Image

```bash
docker push madhav3129/ai-summarizer:v1
```

### Pull Image

```bash
docker pull madhav3129/ai-summarizer:v1
```

---

## Environment Variables

Replace your Hugging Face API token inside:

```java
HuggingFaceService.java
```

```java
private final String TOKEN = "YOUR_HUGGINGFACE_TOKEN";
```

For security reasons, never commit API tokens to GitHub.

---

