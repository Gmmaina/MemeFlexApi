# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY build/libs/meme-backend-all.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables with defaults
ENV MONGODB_URI=""
ENV DATABASE_NAME="memeapp"
ENV JWT_SECRET=""
ENV PORT=""

# Run the application
CMD ["java", "-jar", "app.jar"]