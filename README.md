# Bookmark Saver API

> ⚠️ This project is in an early development stage. Features and APIs may change.

A RESTful API built with **Java** and **Spring Boot** for saving and organizing bookmarks using tags.

The project provides a clean and scalable backend architecture with support for bookmark management, tagging, filtering, pagination, and automatic metadata enrichment.

## Features

- Create, update, retrieve, and delete bookmarks
- Organize bookmarks with tags
- Bulk tag ↔ bookmark association updates
- Filter bookmarks by favorite and tag
- Pagination support
- Automatic metadata enrichment
- Flyway database migrations
- PostgreSQL support

## Tech Stack

- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- Docker

## Getting Started

### Requirements

- Java 21+
- Maven
- PostgreSQL
- Docker

### Run with Docker

Before starting, create your local `.env` file from the example provided in the project:

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

Finally, start the environment with:

```bash
docker compose up --build
```

## API Overview

### Bookmarks

```http
GET    /api/bookmarks
GET    /api/bookmarks/{bookmarkId}
POST   /api/bookmarks
PUT    /api/bookmarks/{bookmarkId}
PUT    /api/bookmarks/{bookmarkId}/tags
DELETE /api/bookmarks/{bookmarkId}
```

### Tags

```http
GET    /api/tags
GET    /api/tags/{tagId}
POST   /api/tags
PUT    /api/tags/{tagId}
PUT    /api/tags/{tagId}/bookmarks
DELETE /api/tags/{tagId}
```

Bookmarks can be filtered and paginated using query parameters.

#### Supported filters

- `favorite` → filter favorite bookmarks
- `tags` → filter by comma-separeted tag names (case-insensitive)

#### Pagination parameters

Standard Spring pagination parameters are supported:

- `page`
- `size`
- `sort`

Examples:

```http
GET /api/bookmarks?favorite=true
GET /api/bookmarks?favorite=true&tags=design,tech
GET /api/bookmarks?favorite=true&page=0&size=10&sort=createdAt,desc
```

## Project Status

The project is actively evolving with a focus on:

- bookmark / tag management
- clean REST API design
- security
- scalability

### Roadmap

Planned improvements and future work:

- [x] Support multiple tags in filtering
- [x] Improve metadata extraction
- [ ] Support for bookmark lists
- [ ] Add JWT authentication with protected routes
- [ ] Add full test coverage
- [ ] Add user interface

## License

This project is licensed under the **Apache License 2.0** license. See [LICENSE](./LICENSE) for more information.
