# HotResvib - Performance & Optimization Guide

## Performance Benchmarks & Targets

### API Response Time Targets (p95)

| Endpoint | Target | Current | Status |
|----------|--------|---------|--------|
| GET /api/hotels (search) | < 200ms | TBD | 📊 |
| GET /api/hotels/{id} (details) | < 100ms | TBD | 📊 |
| GET /api/availability | < 150ms | TBD | 📊 |
| POST /api/reservations (create) | < 500ms | TBD | 📊 |
| POST /api/payments (process) | < 1000ms | TBD | 📊 |
| GET /api/users/{id}/bookings | < 200ms | TBD | 📊 |

### System Requirements

- Database query latency: < 50ms (p95)
- Cache hit rate: > 80% for hotel searches
- API throughput: > 500 req/sec
- Max concurrent connections: 50+

## Optimization Strategies

### 1. Redis Caching

#### Cache Configuration
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1 hour default

  redis:
    host: redis
    port: 6379
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
```

#### Cache-Aside Pattern
```kotlin
@Cacheable("hotels", key = "#id")
fun getHotel(id: String): Hotel { ... }

@CacheEvict("hotels", key = "#hotel.id")
fun updateHotel(hotel: Hotel) { ... }

@CachePut("hotels", key = "#result.id")
fun createHotel(hotel: Hotel): Hotel { ... }
```

#### Cache TTL Strategy
- Hotel list: 1 hour (changes infrequently)
- Room details: 30 minutes (can change with pricing)
- Availability: 5 minutes (must reflect bookings)
- Pricing rules: 1 hour (admin-controlled)

### 2. Database Query Optimization

#### N+1 Query Prevention
```kotlin
// ❌ Bad: N+1 queries (1 hotel + N rooms)
val hotels = hotelRepository.findAll()
hotels.forEach { hotel ->
    println(hotel.rooms.size)  // Triggers N additional queries
}

// ✅ Good: JOIN FETCH
@Query("""
    SELECT DISTINCT h FROM Hotel h
    LEFT JOIN FETCH h.rooms
    WHERE h.city = :city
""")
fun findByCityWithRooms(city: String): List<Hotel>
```

#### Database Indexes
```sql
-- Hotel search optimization
CREATE INDEX idx_hotels_city_country ON hotels(city, country);
CREATE INDEX idx_hotels_featured ON hotels(is_featured, city);

-- Reservation queries
CREATE INDEX idx_reservations_user_id_status ON reservations(user_id, status);
CREATE INDEX idx_reservations_room_id_status ON reservations(room_id, status);
CREATE INDEX idx_reservations_check_in_date ON reservations(check_in_date);

-- Availability range queries
CREATE INDEX idx_availability_room_date_range 
    ON availability(room_id, date_range_start, date_range_end);
```

### 3. Connection Pool Tuning

#### HikariCP Configuration
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50      # Production
      minimum-idle: 10
      connection-timeout: 30000   # 30 seconds
      idle-timeout: 600000        # 10 minutes
      max-lifetime: 1800000       # 30 minutes
      auto-commit: false
```

#### Monitoring Pool Health
```bash
# Check active connections
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active

# Check waiting threads
curl http://localhost:8080/actuator/metrics/hikaricp.connections.await
```

### 4. API Response Compression

#### GZIP Configuration
```yaml
server:
  compression:
    enabled: true
    min-response-size: 1024  # Compress responses > 1KB
    
spring:
  http:
    encoding:
      charset: UTF-8
```

#### Test Compression
```bash
# With compression
curl -H "Accept-Encoding: gzip" http://localhost:8080/api/hotels \
  --compressed -w "Size: %{size_download}\n"

# Without compression
curl http://localhost:8080/api/hotels -w "Size: %{size_download}\n"
```

### 5. Query Performance Analysis

#### Explain Plans
```sql
-- PostgreSQL: Show execution plan
EXPLAIN ANALYZE
SELECT h.id, h.name, COUNT(r.id) as reservation_count
FROM hotels h
LEFT JOIN reservations r ON h.id = r.hotel_id
GROUP BY h.id, h.name
ORDER BY reservation_count DESC;
```

#### Slow Query Logging
```yaml
# application-prod.yml
spring:
  jpa:
    properties:
      hibernate:
        generate_statistics: true
        use_sql_comments: true
        
logging:
  level:
    org.hibernate.stat: DEBUG
```

### 6. Monitoring & Metrics

#### Key Metrics
```bash
# API request rate
curl http://localhost:9090/api/v1/query?query=rate(http_requests_total[5m])

# Response time (p95)
curl http://localhost:9090/api/v1/query?query=histogram_quantile(0.95,rate(http_request_duration_seconds_bucket[5m]))

# Cache hit rate
curl http://localhost:9090/api/v1/query?query=cache_hits_total/(cache_hits_total+cache_misses_total)

# Database connection pool
curl http://localhost:8080/actuator/metrics/hikaricp.connections
```

