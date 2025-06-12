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
* support the ability to handle a large number of HTTP GET requests.
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
Used: SpringBoot 3.5.0, Eclipse Temurin JDK 21, PostgreSQL 17.5

