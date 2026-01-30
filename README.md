# 🚢 Rumo ao Prático - Backend

Backend API for **Rumo ao Prático**, a quiz system designed for Brazilian Navy practical exam preparation.

## Tech Stack

- **Java 21** + **Spring Boot 3.3**
- **PostgreSQL** with **Flyway** migrations
- **JWT** authentication (access + refresh tokens)
- **Swagger/OpenAPI** documentation
- **Lombok** for boilerplate reduction
- **Maven** build system

## Architecture

```
src/main/java/com/rumoaopratico/
├── config/          # Security, OpenAPI, CORS configuration
├── controller/      # REST API endpoints
├── dto/
│   ├── request/     # Input DTOs with validation
│   └── response/    # Output DTOs
├── exception/       # Global exception handling
├── model/           # JPA entities
│   └── enums/       # Enums (QuestionType, Difficulty, QuizMode)
├── repository/      # Spring Data JPA repositories
├── security/        # JWT provider, filter, user principal
└── service/         # Business logic layer
```

## API Endpoints

### Authentication (`/api/auth`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/register` | Register new user |
| POST | `/login` | Login with email/password |
| POST | `/refresh` | Refresh access token |
| POST | `/forgot-password` | Request password reset |

### Users (`/api/users`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/me` | Get current user profile |
| PUT | `/me` | Update profile |
| GET | `/me/stats` | Get user statistics |

### Topics (`/api/topics`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | List topics (paginated) |
| GET | `/{id}` | Get topic by ID |
| POST | `/` | Create topic |
| PUT | `/{id}` | Update topic |
| DELETE | `/{id}` | Delete topic |

### Questions (`/api/questions`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | List questions (paginated, filtered) |
| GET | `/{id}` | Get question by ID |
| POST | `/` | Create question with options |
| PUT | `/{id}` | Update question |
| DELETE | `/{id}` | Soft-delete question |

### Quiz (`/api/quiz`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/start` | Start quiz attempt |
| GET | `/{attemptId}` | Get attempt details |
| POST | `/{attemptId}/answer` | Submit answer |
| POST | `/{attemptId}/finish` | Finish quiz |
| GET | `/{attemptId}/result` | Get quiz result |

### History (`/api/history`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | List quiz history (paginated) |
| GET | `/{attemptId}` | Get detailed history |

### Admin (`/api/admin`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/import-questions` | Import questions from JSON |

## Running Locally

### Prerequisites
- Java 21+
- PostgreSQL 15+
- Maven 3.9+

### Database Setup
```bash
createdb rumo_pratico
```

### Run
```bash
# With Maven
mvn spring-boot:run

# Or build and run JAR
mvn clean package -DskipTests
java -jar target/rumo-ao-pratico-backend-1.0.0.jar
```

### Docker
```bash
# Build image
docker build -t rumo-ao-pratico-backend .

# Run with Docker Compose (requires docker-compose.yml with PostgreSQL)
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/rumo_pratico \
  -e SPRING_DATASOURCE_USERNAME=rumo \
  -e SPRING_DATASOURCE_PASSWORD=rumo_secret \
  rumo-ao-pratico-backend
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db:5432/rumo_pratico` | Database URL |
| `SPRING_DATASOURCE_USERNAME` | `rumo` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `rumo_secret` | Database password |
| `JWT_SECRET` | (built-in) | JWT signing secret (change in production!) |
| `JWT_ACCESS_EXPIRATION` | `900000` | Access token TTL (ms) - 15min |
| `JWT_REFRESH_EXPIRATION` | `604800000` | Refresh token TTL (ms) - 7 days |
| `SERVER_PORT` | `8080` | Server port |

## API Documentation

Once running, access Swagger UI at:
- **Swagger UI**: [http://localhost:8080/swagger](http://localhost:8080/swagger)
- **OpenAPI JSON**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Default Admin User

The seed migration creates an admin user:
- **Email**: `admin@rumo.com`
- **Password**: `admin123`

## Security

- JWT access tokens (15 min) + refresh tokens (7 days)
- BCrypt password hashing
- All endpoints except `/api/auth/**` require authentication
- Users can only access their own data (enforced at service layer)
- CORS configured for `localhost:3000` and `localhost:5173`

## Testing

```bash
mvn test
```

## License

Private - All rights reserved.
