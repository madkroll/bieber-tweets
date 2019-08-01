# Description
This service subscribes in real-time on Twitter Streaming API and collects filtered tweets.
All results are printed to the console in a JSON format.
To properly authorize application - input is required during obtaining OAuth session.

# Technology stack
I decided to go for such set of tools:
- Spring Boot 2+ - DI/IoC, properties framework, console runner, reactive support, non-blocking webclient
- Project Reactor (https://projectreactor.io/) - as an engine for reactive approach
- Lombok - reduce boilerplate code for model classes, logging setup
- JUnit / AssertJ / Mockito - regular Java unit testing
- okhttp3 MockWebServer - to test remote HTTP communication
- Project Reactor test - to test reactive approach
- google-oauth-client - only to send initial OAuth request and open authorized session
- Docker - to push and run application as a container
- Maven - as a building tool

# Comments
After learning more about Twitter API - I decided to solve this task using reactive approach.
There were two main reasons for it.

First, I noticed that Twitter Streaming API is reactive-friendly and sends
real-time data as soon as it's published. After reading carefully task description I also noticed few requirements
where reactive programming has strong points:
- collecting data for fixed limited duration
- limiting number of received results
- Spring Framework 5+ has a built-in support to subscribe for reactive services (WebClient) with simple and clear API

Second reason was that actually I never worked with reactive systems before and wanted to learn new technology.
So now I understand better reactive concepts, related technology stack, testing approaches,
most common issues and limitations.

I also decided to minimize usage of Google OAuth Client library. First I found it's API not effective from
main task's requirements perspective. Then I decided to check it's sources and started to doubt it's really thread-safe
as JavaDoc says - public non-final fields are often mutating during request (e.g. OAuthParameters class).
So then I left it only to authorize Twitter session and started to use WebClient for main functionality.

# Things To-Do:
- check if sending HTTP requests should happen on IO schedulers
- finalize code coverage (few classes are missing it, however I tried to demonstrate all different testing approaches required)
- finish first bonus point: actually I didn't understand what it is about, so left undone. Sounds like I need to setup any kind of persistence
and collect sort of metrics.

# Requirements
- Java 8
- Maven 3+
- Docker
- available internet traffic
- browser (to authorize application via Twitter)

# Build application
to build application run:
```bash
# Verify Maven version
mvn -v

# build project and store docker image locally in repository
mvn clean package
```

# Running
## JAR
To run application as a simple Java process:
```bash
# Verify Java version is 8
java --version

# Run application with default settings
java -jar bieber-tweets-core/target/bieber-tweets-core-1.0.0-SNAPSHOT.jar

# Run application with custom settings
java -jar bieber-tweets-core/target/bieber-tweets-core-1.0.0-SNAPSHOT.jar \
    --service.hashtag="bieber" \
    --service.max-items="100" \
    --service.max-duration-in-seconds="30"
```
## Docker
To run application as a Docker container:
```bash
# Runs container with enabled console input
docker run -it bieber-tweets-core:1.0.0-SNAPSHOT
```