# Bookmark Saver API

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

```bash
docker compose up --build
```

## API Overview

### Bookmarks

```http
GET    /api/bookmarks
GET    /api/bookmarks/{id}
POST   /api/bookmarks
PUT    /api/bookmarks/{id}
PUT    /api/bookmarks/{id}/tags
DELETE /api/bookmarks/{id}
```

### Tags

```http
GET    /api/tags
GET    /api/tags/{id}
POST   /api/tags
PUT    /api/tags/{id}
PUT    /api/tags/{id}/bookmarks
DELETE /api/tags/{id}
```

Bookmarks can be filtered and paginated using query parameters.

#### Supported filters

- `favorite` → filter favorite bookmarks
- `tag` → filter by tag name (case-insensitive)

#### Pagination parameters

Standard Spring pagination parameters are supported:

- `page`
- `size`
- `sort`

Examples:

```http
GET /api/bookmarks?favorite=true
GET /api/bookmarks?favorite=true&tag=design
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

- [ ] Support multiple tags in filtering
- [ ] Improve metadata extraction
- [ ] Add JWT authentication with protected routes
- [ ] Add full test coverage
- [ ] Add search functionality for bookmarks

## License

This project is licensed under the **Apache License 2.0** license. See [LICENSE](./LICENSE) for more information.
