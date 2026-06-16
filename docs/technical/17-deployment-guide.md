# 17 - Deployment Guide

## 1. Prerequisites
To deploy DoConnect AI to a production environment (e.g., AWS EC2, DigitalOcean Droplet, or Heroku), you will need:
*   **Java 21 JRE** (for running the `.jar` files).
*   **Node.js 20+** (only needed for building the frontend).
*   **MySQL 8.0+** (either managed like Amazon RDS or locally hosted).
*   **Nginx** (acting as a reverse proxy).

## 2. Infrastructure Setup

### 2.1 Database Provisioning
Connect to your MySQL instance and execute:
```sql
CREATE DATABASE doconnect_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE doconnect_chat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2.2 Environment Variables
Ensure the production environment contains the required secrets. Do **not** use the default development secrets. Generate a strong 64+ character string for `JWT_SECRET` and `NOTIFICATION_INTERNAL_TOKEN`.

## 3. Build Process

### 3.1 Backend & Chat Service (Maven)
Navigate into both `backend/` and `chat-service/` and execute:
```bash
./mvnw clean package -DskipTests
```
This generates fat JARs in their respective `/target/` directories.

### 3.2 Frontend (Vite)
Navigate to `frontend/`:
```bash
npm install
npm run build
```
This generates optimized static HTML/CSS/JS files in the `/dist/` folder.

## 4. Deployment Topology (Nginx)

In a single-server deployment, use `systemd` to run the two Java `.jar` files on ports `8080` and `8090`. Configure Nginx to serve the React `/dist/` folder and proxy API requests.

### Example Nginx Configuration (`/etc/nginx/sites-available/doconnect`)

```nginx
server {
    listen 80;
    server_name yourdomain.com;

    # Serve React SPA
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # Proxy REST API traffic to Main API
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Proxy Notification WebSockets (STOMP)
    location /ws/notifications {
        proxy_pass http://localhost:8080/ws/notifications;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "Upgrade";
        proxy_set_header Host $host;
    }

    # Proxy Global Chat WebSockets (STOMP)
    location /ws {
        proxy_pass http://localhost:8090/ws;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "Upgrade";
        proxy_set_header Host $host;
    }
}
```

## 5. Dockerization (Future Path)
For modern deployments, the recommended next step is creating a `docker-compose.yml` that defines:
1.  `db`: MySQL 8 image.
2.  `backend`: OpenJDK 21 image running `backend.jar`.
3.  `chat-service`: OpenJDK 21 image running `chat-service.jar`.
4.  `frontend`: Nginx Alpine image serving the built React files.

---
*Next Document: [18-design-decisions.md](18-design-decisions.md)*
