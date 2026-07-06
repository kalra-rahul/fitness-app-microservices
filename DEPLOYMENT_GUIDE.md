# Fitness Microservices — Deployment Guide (100% Free)

## Two Ways to Deploy

| Method | Cost | Sessions | Best For |
|--------|------|----------|----------|
| **Docker Desktop (Local)** | Free forever | No limit | Daily learning, development, testing |
| **Play with Docker** | Free | 4-hour sessions | Practicing cloud-like deployment in browser |

---

## Prerequisites (Skip if already done)

### Install Docker Desktop (Local)
1. Go to https://docs.docker.com/desktop/install/windows-install/
2. Download Docker Desktop for Windows
3. Install (requires WSL2 — Docker installer will guide you)
4. Open Docker Desktop — it should show "Engine running"
5. Verify:
   ```powershell
   docker --version
   docker compose version
   ```

### Docker Hub Account (for Play with Docker)
1. Go to https://hub.docker.com/signup
2. Create free account (email + password, no credit card)
3. Verify email

---

## Option 1: Deploy Locally (Docker Desktop)

Best for learning. Everything runs on your machine.

### Step 1: Build all JARs

Open 6 terminals in the project root:

```powershell
# Terminal 1
cd configserver
mvn clean package -DskipTests
```

```powershell
# Terminal 2
cd eureka
mvn clean package -DskipTests
```

```powershell
# Terminal 3
cd userservice
mvn clean package -DskipTests
```

```powershell
# Terminal 4
cd activityservice
mvn clean package -DskipTests
```

```powershell
# Terminal 5
cd aiservice
mvn clean package -DskipTests
```

```powershell
# Terminal 6
cd gateway
mvn clean package -DskipTests
```

> **No Maven installed?** Use the provided Dockerfiles which build inside the container (skip to Step 2 — Docker Compose will build automatically).

### Step 2: Set environment variables

Edit `.env` file in project root:
```
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent
GEMINI_API_KEY=your-actual-gemini-api-key-here
```

Get a Gemini API key from https://aistudio.google.com/apikey

### Step 3: Start everything

```powershell
# From project root (fitness-microservices)
docker compose up -d
```

### Step 4: Watch it boot up

```powershell
docker compose ps
docker compose logs --tail=50 -f
```

Startup takes 2-3 minutes. Services start in this order:
1. **Infra**: postgres, mongodb, zookeeper, keycloak
2. **Kafka**: (depends on zookeeper)
3. **config-server**: (provides configs)
4. **eureka**: (service discovery)
5. **user-service, activity-service, ai-service**
6. **gateway**: (routes to all services)
7. **frontend**: (React app via nginx)

### Step 5: Test it

| URL | What it is |
|-----|-----------|
| http://localhost:5173 | Frontend |
| http://localhost:8080/api | Gateway (API entry point) |
| http://localhost:8761 | Eureka Dashboard |
| http://localhost:8181 | Keycloak Admin (admin/admin) |

### Step 6: Stop everything

```powershell
docker compose down
```

To also delete volumes (clear database data):
```powershell
docker compose down -v
```

---

## Option 2: Deploy on Play with Docker (Browser)

Simulates cloud deployment. Sessions last 4 hours.

### Step 1: Push your code to GitHub

```powershell
# Create a GitHub account if you don't have one
# Create a new repo on GitHub (public)
# Push your code
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/<your-username>/fitness-microservices.git
git push -u origin main
```

### Step 2: Open Play with Docker

1. Go to https://labs.play-with-docker.com/
2. Click **"Start"**
3. Sign in with your Docker Hub account
4. Click **"ADD NEW INSTANCE"** — a terminal opens

### Step 3: Clone your repo

```bash
git clone https://github.com/<your-username>/fitness-microservices.git
cd fitness-microservices
```

### Step 4: Set environment variables

```bash
export GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent
export GEMINI_API_KEY=your-actual-gemini-api-key-here
```

### Step 5: Build & Start all services

```bash
docker compose up -d
```

This builds all images from Dockerfiles, pulls infra images (postgres, mongo, etc.), and starts everything.

### Step 6: Monitor

```bash
docker compose ps
docker compose logs --tail=30 -f config-server
```

### Step 7: Access your services

Play with Docker shows ports at the top of the page. Click on a port number (e.g., 8080, 8761) to open it in a new tab.

### Step 8: Stop

```bash
docker compose down
```

The session auto-deletes after 4 hours.

---

## Useful Docker Commands (For Future)

