# 1) Build stage: use Gradle + JDK to compile & shadowJar
FROM gradle:8.14.3-jdk21 AS builder
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . .
# Run the Gradle shadowJar task (no daemon for CI)
RUN gradle clean shadowJar --no-daemon

# 2) Runtime stage: slim JDK image with only the fat JAR
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy the fat JAR from the builder stage
COPY --from=builder /home/gradle/project/build/libs/meme-backend-all.jar app.jar

EXPOSE 8080
ENV MONGODB_URI=""
ENV DATABASE_NAME="memeapp"
ENV JWT_SECRET=""

CMD ["java", "-jar", "app.jar"]
