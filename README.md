# MemeFlex

MemeFlex is a modern web application designed to provide a flexible and scalable platform for creating, sharing, and managing memes. Built with [Ktor](https://ktor.io), it leverages cutting-edge features to deliver a seamless and efficient experience for developers and users alike.

## Features

MemeFlex includes the following features:

| Feature                                                               | Description                                                                        |
| ----------------------------------------------------------------------|------------------------------------------------------------------------------------ |
| [CORS](https://start.ktor.io/p/cors)                                  | Enables Cross-Origin Resource Sharing (CORS) for secure API access                 |
| [Rate Limiting](https://start.ktor.io/p/ktor-server-rate-limiting)    | Controls the rate of incoming requests to prevent abuse                            |
| [Routing](https://start.ktor.io/p/routing)                            | Provides structured and efficient routing for API endpoints                        |
| [Authentication](https://start.ktor.io/p/auth)                        | Handles user authentication and session management                                 |
| [Authentication JWT](https://start.ktor.io/p/auth-jwt)                | Supports JSON Web Token (JWT) bearer authentication                                |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation)    | Automatically converts content based on `Content-Type` and `Accept` headers        |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization)| Handles JSON serialization using the kotlinx.serialization library                 |
| [Status Pages](https://start.ktor.io/p/status-pages)                  | Provides exception handling and custom error responses                             |
| [Call Logging](https://start.ktor.io/p/call-logging)                  | Logs client requests for debugging and monitoring                                  |

## Getting Started

Follow these steps to set up and run the project:

### Prerequisites

- [JDK 17+](https://adoptopenjdk.net/)
- [Gradle](https://gradle.org/) (or use the included Gradle wrapper)
- [Docker](https://www.docker.com/) (optional, for containerized deployment)

### Building & Running

To build or run the project, use the following commands:

| Task                          | Description                                                          |
| -------------------------------|---------------------------------------------------------------------- |
| `./gradlew test`              | Run the tests                                                        |
| `./gradlew build`             | Build the project                                                    |
| `./gradlew run`               | Run the server locally                                               |
| `./gradlew buildFatJar`       | Build an executable JAR with all dependencies                        |
| `./gradlew buildImage`        | Build a Docker image for deployment                                  |
| `./gradlew publishImageToLocalRegistry` | Publish the Docker image locally                                |

Once the server starts successfully, you can access it at:

```
http://localhost:8080
```

### Example Output

When the server starts, you should see output similar to this:

```
2025-07-19 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2025-07-19 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

## Contributing

Contributions are welcome! Feel free to open issues or submit pull requests to improve the project.

## License

This project is licensed under the [MIT License](LICENSE).

## Acknowledgments

- Built with [Ktor](https://ktor.io)
- Inspired by the creativity of meme culture
