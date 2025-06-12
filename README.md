# mogako

This project was created using the [Ktor Project Generator](https://start.ktor.io).

Here are some useful links to get you started:

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- The [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). You'll need to [request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up) to join.

## Features

Here's a list of features included in this project:

| Name                                                                   | Description                                                                        |
| ------------------------------------------------------------------------|------------------------------------------------------------------------------------ |
| [Koin](https://start.ktor.io/p/koin)                                   | Provides dependency injection                                                      |
| [Routing](https://start.ktor.io/p/routing)                             | Provides a structured routing DSL                                                  |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization) | Handles JSON serialization using kotlinx.serialization library                     |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation)     | Provides automatic content conversion according to Content-Type and Accept headers |
| [Postgres](https://start.ktor.io/p/postgres)                           | Adds Postgres database to your application                                         |
| [Exposed](https://start.ktor.io/p/exposed)                             | Adds Exposed database to your application                                          |
| [Request Validation](https://start.ktor.io/p/request-validation)       | Adds validation for incoming requests                                              |
| [Authentication](https://start.ktor.io/p/auth)                         | Provides extension point for handling the Authorization header                     |
| [Authentication OAuth](https://start.ktor.io/p/auth-oauth)             | Handles OAuth Bearer authentication scheme                                         |
| [Micrometer Metrics](https://start.ktor.io/p/metrics-micrometer)       | Enables Micrometer metrics in your Ktor server application.                        |
| [Status Pages](https://start.ktor.io/p/status-pages)                   | Provides exception handling for routes                                             |
| [Default Headers](https://start.ktor.io/p/default-headers)             | Adds a default set of headers to HTTP responses                                    |
| [Shutdown URL](https://start.ktor.io/p/shutdown-url)                   | Enables a URL that shuts down the server when accessed                             |
| [Call Logging](https://start.ktor.io/p/call-logging)                   | Logs client requests                                                               |
| [Call ID](https://start.ktor.io/p/callid)                              | Allows to identify a request/call.                                                 |
| [OpenAPI](https://start.ktor.io/p/openapi)                             | Serves OpenAPI documentation                                                       |

## Building & Running

To build or run the project, use one of the following tasks:

| Task                          | Description                                                          |
| -------------------------------|---------------------------------------------------------------------- |
| `./gradlew test`              | Run the tests                                                        |
| `./gradlew build`             | Build everything                                                     |
| `buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `buildImage`                  | Build the docker image to use with the fat JAR                       |
| `publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `run`                         | Run the server                                                       |
| `runDocker`                   | Run using the local docker image                                     |

## GitHub Actions Workflow

This project includes a GitHub Actions workflow that automatically runs tests, builds Docker images, and publishes them to Docker Hub when a commit message starts with `build:`.

### Workflow Steps

1. Runs tests for all modules
2. Builds Docker images for auth-api and calendar-api
3. Publishes Docker images to Docker Hub

### Setting Up GitHub Secrets

To use this workflow, you need to set up the following GitHub secrets:

1. `DOCKER_HUB_USERNAME`: Your Docker Hub username
2. `DOCKER_HUB_PASSWORD`: Your Docker Hub password or access token

### Triggering the Workflow

To trigger the workflow, push a commit with a message that starts with `build:`:

```bash
git commit -m "build: your commit message here"
git push
```

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```
