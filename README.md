# Bookmark Saver

> ⚠️ This project is in an early development stage. Features and APIs may change.

A full-stack application for saving and organizing bookmarks, built with Spring Boot and Angular.

It provides a scalable backend API and a web interface, with support for lists, tags, filtering, and pagination. 

## Features

- Bookmark management (CRUD)
- Organize bookmarks with lists and tags
- Filtering and pagination
- Automatic bookmark metadata extraction
- REST API + Web UI
- Flyway database migrations
- PostgreSQL support
- Docker-based setup
- Authentication layer (via Keycloak, WIP)

## Tech Stack

### Backend

- Java
- Spring Boot
- Spring Data JPA
- Hibernate

### Frontend

- Angular

### Infrastructure

- PostgreSQL
- Flyway
- Docker
- Keycloak (work in progress)

## Getting Started

### Requirements

- Java 21+
- Node.js
- Docker

### Run with Docker

Create your local `.env` file from the example provided in the project:

```bash
cp .env.example .env
```

Then set the environment variables you need, including `ENVIRONMENT`, which is used to load the corresponding Docker Compose file.

For example:

```env
ENVIRONMENT=dev
```

This will load `docker-compose.dev.yml`

If not specified, the application defaults to `docker-compose.prod.yml`.

Finally, start everything with:

```bash
docker compose up --build
```

## API Overview

The backend exposes a REST API for managing:

- bookmarks
- lists
- tags

Endpoints follow standard REST conventions:

```http
GET    /api/bookmarks
GET    /api/bookmarks/{id}
POST   /api/bookmarks
PUT    /api/bookmarks/{id}
PUT    /api/bookmarks/{id}/lists
PUT    /api/bookmarks/{id}/tags
DELETE /api/bookmarks/{id}
```

Supports filtering and pagination via query parameters.

## Project Status

The project is evolving towards a complete self-hostable application.

### Roadmap

- [x] Lists and tags support
- [x] Filtering and pagination
- [x] Flyway migrations
- [x] Angular frontend setup
- [ ] JWT authentication (Keycloak integration)
- [ ] UI improvements
- [ ] Database support improvements
- [ ] Production-ready Docker setup

## License

This project is licensed under the **Apache License 2.0** license. See [LICENSE](./LICENSE) for more information.
