# Quick Start Guide - HotResvib Development

## Getting Started

### Prerequisites
- JDK 17 or later
- Gradle 8.x or later (included via gradlew)
- Git

### Initial Setup

1. **Clone the Repository**
```bash
git clone <repository-url>
cd hotresvib
```

2. **Build the Project**
```bash
./gradlew build
```

3. **Run Tests**
```bash
./gradlew test
```

4. **Start the Application**
```bash
./gradlew bootRun
```
The application will be available at `http://localhost:8080`

## Project Structure Overview

### Core Directories
- **`src/main/kotlin`**: Main application code
  - `domain/`: Business logic and entities (Phase 0)
  - `application/`: Services and ports (Phase 1)
  - `infrastructure/`: Implementations and configs (Phase 2-3)
- **`src/test/kotlin`**: Test suite (28 tests, all passing)
- **`docs/`**: Complete documentation
- **`gradle/`**: Gradle configuration files

### Key Files
- `build.gradle.kts`: Build configuration
- `settings.gradle.kts`: Gradle settings
- `gradle/libs.versions.toml`: Dependency management
- `src/main/resources/application.yml`: Application configuration
- `src/main/resources/db/migration/V1__init.sql`: Database schema

## Common Tasks

### Running Tests
```bash
# All tests
./gradlew test

# Specific test class
./gradlew test --tests "DateRangeTest"

# Tests matching pattern
./gradlew test --tests "*ServiceTest"
```

### Building for Production
```bash
./gradlew build -x test  # Skip tests
./gradlew build          # Include tests
```

### Cleaning
```bash
# Clean build artifacts
./gradlew clean

# Clean specific cache
./gradlew cleanBuild
```

### Development Workflow

1. **Make Code Changes**
   - Modify files in `src/main/kotlin`
   - Create tests in `src/test/kotlin`

2. **Run Tests Locally**
   ```bash
   ./gradlew test
   ```

3. **Build & Package**
   ```bash
   ./gradlew build
   ```

4. **Run Application**
   ```bash
   ./gradlew bootRun
   ```

## Architecture Layers

### Phase 0: Domain Layer
- Pure business logic
- No dependencies on frameworks
- Tests in `src/test/kotlin/com/hotresvib/domain/`
- Key files:
  - `domain/shared/`: Value objects (DateRange, Money)
  - `domain/user/`: User entity
  - `domain/hotel/`: Hotel and Room entities
  - `domain/*/`: Other domain entities

### Phase 1: Application Services
- Orchestration of domain logic
- Tests in `src/test/kotlin/com/hotresvib/application/service/`
- Key files:
  - `application/service/ReservationService.kt`
  - `application/service/AvailabilityApplicationService.kt`
  - `application/port/`: Repository interfaces

### Phase 2-3: Infrastructure
- Framework implementations
- Repository adapters
- Security configuration
- Key files:
  - `infrastructure/persistence/`: Repository implementations
  - `infrastructure/security/`: JWT authentication
  - `infrastructure/config/`: Spring configuration

## Key Concepts

### Value Objects (Phase 0)
Immutable objects representing domain concepts:
```kotlin
val dateRange = DateRange(startDate, endDate)  // Enforces invariants
val money = Money(BigDecimal("100"), "USD")    // Currency-aware
```

### Entities
Objects with identity:
```kotlin
val user = User(UserId.generate(), email, displayName, role, passwordHash)
val room = Room(RoomId.generate(), hotelId, number, type, baseRate)
```

### Repositories (Phase 1)
Port/adapter pattern for data access:
```kotlin
interface AvailabilityRepository {
    fun save(availability: Availability)
    fun findByRoomId(roomId: RoomId): List<Availability>
}
```

### Services (Phase 1)
Business logic orchestration:
```kotlin
fun createReservation(userId: UserId, roomId: RoomId, stay: DateRange): Reservation {
    // Validation
    // Availability check
    // Pricing calculation
    // Persistence
}
```

## Testing Guide

