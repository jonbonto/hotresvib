# Test Suite Structure - Hotel Reservation System

## Overview

Comprehensive test suite for Phases 0-3 with:
- **Unit Tests**: Domain entities, value objects, services
- **Integration Tests**: End-to-end flows, database interaction
- **Controller Tests**: HTTP endpoints, request/response handling
- **Architecture Tests**: Layer structure validation

## Directory Structure

```
src/test/kotlin/com/hotresvib/
├── ArchitectureTest.kt                    # Phase 0: Structure validation
├── domain/
│   ├── shared/
│   │   ├── DateRangeTest.kt              # Phase 1: DateRange value object
│   │   └── MoneyTest.kt                  # Phase 1: Money value object
│   ├── user/
│   │   └── UserTest.kt                   # Phase 1: User entity
│   └── hotel/
│       └── HotelTest.kt                  # Phase 1: Hotel & Room entities
├── application/
│   ├── service/
│   │   ├── AvailabilityApplicationServiceTest.kt    # Phase 2: Availability logic
│   │   └── ReservationApplicationServiceTest.kt     # Phase 2: Reservation creation
│   ├── security/
│   │   └── AuthenticationServiceTest.kt  # Phase 3: Authentication
│   └── web/
│       ├── ReservationControllerTest.kt  # Phase 2: REST endpoints
│       └── AvailabilityControllerTest.kt # Phase 2: REST endpoints
├── infrastructure/
│   └── security/
│       └── JwtTokenProviderTest.kt       # Phase 3: Token generation
└── integration/
    ├── ReservationFlowIntegrationTest.kt # Phase 2: Booking flow
    └── AuthenticationIntegrationTest.kt  # Phase 3: Auth flow
```

## Test Categories

### Phase 0: Architecture & Domain Modeling
- `ArchitectureTest.kt`: Validates layer structure, package organization, class presence

### Phase 1: Backend Domain
**Unit Tests:**
- `DateRangeTest.kt`: Value object with date calculations
- `MoneyTest.kt`: Value object with currency handling
- `UserTest.kt`: User entity with validation
- `HotelTest.kt`: Hotel and Room entities

**Design Validation:**
- Value objects are immutable
- Entities have proper validation
- No domain logic in infrastructure layer

### Phase 2: Reservation & Availability
**Unit Tests:**
- `AvailabilityApplicationServiceTest.kt`: Availability checking with Mockito
- `ReservationApplicationServiceTest.kt`: Reservation creation and lookup

**Controller Tests:**
- `ReservationControllerTest.kt`: HTTP POST/GET endpoints
- `AvailabilityControllerTest.kt`: HTTP GET with date parameters

**Integration Tests:**
- `ReservationFlowIntegrationTest.kt`: End-to-end booking
- Tests overbooking prevention
- Tests concurrent reservation handling

### Phase 3: Authentication & Authorization
**Unit Tests:**
- `AuthenticationServiceTest.kt`: Credential validation
- `JwtTokenProviderTest.kt`: Token generation and validation

**Integration Tests:**
- `AuthenticationIntegrationTest.kt`: Full login flow
- Tests JWT token issuance
- Tests error responses

## Running Tests

### All Tests
```bash
export JAVA_HOME=/workspaces/hotresvib/.jdks
export PATH=$JAVA_HOME/bin:$PATH
./gradlew test --no-daemon
```

### Specific Test Class
```bash
./gradlew test --tests "com.hotresvib.ArchitectureTest" --no-daemon
```

### Specific Test Method
```bash
./gradlew test --tests "com.hotresvib.ArchitectureTest.should have proper package structure" --no-daemon
```

### With Code Coverage (Jacoco)
```bash
./gradlew jacocoTestReport --no-daemon
# Report: build/reports/jacoco/test/html/index.html
```

### Watch Mode (Run tests on file change)
```bash
./gradlew test --watch --no-daemon
```

## Test Dependencies

```gradle
testImplementation(libs.spring.boot.starter.test)
testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
testImplementation("org.mockito:mockito-inline:5.2.0")
testRuntimeOnly("org.junit.platform:junit-platform-launcher")
```

## Mocking Strategy

### Service Layer Tests
- Mock repositories using Mockito
- Test service business logic in isolation
- Example: `AvailabilityApplicationServiceTest`

### Controller Tests
- Use `@WebMvcTest` for isolated HTTP layer testing
- Mock service layer
- Test HTTP status codes and JSON responses

### Integration Tests
- Use `@SpringBootTest` for full context
- Use real database (or test database)
- Test end-to-end flows

## Key Test Patterns

### 1. Unit Test with Mocks
```kotlin
@BeforeEach
fun setup() {
    repository = mock(Repository::class.java)
    service = Service(repository)
}

@Test
fun `should do something`() {
    `when`(repository.find(id)).thenReturn(entity)
    val result = service.doSomething(id)
    assertEquals(expected, result)
    verify(repository).find(id)
}
```

### 2. Controller Test
```kotlin
@WebMvcTest(MyController::class)
class MyControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Test
    fun `should return 200`() {
        mockMvc.perform(get("/api/endpoint"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.field").value("expected"))
    }
}
```

### 3. Integration Test
```kotlin
@SpringBootTest
@Transactional
class MyIntegrationTest {
    @Autowired
    private lateinit var service: Service
    
    @Test
    fun `should work end-to-end`() {
        val result = service.createEntity()
        assertNotNull(result)
    }
}
```

## Test Data Builders (Recommended for Phase 4+)

```kotlin
object TestDataBuilder {
    fun reservation(
        id: UUID = UUID.randomUUID(),
        status: String = "PENDING"
    ) = Reservation(id, ..., status)
}
```

## Continuous Integration

### GitHub Actions Example
```yaml
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - run: ./gradlew test
```

## Performance Benchmarks (Target)

| Category | Target | Status |
|----------|--------|--------|
| Unit Test Execution | < 5s | ✅ |
| Integration Test | < 30s | ✅ |
| Full Suite | < 2min | ✅ |

## Debugging Tests

### Run Single Test with Output
```bash
./gradlew test --tests "TestClass" -i
```

### Enable SQL Logging (in application-test.yml)
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

### Use IDE Debugger
- Right-click test class/method → Debug
- Set breakpoints
- Inspect variables

## Next Steps for QA Team

1. **Phase 4 Tests**: Payment flow, reservation state machine
2. **Phase 5 Tests**: Admin/staff operations, role-based access
3. **Phase 6 Tests**: Frontend API contract tests
4. **Phase 7 Tests**: Dashboard and reporting
5. **Phase 8 Tests**: Edge cases, concurrency, security

## Questions?

Refer to:
- Test Report: `QA_TEST_REPORT_PHASE_0_3.md`
- Domain Models: `src/main/kotlin/com/hotresvib/domain/`
- Service Layer: `src/main/kotlin/com/hotresvib/application/service/`
