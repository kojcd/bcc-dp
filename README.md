# SRC BCC - Backend Coding Challenge
____________________________________

# Demo Project
Your final product should be a backend application for movies and actors management via REST API, described in
details in the following sections. Keep in mind that application can be used by multiple clients at the same time and all
data needs to be persisted somewhere (H2 database will be ok). UI is not part of this demo project.

Tasks:
* implement HTTP cache mechanisms where is needed
* implement request counter for all REST calls
* protect services with modern authorization mechanisms
* implement movies and actor services as at least two independent deployable services
* support the ability to handle a large number of HTTP GET requests
* provide (or describe) mechanism how database objects can be initialized
* dockerize services
* provide docker compose file
  
# Movies
Movies have properties as title, year, description, list of actors, some pictures, etc. (as identifier use imdbID).
Implement REST operations to support basic UI requirements:
* list all movies
* list movies with pagination support
* search of movie
* CRUD operations
  
# Actors
Actors have properties as first name, last name, born date, list of movies, etc.
Implement REST operations to support basic UI requirements:
* list all actors
* list actors with pagination support
* CRUD operations

Prepared by: src.si
Implemented: Damjan Kojc, damjan.kojc@gmail.com
Used: SpringBoot 3.5.0 (with springdoc-openapi 2.8.9), Eclipse Temurin JDK 21, PostgreSQL 17.5
____________________________________

# Notes to implementation of demo project

Swagger-UI (overview of REST API methods, available testing, /v3/api-docs):
Movies Service: http://localhost:8081/swagger-ui/index.html , http://localhost:8081/v3/api-docs
Actors Service: http://localhost:8082/swagger-ui/index.html , http://localhost:8082/v3/api-docs

Protect services with modern authorization mechanisms:
* in both services I prepared endpoint for requesting JWT token with credentials demo/password123, credential stored in application.yml/application-docker.yml properties for demo purposes (suggested use of Keycloak):
/api/auth/test-token

Database container 'postgres':
* database (named bcc_db) is PostgreSQL database in additional container, every service has his own schema (independent): actors, movies
* for running tests in both services is used H2 database
* used the same JPA/Hibernate model
* init of database bcc_db and both schemas, users, privileges... is done with init.sql script in init-db/init.sql, tables in schemas are created with JPA/Hibernate from entities

Service containers 'actors-service' and 'movies-service':
* independent deployable services with REST API (SpringBoot applications running on Apache Tomcat)

HTTP cache mechanism and support the ability to handle a large number of HTTP GET requests:
* all is configurable via application.yml/application-docker.yml
* used Caffeine cache is a high-performance cache library for Java - section 'spring.cache'
* Apache Tomcat configuration - section 'server'.

Docker compose file prepared. Also instructions, how to build and run.

Availible metrics:
* In-memory request counter (AtomicLong) in both services
* Micrometer/Prometheus metrics through Actuator endpoints
* HTTP request tracking with path and method information
* Request duration measurements

All of these are accessible through the Actuator endpoints without any performance impact on the actual service operations.
Movies Service (port 8081):
1. Health Check:
   http://localhost:8081/actuator/health

2. Basic Info:
   http://localhost:8081/actuator/info

3. All Metrics:
   http://localhost:8081/actuator/metrics

4. Specific Metrics (examples):
   http://localhost:8081/actuator/metrics/http_requests_total
   http://localhost:8081/actuator/metrics/http_request_duration_seconds
   http://localhost:8081/actuator/metrics/jvm.memory.used
   http://localhost:8081/actuator/metrics/system.cpu.usage

5. Prometheus Format:
   http://localhost:8081/actuator/prometheus

6. Request Counter (custom endpoint):
   http://localhost:8081/api/movies/stats/requests

Actors Service (port 8082):

1. Health Check:
   http://localhost:8082/actuator/health

2. Basic Info:
   http://localhost:8082/actuator/info

3. All Metrics:
   http://localhost:8082/actuator/metrics

4. Specific Metrics (examples):
   http://localhost:8082/actuator/metrics/http_requests_total
   http://localhost:8082/actuator/metrics/http_request_duration_seconds
   http://localhost:8082/actuator/metrics/jvm.memory.used
   http://localhost:8082/actuator/metrics/system.cpu.usage

5. Prometheus Format:
   http://localhost:8082/actuator/prometheus

6. Request Counter (custom endpoint):
   http://localhost:8082/api/actors/stats/requests