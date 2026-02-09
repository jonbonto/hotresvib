# HotResvib - Deployment Guide

## Production Deployment Architecture

### Infrastructure Requirements

#### PostgreSQL Database
- Version: PostgreSQL 14+
- CPU: 2 vCPU
- Memory: 4GB RAM
- Storage: 100GB SSD (scalable)
- Backup: Daily, 30-day retention

#### Redis Cache
- Version: Redis 7+
- CPU: 1 vCPU
- Memory: 1GB RAM
- Persistence: AOF (Append Only File)
- Replication: Single instance or Master-Replica

#### Application Servers
- Instances: 2+ (for high availability)
- CPU: 2 vCPU per instance
- Memory: 4GB RAM per instance
- JDK: 17+
- Health checks: Active

#### Load Balancer
- Technology: Nginx or AWS ALB
- SSL/TLS: Let's Encrypt certificate
- Port: 443 (HTTPS), 80 (HTTP redirect)
- Health check: /actuator/health every 10 seconds

### Network Architecture

```
Internet
  ↓
[Load Balancer - HTTPS/443]
  ↓
┌─────────────────────────────────────┐
│  API Instances (2+)                 │
│  - Port 8080 (internal)             │
│  - Port 9090 (metrics)              │
└─────────────────────────────────────┘
  ↓                    ↓
[PostgreSQL]      [Redis]
  Port: 5432        Port: 6379
  (Private)         (Private)
```

## Deployment Steps

### 1. Environment Preparation

```bash
# Create deployment user
useradd -m -s /bin/bash -d /opt/hotresvib hotresvib

# Create required directories
mkdir -p /opt/hotresvib/app
mkdir -p /var/log/hotresvib
mkdir -p /etc/hotresvib
chown -R hotresvib:hotresvib /opt/hotresvib /var/log/hotresvib /etc/hotresvib

# Set permissions
chmod 755 /opt/hotresvib
chmod 755 /var/log/hotresvib
chmod 600 /etc/hotresvib
```

### 2. Docker Deployment

#### Using Docker Compose (Development/Staging)

```bash
# Clone repository
git clone https://github.com/your-org/hotresvib.git
cd hotresvib

# Create .env file with secrets
cat > .env << EOF
JWT_SECRET=<generate-secure-random>
STRIPE_API_KEY_SECRET=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...
SENTRY_DSN=https://...
REDIS_PASSWORD=<secure-password>
EOF

# Start services
docker-compose up -d

# Verify services
docker-compose ps
docker-compose logs -f app
```

#### Using Kubernetes (Production)

```bash
# Create namespace
kubectl create namespace hotresvib

# Create secrets
kubectl create secret generic hotresvib-secrets \
  --from-literal=jwt-secret=<value> \
  --from-literal=stripe-key=<value> \
  -n hotresvib

# Apply Kubernetes manifests
kubectl apply -f k8s/ -n hotresvib

# Verify deployment
kubectl get pods -n hotresvib
kubectl logs -f deployment/hotresvib -n hotresvib
```

### 3. Database Initialization

```bash
# Connect to PostgreSQL
PGPASSWORD=hotresvib_pass psql -U hotresvib_user -d hotresvib -h db-host

# Run Flyway migrations (automatic on app startup)
# Or manually:
./gradlew flywayMigrate -Dflyway.url=jdbc:postgresql://host:5432/hotresvib

# Verify schema
\dt  # List tables
```

### 4. SSL/TLS Configuration

#### Using Let's Encrypt with Nginx

```bash
# Install Certbot
apt-get install certbot python3-certbot-nginx

# Generate certificate
certbot certonly --standalone -d hotresvib.example.com

# Configure Nginx (see nginx.conf example below)
cp nginx.conf /etc/nginx/sites-available/hotresvib
ln -s /etc/nginx/sites-available/hotresvib /etc/nginx/sites-enabled/
nginx -t
systemctl reload nginx

# Auto-renewal
certbot renew --dry-run
```

#### Nginx Configuration Example

```nginx
upstream hotresvib {
    server app1:8080;
    server app2:8080;
    keepalive 32;
}

server {
    listen 80;
    server_name hotresvib.example.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name hotresvib.example.com;

    ssl_certificate /etc/letsencrypt/live/hotresvib.example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/hotresvib.example.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    gzip on;
    gzip_types text/plain text/css application/json;
    gzip_min_length 1024;

    location / {
        proxy_pass http://hotresvib;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    location /health {
        access_log off;
        proxy_pass http://hotresvib/actuator/health;
    }
}
```

### 5. Monitoring Setup

#### Prometheus Configuration

