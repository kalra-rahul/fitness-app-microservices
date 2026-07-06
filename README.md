# Fitness Microservices Platform 🏋️

A **cloud-native, event-driven fitness platform** built with Spring Boot microservices, React frontend, and containerized deployment. Demonstrates enterprise-grade architecture patterns including service discovery, API gateway, centralized config, OAuth2 security, and event streaming.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                           CLIENT LAYER                              │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │          React SPA (React 19 + MUI + Redux Toolkit)          │   │
│  │                     Port :5173 (dev) / :80                    │   │
│  └──────────────────────────┬───────────────────────────────────┘   │
│                              │                                       │
│                              │  REST + JWT (OAuth2 PKCE)            │
│                              ▼                                       │
├─────────────────────────────────────────────────────────────────────┤
│                         GATEWAY LAYER                                │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │         Spring Cloud API Gateway (Port :8080)                │   │
│  │  ┌──────────────────────────────────────────────────────┐   │   │
│  │  │  Security: OAuth2 Resource Server (JWT validation)  │   │   │
│  │  │  Routing: Service discovery via Eureka (LB)         │   │   │
│  │  │  CORS: Allowed origins for frontend                 │   │   │
│  │  └──────────────────────────────────────────────────────┘   │   │
│  └──┬──────────────────┬──────────────────┬────────────────────┘   │
│     │                  │                  │                         │
│     ▼                  ▼                  ▼                         │
├─────────────────────────────────────────────────────────────────────┤
│                       MICROSERVICES LAYER                           │
│                                                                     │
│  ┌──────────────┐  ┌────────────────┐  ┌──────────────────┐       │
│  │  User Service │  │Activity Service │  │    AI Service    │       │
│  │  (Port :8001) │  │  (Port :8002)  │  │  (Port :8003)   │       │
│  │               │  │                │  │                  │       │
│  │ • Registration│  │ • Track workout│  │ • AI recommend.  │       │
│  │ • Profile mgmt│  │ • Activity crud│  │ • Gemini integ.  │       │
│  │ • JPA/Hibernate│ │ • Kafka events  │  │ • Kafka consumer │       │
│  └───────┬───────┘  └───────┬────────┘  └────────┬─────────┘       │
│          │                  │                     │                 │
│          ▼                  ▼                     ▼                 │
│  ┌──────────────┐  ┌────────────────┐  ┌──────────────────┐       │
│  │  PostgreSQL   │  │    MongoDB     │  │    MongoDB       │       │
│  │  (SQL/Rel.)   │  │  (NoSQL/Docs)  │  │  (NoSQL/Docs)   │       │
│  └──────────────┘  └────────────────┘  └──────────────────┘       │
│                                                                     │
├─────────────────────────────────────────────────────────────────────┤
│                       INFRASTRUCTURE LAYER                          │
│                                                                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────┐ │
│  │   Keycloak   │  │    Kafka     │  │   Zookeeper              │ │
│  │  (Auth/OAuth) │  │ (Event Bus)  │  │  (Kafka Coordinator)    │ │
│  │  Port :8181  │  │  Port :9092  │  │  Port :2181              │ │
│  └──────────────┘  └──────────────┘  └──────────────────────────┘ │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Config Server (Port :8888)  ← Centralized config (native)  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Eureka Server (Port :8761)  ← Service Discovery Registry   │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

### Data Flow

```
User Request
    │
    ▼
React SPA ──(JWT/OAuth2 PKCE)──▶ Keycloak (Auth)
    │
    │ (JWT Token)
    ▼
API Gateway ──(validates JWT)──▶ Routes to Service
    │
    ├──▶ User Service ──▶ PostgreSQL
    ├──▶ Activity Service ──▶ MongoDB ──(Kafka event)──▶ AI Service
    └──▶ AI Service ──▶ MongoDB ──(Gemini API)──▶ Recommendations
```

---

## Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Runtime |
| Spring Boot | 4.1.0 | Microservices framework |
| Spring Cloud | Latest | Config, Gateway, Eureka |
| Spring Data JPA | - | ORM / PostgreSQL |
| Spring Data MongoDB | - | NoSQL data access |
| Spring Cloud Gateway | - | API Gateway (reactive) |
| Spring Security OAuth2 | - | JWT resource server |
| Apache Kafka | 7.7.0 | Event streaming |
| Netflix Eureka | - | Service discovery |
| Spring Cloud Config | - | Centralized config |
| Keycloak | 25.0.0 | OAuth2 / OpenID Connect |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| React | 19 | UI framework |
| Redux Toolkit | 2.6 | State management |
| React Router | 7.2 | Client routing |
| Material UI (MUI) | 6.4 | UI components |
| Axios | 1.8 | HTTP client |
| Vite | 6.2 | Build tool |
| react-oauth2-code-pkce | 1.22 | OAuth2 PKCE flow |

### Infrastructure & DevOps
| Technology | Purpose |
|------------|---------|
| Docker | Containerization |
| Docker Compose | Local orchestration |
| PostgreSQL | User data (relational) |
| MongoDB | Activity & recommendation data (document) |
| Apache Kafka | Async event-driven communication |
| OCI Container Registry | Docker image registry |

---

## Microservices Breakdown

### 1. Config Server (`:8888`)
Centralized configuration hub. All services fetch their config from here on startup.
- **Pattern**: Spring Cloud Config Server (native profile)
- **Configs served**: `user-service.yml`, `activity-service.yml`, `ai-service.yml`, `api-gateway.yml`

