# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

# Copy Maven wrapper first (layer cache — only re-downloads deps when pom.xml changes)
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copy source and build the JAR
COPY src src
RUN ./mvnw clean package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=builder /app/target/summarizer-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Pass your Hugging Face token at runtime: -e HUGGINGFACE_TOKEN=hf_xxx
ENTRYPOINT ["java", "-jar", "app.jar"]