```bash
# Start Prometheus scraping metrics
# Configuration at: ./prometheus.yml
docker run -d \
  -p 9091:9090 \
  -v $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml \
  -v prometheus-data:/prometheus \
  prom/prometheus

# Verify metrics endpoint
curl http://localhost:8080/actuator/prometheus
```

#### Grafana Dashboard

```bash
# Start Grafana
docker run -d \
  -p 3000:3000 \
  -e GF_SECURITY_ADMIN_PASSWORD=admin \
  -v grafana-data:/var/lib/grafana \
  grafana/grafana

# Access at: http://localhost:3000 (admin/admin)
# Add Prometheus data source: http://prometheus:9090
# Import dashboard: ID 4701 (JVM) or ID 1860 (Node Exporter)
```

### 6. Backup Strategy

#### PostgreSQL Daily Backup

```bash
# Create backup script
cat > /usr/local/bin/backup-postgres.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/backups/hotresvib"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/hotresvib_$DATE.sql.gz"

mkdir -p $BACKUP_DIR

PGPASSWORD=$DB_PASSWORD pg_dump \
  -U hotresvib_user \
  -h db-host \
  hotresvib | gzip > $BACKUP_FILE

# Keep 30 days of backups
find $BACKUP_DIR -mtime +30 -delete

# Upload to S3
aws s3 cp $BACKUP_FILE s3://hotresvib-backups/
EOF

chmod +x /usr/local/bin/backup-postgres.sh

# Schedule with cron
echo "0 2 * * * /usr/local/bin/backup-postgres.sh" | crontab -
```

#### Restore from Backup

```bash
# Restore PostgreSQL
gunzip < backup.sql.gz | psql -U hotresvib_user hotresvib

# Restore Redis
redis-cli --pipe < dump.rdb
```

### 7. Environment Variables

Create `/etc/hotresvib/.env`:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://db-host:5432/hotresvib
SPRING_DATASOURCE_USERNAME=hotresvib_user
SPRING_DATASOURCE_PASSWORD=<secure-password>

# Redis
REDIS_HOST=redis-host
REDIS_PORT=6379
REDIS_PASSWORD=<secure-password>

# JWT
JWT_SECRET=<generate-with-openssl-rand-hex-32>

# Stripe
STRIPE_API_KEY_SECRET=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Sentry
SENTRY_DSN=https://<key>@sentry.io/<project>
SENTRY_ENABLED=true

# Application
SPRING_PROFILES_ACTIVE=prod
ENVIRONMENT=production
```

### 8. Health Checks

```bash
# Application health
curl -s http://localhost:8080/actuator/health | jq .

# Database health
curl -s http://localhost:8080/actuator/health/db | jq .

# Redis health
curl -s http://localhost:8080/actuator/health/redis | jq .

# Prometheus metrics
curl -s http://localhost:9090/api/v1/targets
```

### 9. Graceful Shutdown

Application shutdown sequence (30 seconds):
1. Stop accepting new requests
2. Wait for in-flight requests to complete
3. Close database connections
4. Flush Redis cache
5. Exit

```bash
# Trigger graceful shutdown
curl -X POST http://localhost:8080/actuator/shutdown
```

### 10. Troubleshooting

#### Application won't start
```bash
# Check logs
docker logs hotresvib-app

# Check database connectivity
PGPASSWORD=pass psql -U user -h host -d hotresvib -c "SELECT 1"

# Check Redis connectivity
redis-cli -h redis-host PING
```

#### High memory usage
```bash
# Check JVM heap
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Increase heap size
export JAVA_OPTS="-Xms1g -Xmx2g"

# Restart application
docker restart hotresvib-app
```

#### Database performance issues
```bash
# Check slow queries (if configured)
# Enable in PostgreSQL: log_min_duration_statement = 1000

# Check connection pool status
curl http://localhost:8080/actuator/metrics/hikaricp.connections
```

## Production Checklist

- [ ] SSL/TLS certificate installed and renewed
- [ ] Database backups configured and tested
- [ ] Monitoring (Prometheus/Grafana) running
- [ ] Sentry error tracking enabled
- [ ] Load balancer health checks passing
- [ ] Environment variables secured (no hardcoded secrets)
- [ ] Firewall rules configured (only necessary ports open)
- [ ] Log rotation configured (/var/log/hotresvib)
- [ ] Database user has minimal required permissions
- [ ] Application restarts automatically on failure
- [ ] Backup retention policy verified (30 days minimum)
- [ ] Incident response plan documented

## Contact & Support

For deployment issues, contact the DevOps team or create an issue in the repository.