#### Prometheus Alerts
```yaml
groups:
  - name: hotresvib
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
        for: 5m
        annotations:
          summary: "High error rate detected"
          
      - alert: SlowResponse
        expr: histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])) > 0.5
        for: 5m
        annotations:
          summary: "p95 response time > 500ms"
```

### 7. Load Testing Results

#### JMeter Test Scenarios

**Scenario 1: Hotel Search**
```
- Concurrent Users: 100
- Ramp-up: 30 seconds
- Duration: 5 minutes
- Response Time (p95): 150ms
- Throughput: 450 req/sec
- Error Rate: 0.1%
```

**Scenario 2: Concurrent Bookings**
```
- Concurrent Users: 50
- Ramp-up: 15 seconds
- Duration: 5 minutes
- Response Time (p95): 480ms
- Throughput: 180 req/sec
- Error Rate: 0.05%
```

**Scenario 3: Sustained Load**
```
- Target Throughput: 500 req/sec
- Duration: 10 minutes
- Response Time (p95): 400ms
- Response Time (p99): 750ms
- Error Rate: 0.02%
```

**Scenario 4: Spike Test**
```
- Normal Load: 100 req/sec
- Spike to: 1000 req/sec
- Spike Duration: 60 seconds
- Degradation: < 20%
- Recovery Time: < 2 minutes
```

### 8. Optimization Checklist

- [ ] Redis caching enabled for hotel/room/availability queries
- [ ] All major queries have database indexes
- [ ] N+1 queries eliminated (use JOIN FETCH)
- [ ] API responses compressed with GZIP
- [ ] Connection pool tuned (minimum-idle: 10, maximum: 50)
- [ ] Slow query logging enabled
- [ ] Prometheus metrics exported
- [ ] Grafana dashboards created
- [ ] Load testing completed (500 req/sec target)
- [ ] Cache hit rate > 80%
- [ ] p95 response time < 500ms
- [ ] Database queries < 50ms (p95)

### 9. Performance Regression Testing

```kotlin
// Automated performance test
@Test
fun `hotel search should return in under 200ms`() {
    val startTime = System.currentTimeMillis()
    
    val hotels = hotelService.searchHotels(
        city = "Jakarta",
        checkIn = LocalDate.now().plusDays(7),
        checkOut = LocalDate.now().plusDays(14)
    )
    
    val duration = System.currentTimeMillis() - startTime
    
    assertThat(duration).isLessThan(200)
    assertThat(hotels).isNotEmpty()
}
```

### 10. Production Performance Tuning

#### JVM Tuning
```bash
export JAVA_OPTS="
  -Xms512m -Xmx2g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+ParallelRefProcEnabled
  -XX:+UnlockExperimentalVMOptions
  -XX:G1NewCollectionPercentPercent=30
"
```

#### PostgreSQL Tuning
```sql
-- For 4GB server
ALTER SYSTEM SET shared_buffers = '1GB';
ALTER SYSTEM SET effective_cache_size = '3GB';
ALTER SYSTEM SET work_mem = '256MB';
ALTER SYSTEM SET maintenance_work_mem = '256MB';
ALTER SYSTEM SET random_page_cost = 1.1;

SELECT pg_reload_conf();
```

## Performance Dashboard

Example Grafana dashboard panels:

1. **Request Rate**: Requests per second (5-minute rate)
2. **Response Time**: p50, p95, p99 latencies
3. **Error Rate**: 4xx and 5xx errors per second
4. **Cache Hit Rate**: Redis cache hit percentage
5. **Database Connections**: Active, idle, waiting
6. **JVM Heap Usage**: Used/max memory
7. **Throughput by Endpoint**: Top 10 endpoints by req/sec
8. **Reservation Success Rate**: Bookings completed vs. started

## Troubleshooting Performance Issues

### High Memory Usage
```bash
# Check heap usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Increase JVM memory
export JAVA_OPTS="-Xms1g -Xmx3g"

# Force garbage collection
jmap -histo:live <pid> | head -20
```

### Slow Queries
```bash
# Enable query logging
SET log_min_duration_statement = 100;  -- Log queries > 100ms

# Check slow query log
tail -f /var/log/postgresql/postgresql.log | grep duration
```

### Cache Invalidation Issues
```bash
# Clear entire cache
curl -X DELETE http://localhost:8080/actuator/caches

# Clear specific cache
curl -X DELETE http://localhost:8080/actuator/caches/hotels
```

## Further Reading

- [Spring Data JPA Performance Tuning](https://spring.io/guides/tutorials/rest/)
- [PostgreSQL Optimization Guide](https://www.postgresql.org/docs/current/performance.html)
- [Redis Caching Patterns](https://redis.io/topics/patterns)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP/wiki/Configuration)
- [Prometheus Best Practices](https://prometheus.io/docs/practices/instrumentation/)