```bash
# ===== BUILDING =====
docker build -t my-image:tag ./path       # Build image from Dockerfile
docker build --no-cache -t my-image:tag . # Force rebuild (no cache)

# ===== IMAGES =====
docker images                             # List all images
docker rmi my-image:tag                   # Delete an image
docker system prune -a                    # Delete ALL unused images/containers

# ===== CONTAINERS =====
docker ps                                 # List running containers
docker ps -a                              # List all containers (including stopped)
docker stop container-name                # Stop a container
docker start container-name               # Start a stopped container
docker rm container-name                  # Delete a container
docker logs -f container-name             # Follow logs of a container
docker exec -it container-name sh         # Open shell inside a container
docker stats                              # Live CPU/memory usage

# ===== DOCKER COMPOSE =====
docker compose up -d                      # Start all services (detached)
docker compose up -d service-name         # Start a specific service
docker compose down                       # Stop all services
docker compose down -v                    # Stop + delete volumes (data loss!)
docker compose ps                         # List services status
docker compose logs -f                    # Follow all logs
docker compose logs -f service-name       # Follow logs of one service
docker compose build                      # Rebuild images
docker compose build service-name         # Rebuild one service
docker compose pull                       # Pull latest images
docker compose restart service-name       # Restart one service

# ===== NETWORKING =====
docker network ls                         # List networks
docker network inspect bridge             # Inspect default network

# ===== VOLUMES =====
docker volume ls                          # List volumes
docker volume rm volume-name              # Delete a volume
```

---

## Architecture Diagram

```
┌──────────────────────────────────────────────────────┐
│                   docker-compose                      │
│                                                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐           │
│  │ Postgres │  │  MongoDB │  │Zookeeper │           │
│  │  :5432   │  │  :27017  │  │  :2181   │           │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘           │
│       │              │              │                 │
│       │         ┌────▼──────────────▼─────┐          │
│       │         │        Kafka           │          │
│       │         │        :9092           │          │
│       │         └────────────────────────┘          │
│       │                                             │
│  ┌────▼────────────────────────────────────────┐    │
│  │           Config Server :8888                │    │
│  └────────────────────┬────────────────────────┘    │
│                       │                             │
│  ┌────────────────────▼────────────────────────┐    │
│  │              Eureka :8761                    │    │
│  └──┬─────────┬─────────┬──────────┬───────────┘    │
│     │         │         │          │                 │
│  ┌──▼───┐ ┌───▼────┐ ┌──▼────┐ ┌─▼──────────┐      │
│  │ User │ │Activity│ │  AI   │ │ Keycloak   │      │
│  │:8001 │ │:8002   │ │:8003  │ │ :8181      │      │
│  └──┬───┘ └───┬────┘ └──┬────┘ └─────┬──────┘      │
│     │         │         │            │              │
│  ┌──▼─────────▼─────────▼────────────▼──────────┐   │
│  │              Gateway :8080                    │   │
│  └──────────────────────┬───────────────────────┘   │
│                         │                           │
│  ┌──────────────────────▼───────────────────────┐   │
│  │           Frontend (Nginx) :80               │   │
│  └──────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────┘
```

---

## Troubleshooting

### Port already in use
```powershell
# Find what's using the port:
netstat -ano | findstr :8080
# Kill the process: note the PID, then
taskkill /PID <PID> /F
```

### Container exits immediately
```bash
docker compose logs <service-name>
# Common causes: missing env vars, DB not ready, port conflict
```

### Can't connect to database
Check service name in `application.yml` matches docker-compose service name.
- Inside Docker: use service name (e.g., `postgres`)
- NOT `localhost`

### Maven build slow
Add `--no-cache` or use Docker's layer caching — don't modify `pom.xml` often.

### Out of disk space
```powershell
docker system prune -a --volumes
# This deletes ALL unused images, containers, volumes
```

### WSL2 memory high (Windows)
Create `%USERPROFILE%\.wslconfig`:
```
[wsl2]
memory=4GB
processors=2
```
Then restart WSL: `wsl --shutdown`

---

## Next Steps After Learning

Once you're comfortable with Docker and want permanent hosting:

| Service | Cost | Best For |
|---------|------|----------|
| **Hetzner VPS** | €3.49/mo | Full VM, run docker-compose |
| **Fly.io** | Free 3 VMs | Small always-on services |
| **Railway** | Free $5 credit | Quick deployments |
| **Koyeb** | Free 1 app | Single service hosting |

All accept PayPal. No credit card needed.
