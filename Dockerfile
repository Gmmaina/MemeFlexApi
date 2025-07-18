# Multi-stage build
FROM gradle:8.5-jdk21 AS build

# Set working directory
WORKDIR /app

# Copy gradle files first (for better caching)
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

# Copy source code
COPY src ./src

# Build the application
RUN gradle buildFatJar --no-daemon

# Runtime stage
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/build/libs/meme-backend-all.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables with defaults
ENV MONGODB_URI=""
ENV DATABASE_NAME="memeapp"
ENV JWT_SECRET=""

# Run the application
CMD ["java", "-jar", "app.jar"]