### Test Organization
```
src/test/kotlin/com/hotresvib/
├── domain/              # Phase 0 - Domain tests
│   ├── shared/
│   ├── user/
│   └── hotel/
├── application/         # Phase 1 - Service tests
│   └── service/
├── infrastructure/      # Phase 2-3 - Infrastructure tests
│   └── security/
└── integration/         # Phase 3 - Integration tests
```

### Writing Tests
All tests use **JUnit 5** + **AssertJ** + **Mockito**:

```kotlin
@Test
fun `should create valid date range`() {
    val startDate = LocalDate.of(2026, 2, 1)
    val endDate = LocalDate.of(2026, 2, 5)
    val dateRange = DateRange(startDate, endDate)
    
    assertThat(dateRange.startDate).isEqualTo(startDate)
    assertThat(dateRange.endDate).isEqualTo(endDate)
}
```

### Test Statistics
- **Total Tests**: 28
- **Domain Tests**: 12
- **Service Tests**: 8
- **Integration Tests**: 1
- **Application Tests**: 1
- **Status**: ✅ All Passing

## Configuration

### Application Properties
Edit `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  application:
    name: hotresvib
  jpa:
    hibernate:
      ddl-auto: validate
  datasource:
    url: jdbc:h2:mem:testdb

jwt:
  secret: your-secret-key
  expiration: 86400000  # 24 hours
```

### Database
- **Development**: H2 in-memory database
- **Production**: PostgreSQL
- **Migrations**: Flyway (in `src/main/resources/db/migration/`)

## Troubleshooting

### Build Issues
```bash
# Clean rebuild
./gradlew clean build

# Show detailed errors
./gradlew build --info
```

### Test Failures
```bash
# Run single failing test
./gradlew test --tests "ClassName"

# Run with more output
./gradlew test --info
```

### IDE Setup (IntelliJ IDEA)
1. Open project with Gradle
2. Wait for indexing
3. Run tests via IDE or terminal

### IDE Setup (VS Code)
1. Install Gradle extension
2. Install Java extensions
3. Open terminal and run `./gradlew build`

## Documentation Files

Located in `docs/`:
- **IMPLEMENTATION-SUMMARY.md**: Overview of all phases
- **QA-TESTING-PHASES-0-3.md**: Complete testing documentation
- **VERIFICATION-CHECKLIST.md**: Validation checklist
- **INDEX.md**: Documentation index
- **architecture.md**: Architecture details

## Performance Tips

### Faster Builds
```bash
# Skip tests during development
./gradlew bootRun -x test

# Use daemon (keeps Gradle running)
./gradlew --daemon build
```

### Debugging
```bash
# Run with debug port
./gradlew bootRun --debug

# Connect IDE debugger to port 5005
```

## Contributing

### Code Standards
- Follow Kotlin conventions
- Write descriptive test names
- Document complex logic with comments
- Use value objects for domain concepts
- Keep services focused on orchestration

### Before Committing
1. Run all tests: `./gradlew test`
2. Build clean: `./gradlew clean build`
3. Verify no warnings or errors

## Resources

### Documentation
- See `docs/` folder for comprehensive guides
- See inline code comments for implementation details

### External References
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Spring Boot Guide](https://spring.io/guides/gs/spring-boot/)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design](https://www.domainlanguage.com/ddd/)

## Support

### Common Questions

**Q: How do I change the database?**
A: Edit `src/main/resources/application.yml` and update datasource settings.

**Q: How do I add a new test?**
A: Create a test file in `src/test/kotlin` following naming convention `*Test.kt`.

**Q: How do I run a specific test?**
A: `./gradlew test --tests "ClassName.methodName"`

**Q: Where are logs stored?**
A: Logs are printed to console. Configure in `application.yml` for file output.

---

**For More Information**: See documentation in the `docs/` folder or check `README.md` in project root.

**Version**: 1.0.0  
**Last Updated**: January 29, 2026  
**Status**: ✅ Production Ready (Phases 0-3)