### 2. Eureka Server (`:8761`)
Service discovery registry. Every microservice registers here and discovers others dynamically.
- **Pattern**: Netflix Eureka Server
- **High availability**: Supports peer awareness (production)

### 3. User Service (`:8001`)
Manages user accounts and profiles.
- **Endpoints**: `POST /api/users/register`, `GET /api/users/{id}`, `GET /api/users/{id}/validate`
- **DB**: PostgreSQL with JPA/Hibernate
- **Pattern**: Database-per-service

### 4. Activity Service (`:8002`)
Tracks and manages fitness activities (workouts, runs, etc.).
- **Endpoints**: `POST /api/activities`, `GET /api/activities`, `GET /api/activities/{id}`
- **DB**: MongoDB (document store for flexible activity schemas)
- **Events**: Publishes `activity-events` to Kafka for downstream processing

### 5. AI Service (`:8003`)
Generates AI-powered fitness recommendations using Google Gemini.
- **Endpoints**: `GET /api/recommendations/user/{id}`, `GET /api/recommendations/activity/{id}`
- **DB**: MongoDB
- **Events**: Consumes `activity-events` from Kafka (async processing)
- **AI**: Integrates Gemini API for intelligent recommendations

### 6. API Gateway (`:8080`)
Single entry point with routing, security, and cross-cutting concerns.
- **Routing**: Load-balances requests to services via Eureka (`lb://USER-SERVICE`, etc.)
- **Security**: OAuth2 resource server validating JWT tokens from Keycloak
- **CORS**: Configured for frontend origin

### 7. Frontend (`:5173` / `:80`)
Modern React SPA with OAuth2 authentication.
- **Login**: Keycloak OAuth2 with PKCE flow
- **State**: Redux Toolkit for global state
- **UI**: Material UI components
- **API**: Communicates exclusively through API Gateway

---

## Architecture Patterns Achieved

| Pattern | Implementation |
|---------|---------------|
| **Microservices** | 6 independent Spring Boot services |
| **API Gateway** | Spring Cloud Gateway (single entry point) |
| **Service Discovery** | Netflix Eureka (dynamic registration) |
| **Centralized Config** | Spring Cloud Config Server |
| **Database per Service** | PostgreSQL (user) + MongoDB (activity, AI) |
| **Event-driven** | Kafka (async activity events) |
| **CQRS-like** | Activity service writes events, AI service reads them |
| **OAuth2 / JWT** | Keycloak + Spring Security Resource Server |
| **Containerization** | Docker + multi-stage builds |
| **Orchestration** | Docker Compose (local) |
| **Polyglot Persistence** | SQL + NoSQL coexistence |

---

## How to Run

### Prerequisites
- Docker & Docker Compose
- Java 17 + Maven (optional — Docker builds handle this)

### Step 1: Clone & Build
```bash
git clone <repo-url>
cd fitness-microservices

# Build all JARs (optional — Docker builds automatically)
cd configserver && mvn clean package -DskipTests && cd ..
cd eureka && mvn clean package -DskipTests && cd ..
cd userservice && mvn clean package -DskipTests && cd ..
cd activityservice && mvn clean package -DskipTests && cd ..
cd aiservice && mvn clean package -DskipTests && cd ..
cd gateway && mvn clean package -DskipTests && cd ..
```

### Step 2: Set environment variables
```bash
# Create .env file with your Gemini API key
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent
GEMINI_API_KEY=your-key-here
```

### Step 3: Start all services
```bash
docker compose up -d
```

### Step 4: Access the platform
| URL | Service |
|-----|---------|
| http://localhost:5173 | Frontend |
| http://localhost:8080/api | API Gateway |
| http://localhost:8761 | Eureka Dashboard |
| http://localhost:8181 | Keycloak Admin (admin/admin) |

### Step 5: Stop
```bash
docker compose down
```

---

## Project Structure

```
fitness-microservices/
├── configserver/          # Spring Cloud Config Server
│   └── src/main/resources/config/
│       ├── user-service.yml
│       ├── activity-service.yml
│       ├── ai-service.yml
│       └── api-gateway.yml
├── eureka/                # Netflix Eureka Service Registry
├── gateway/               # Spring Cloud API Gateway
├── userservice/           # User Management Service
├── activityservice/       # Activity Tracking Service
├── aiservice/             # AI Recommendation Service
├── fitness-app-frontend/  # React SPA Frontend
├── docker-compose.yml     # Local orchestration
├── docker-compose.ocir.yml # OCI-ready compose
├── .env                   # Environment variables
└── DEPLOYMENT_GUIDE.md    # Deployment instructions
```

---

## Key Achievements

✅ **6 independent microservices** with clear domain boundaries
✅ **Event-driven architecture** using Kafka for async communication
✅ **AI integration** with Google Gemini for intelligent recommendations
✅ **OAuth2 security** with Keycloak and JWT-based authentication
✅ **Containerized** with multi-stage Docker builds (optimized images)
✅ **Service discovery & load balancing** via Eureka
✅ **Centralized configuration** with Spring Cloud Config
✅ **Polyglot persistence** — PostgreSQL + MongoDB for different data needs
✅ **Modern React frontend** with Redux state management and MUI design
✅ **Fully runnable with single command** (`docker compose up -d`)
