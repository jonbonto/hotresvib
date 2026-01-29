# 📋 Detailed Implementation Prompts
## HotResvib Development Phases 4-12

**Purpose**: Copy-paste ready prompts for AI assistant or development team  
**Generated**: January 29, 2026

---

## Phase 4: User Authentication & Authorization

### 🎯 Prompt for Phase 4

```
CONTEXT:
You are working on HotResvib, a hotel reservation system built with Kotlin and Spring Boot.
Phases 0-3 are complete (domain layer, services, security foundations).
Current codebase location: /workspaces/hotresvib

OBJECTIVE:
Implement complete user authentication and authorization system with registration, login, 
JWT tokens, refresh tokens, and role-based access control.

REQUIREMENTS:

1. USER REGISTRATION
   - Create POST /api/auth/register endpoint
   - Accept: email, password, displayName
   - Validate email format and uniqueness
   - Hash password with BCrypt (strength 12)
   - Create user with CUSTOMER role by default
   - Return success message (not JWT token)
   - Validation: email required, password min 8 chars, displayName required

2. USER LOGIN
   - Create POST /api/auth/login endpoint
   - Accept: email, password
   - Validate credentials against hashed password
   - Generate JWT access token (expires in 1 hour)
   - Generate refresh token (expires in 7 days)
   - Store refresh token in database
   - Return: { accessToken, refreshToken, user: { id, email, displayName, role } }

3. TOKEN REFRESH
   - Create POST /api/auth/refresh endpoint
   - Accept: refreshToken
   - Validate refresh token from database
   - Check expiration
   - Generate new access token
   - Return: { accessToken }

4. PASSWORD HASHING
   - Create PasswordHashingService interface
   - Implement BCryptPasswordHashingService (strength 12)
   - Methods: hashPassword(plainText), verifyPassword(plainText, hashedPassword)
   - Update User entity to use hashed passwords

5. REFRESH TOKEN ENTITY
   - Create RefreshToken entity
   - Fields: id (UUID), token (String), userId (UserId), expiresAt (Instant), createdAt (Instant)
   - Create RefreshTokenRepository interface
   - Create InMemoryRefreshTokenRepository implementation
   - Methods: save, findByToken, deleteByUserId, deleteExpired

6. AUTH CONTROLLER
   - Create AuthController in application/web package
   - Endpoints: /api/auth/register, /api/auth/login, /api/auth/refresh, /api/auth/logout
   - DTO classes: RegisterRequest, LoginRequest, RefreshRequest, AuthResponse, UserResponse
   - Error handling: 400 for validation, 401 for invalid credentials, 409 for duplicate email

7. USER PROFILE ENDPOINTS
   - Create GET /api/users/me (authenticated users only)
   - Create PUT /api/users/me (update displayName only)
   - Return current user details from JWT token

8. ROLE-BASED ACCESS CONTROL
   - Add @PreAuthorize annotations to existing endpoints
   - ReservationController.createReservation: CUSTOMER or ADMIN
   - HotelController.createHotel: ADMIN only
   - HotelController.createRoom: ADMIN only
   - Update SecurityConfig to enable method security

9. SECURITY IMPROVEMENTS
   - Update JwtAuthenticationFilter to extract userId from token
   - Update JwtTokenProvider to include userId and role in claims
   - Add token blacklist mechanism for logout
   - Configure CORS properly (allow localhost:3000 for frontend)

10. TESTING
   - AuthControllerTest: test all endpoints
   - BCryptPasswordHashingServiceTest: test hashing and verification
   - AuthenticationServiceTest: test login flow
   - Integration test: full registration → login → access protected endpoint
   - Edge cases: invalid email, weak password, duplicate email, expired token

FILES TO CREATE:
- src/main/kotlin/com/hotresvib/application/web/AuthController.kt
- src/main/kotlin/com/hotresvib/application/dto/AuthDtos.kt
- src/main/kotlin/com/hotresvib/application/service/PasswordHashingService.kt
- src/main/kotlin/com/hotresvib/application/service/BCryptPasswordHashingService.kt
- src/main/kotlin/com/hotresvib/domain/auth/RefreshToken.kt
- src/main/kotlin/com/hotresvib/application/port/RefreshTokenRepository.kt
- src/main/kotlin/com/hotresvib/infrastructure/persistence/inmemory/InMemoryRefreshTokenRepository.kt
- src/test/kotlin/com/hotresvib/application/web/AuthControllerTest.kt
- src/test/kotlin/com/hotresvib/application/service/BCryptPasswordHashingServiceTest.kt
- src/test/kotlin/com/hotresvib/integration/AuthenticationFlowTest.kt

FILES TO UPDATE:
- src/main/kotlin/com/hotresvib/domain/user/User.kt (add passwordHash field)
- src/main/kotlin/com/hotresvib/infrastructure/security/JwtTokenProvider.kt (add userId, role to claims)
- src/main/kotlin/com/hotresvib/infrastructure/security/JwtAuthenticationFilter.kt (extract userId from token)
- src/main/kotlin/com/hotresvib/infrastructure/config/SecurityConfig.kt (enable method security, update CORS)
- src/main/kotlin/com/hotresvib/application/web/ReservationController.kt (add @PreAuthorize)
- src/main/kotlin/com/hotresvib/application/web/HotelController.kt (add @PreAuthorize)

ACCEPTANCE CRITERIA:
✅ User can register with email, password, displayName
✅ Password is hashed with BCrypt
✅ User can login and receive JWT access token + refresh token
✅ User can refresh access token using refresh token
✅ User can logout (token blacklisted)
✅ Protected endpoints require valid JWT token
✅ Admin-only endpoints reject non-admin users
✅ All tests pass (minimum 15 new tests)
✅ Build succeeds with no warnings

CONSTRAINTS:
- Follow existing code style and architecture (Clean Architecture, Hexagonal)
- Use existing DTOs pattern
- Use existing repository pattern
- BCrypt strength must be 12
- JWT tokens use HS256 algorithm
- Access token expires in 1 hour
- Refresh token expires in 7 days
```

---

## Phase 5: Database Persistence (JPA Integration)

### 🎯 Prompt for Phase 5

```
CONTEXT:
You are working on HotResvib, a hotel reservation system built with Kotlin and Spring Boot.
Phases 0-4 are complete. Currently using in-memory repositories. 
Need to migrate to JPA/PostgreSQL persistence.

OBJECTIVE:
Add JPA annotations to all domain entities, configure relationships, and switch to 
database persistence using PostgreSQL.

REQUIREMENTS:

1. USER ENTITY JPA ANNOTATIONS
   - Add @Entity, @Table(name = "users") annotations
   - @Id @GeneratedValue on id field
   - @Column annotations: email (unique, nullable=false), passwordHash, displayName, role
   - @Enumerated(EnumType.STRING) on role field
   - @CreatedDate, @LastModifiedDate for timestamps
   - Add JPA converter for UserId value class
   - Add JPA converter for EmailAddress value object

2. HOTEL ENTITY JPA ANNOTATIONS
   - Add @Entity, @Table(name = "hotels")
   - @Id @GeneratedValue on id field
   - @Column: name (nullable=false), city, country
   - @OneToMany(mappedBy = "hotel") for rooms relationship
   - Add JPA converter for HotelId value class
   - Add JPA converter for HotelName value object

3. ROOM ENTITY JPA ANNOTATIONS
   - Add @Entity, @Table(name = "rooms")
   - @Id @GeneratedValue on id field
   - @ManyToOne @JoinColumn(name = "hotel_id") for hotel relationship
   - @Column: roomNumber (nullable=false), type, baseRateAmount, baseRateCurrency
   - @Enumerated(EnumType.STRING) on type field
   - @OneToMany for availability and pricingRules
   - Add JPA converter for RoomId, RoomNumber value classes

4. RESERVATION ENTITY JPA ANNOTATIONS
   - Add @Entity, @Table(name = "reservations")
   - @Id @GeneratedValue on id field
   - @ManyToOne @JoinColumn(name = "user_id") for user
   - @ManyToOne @JoinColumn(name = "room_id") for room
   - @Column: stayStart, stayEnd, totalAmountValue, totalAmountCurrency, status
   - @Enumerated(EnumType.STRING) on status
   - @CreatedDate for createdAt
   - Add JPA converter for ReservationId
   - Add index on (roomId, status) for performance

5. AVAILABILITY ENTITY JPA ANNOTATIONS
   - Add @Entity, @Table(name = "availability")
   - @Id @GeneratedValue on id
   - @ManyToOne @JoinColumn(name = "room_id")
   - @Column: dateRangeStart, dateRangeEnd, quantity
   - Add unique constraint on (room_id, date_range_start, date_range_end)
   - Add index on (room_id, date_range_start)

6. PRICING RULE ENTITY JPA ANNOTATIONS
   - Add @Entity, @Table(name = "pricing_rules")
   - @Id on id field (String type)
   - @ManyToOne @JoinColumn(name = "room_id")
   - @Column: dateRangeStart, dateRangeEnd, priceAmount, priceCurrency
   - Add JPA converter for PricingRuleId

7. PAYMENT ENTITY JPA ANNOTATIONS
   - Add @Entity, @Table(name = "payments")
   - @Id on id field (String/UUID)
   - @ManyToOne @JoinColumn(name = "reservation_id")
   - @Column: amount, currency, status, createdAt
   - @Enumerated(EnumType.STRING) on status
   - Add index on reservationId

8. REFRESH TOKEN ENTITY JPA ANNOTATIONS
   - Add @Entity, @Table(name = "refresh_tokens")
   - @Id @GeneratedValue on id field
   - @ManyToOne @JoinColumn(name = "user_id")
   - @Column: token (unique, nullable=false), expiresAt
   - @CreatedDate for createdAt
   - Add index on (userId, expiresAt)

9. JPA REPOSITORY ADAPTERS
   - Update all JpaAdapter classes to use JPA repositories as primary
   - Implement proper error handling (EntityNotFoundException)
   - Add pagination support where needed
   - Implement custom queries for complex searches

10. SPRING CONFIGURATION
   - Update RepositoryConfig to inject JPA adapters as @Primary
   - Configure JPA properties in application.yml
   - Set ddl-auto: validate (use Flyway for migrations)
   - Enable query logging in dev profile
   - Configure connection pool (HikariCP)

11. FLYWAY MIGRATIONS
   - Review existing V1__init.sql and V2__phase4_persistence.sql
   - Create V3__phase5_jpa_updates.sql if needed for:
     - Indexes on foreign keys
     - Unique constraints
     - Default values
   - Ensure all columns match JPA annotations

12. TESTING
   - Create JPA repository integration tests using @DataJpaTest
   - Test all CRUD operations
   - Test relationship mappings (save parent with children)
   - Test custom queries
   - Test cascading operations
   - Test optimistic locking if used
   - Update existing tests to use PostgreSQL test container

FILES TO CREATE:
- src/main/kotlin/com/hotresvib/infrastructure/persistence/jpa/converters/ValueObjectConverters.kt
- src/main/resources/db/migration/V3__phase5_jpa_updates.sql (if needed)
- src/test/kotlin/com/hotresvib/infrastructure/persistence/jpa/UserJpaRepositoryTest.kt
- src/test/kotlin/com/hotresvib/infrastructure/persistence/jpa/HotelJpaRepositoryTest.kt
- src/test/kotlin/com/hotresvib/infrastructure/persistence/jpa/ReservationJpaRepositoryTest.kt
- src/test/kotlin/com/hotresvib/integration/DatabaseIntegrationTest.kt

FILES TO UPDATE:
- src/main/kotlin/com/hotresvib/domain/user/User.kt (add JPA annotations)
- src/main/kotlin/com/hotresvib/domain/hotel/Hotel.kt (add JPA annotations)
- src/main/kotlin/com/hotresvib/domain/hotel/Room.kt (add JPA annotations)
- src/main/kotlin/com/hotresvib/domain/reservation/Reservation.kt (add JPA annotations)
- src/main/kotlin/com/hotresvib/domain/availability/Availability.kt (add JPA annotations)
- src/main/kotlin/com/hotresvib/domain/pricing/PricingRule.kt (add JPA annotations)
- src/main/kotlin/com/hotresvib/domain/payment/Payment.kt (add JPA annotations)
- src/main/kotlin/com/hotresvib/domain/auth/RefreshToken.kt (add JPA annotations)
- src/main/kotlin/com/hotresvib/infrastructure/config/RepositoryConfig.kt (make JPA adapters primary)
- src/main/resources/application.yml (add JPA config)
- build.gradle.kts (add PostgreSQL driver, Testcontainers if needed)

ACCEPTANCE CRITERIA:
✅ All domain entities have proper JPA annotations
✅ Database schema matches entity definitions
✅ All relationships properly mapped (ManyToOne, OneToMany)
✅ JPA repositories work for all entities
✅ No in-memory repositories used in production profile
✅ All indexes and constraints created
✅ Flyway migrations run successfully
✅ All existing tests pass with database
✅ 20+ new JPA integration tests pass
✅ Build succeeds with no warnings
✅ Application starts and connects to PostgreSQL

CONSTRAINTS:
- Don't break existing domain model
- Use Flyway for schema management (not ddl-auto: create)
- Use value class converters for type safety
- Follow JPA best practices (lazy loading, fetch types)
- Add indexes on foreign keys and frequently queried columns
```

---

## Phase 6: Payment Integration & Reservation Lifecycle

### 🎯 Prompt for Phase 6

```
CONTEXT:
You are working on HotResvib, a hotel reservation system built with Kotlin and Spring Boot.
Phases 0-5 complete. Need to implement full reservation lifecycle with payment integration.

OBJECTIVE:
Implement complete reservation lifecycle (DRAFT → PENDING_PAYMENT → CONFIRMED → CANCELLED/REFUNDED/EXPIRED)
and integrate with payment gateway (Stripe or mock).

REQUIREMENTS:

1. UPDATE RESERVATION STATUS ENUM
   - Add new states: DRAFT, PENDING_PAYMENT, EXPIRED, REFUNDED
   - Final states: DRAFT, PENDING_PAYMENT, CONFIRMED, CANCELLED, EXPIRED, REFUNDED
   - Update ReservationStatus enum in domain/reservation/Reservation.kt
   - Update database migration to support new states

2. RESERVATION LIFECYCLE SERVICE
   - Create ReservationLifecycleService
   - Method: createDraft(userId, roomId, stay) → Reservation (status=DRAFT)
   - Method: initiatePayment(reservationId) → PaymentIntent (status=PENDING_PAYMENT)
   - Method: confirmPayment(reservationId, paymentId) → Reservation (status=CONFIRMED)
   - Method: expireReservation(reservationId) → Reservation (status=EXPIRED)
   - Method: refundReservation(reservationId) → Reservation (status=REFUNDED)
   - Validate state transitions (e.g., can't confirm if not PENDING_PAYMENT)

3. STATE TRANSITION RULES
   - DRAFT → PENDING_PAYMENT (when payment initiated)
   - PENDING_PAYMENT → CONFIRMED (when payment succeeds)
   - PENDING_PAYMENT → EXPIRED (when timeout reached, default 30 minutes)
   - CONFIRMED → CANCELLED (user cancellation, before check-in)
   - CANCELLED → REFUNDED (after refund processed)
   - Add state validation in Reservation entity or service

4. PAYMENT GATEWAY INTEGRATION (STRIPE)
   - Add Stripe SDK dependency (com.stripe:stripe-java)
   - Create StripePaymentService implementing PaymentService
   - Method: createPaymentIntent(amount, currency, metadata) → PaymentIntent
   - Method: confirmPayment(paymentIntentId) → PaymentStatus
   - Method: refundPayment(paymentIntentId) → RefundStatus
   - Use Stripe test keys from application.yml

5. PAYMENT WEBHOOK HANDLER
   - Create POST /api/webhooks/stripe endpoint (public, no auth)
   - Verify Stripe signature
   - Handle events: payment_intent.succeeded, payment_intent.payment_failed
   - Call ReservationLifecycleService.confirmPayment on success
   - Call ReservationLifecycleService.expireReservation on failure
   - Ensure idempotency (check if already processed)
   - Log all webhook events

6. AUTOMATIC EXPIRATION MECHANISM
   - Create @Scheduled job: ReservationExpirationJob
   - Run every 5 minutes
   - Find all PENDING_PAYMENT reservations older than 30 minutes
   - Call ReservationLifecycleService.expireReservation for each
   - Restore availability when expired
   - Log expiration events

7. PAYMENT ENTITY UPDATES
   - Add paymentIntentId field (Stripe payment intent ID)
   - Add metadata field (JSON string for additional data)
   - Add idempotencyKey field (for webhook deduplication)
   - Link Payment to Reservation (already exists)

8. AVAILABILITY MANAGEMENT
   - Hold availability when reservation created (DRAFT state)
   - Keep hold during PENDING_PAYMENT
   - Confirm hold when CONFIRMED
   - Release availability when EXPIRED or CANCELLED
   - Ensure atomic operations (use @Transactional)

9. RESERVATION TIMEOUT CONFIGURATION
   - Add property: reservation.payment-timeout-minutes (default 30)
   - Add property: stripe.api-key-secret
   - Add property: stripe.webhook-secret
   - Configure in application.yml

10. PAYMENT DTO UPDATES
    - Create PaymentIntentRequest (amount, currency, reservationId)
    - Create PaymentIntentResponse (clientSecret, paymentIntentId, amount)
    - Create WebhookEventRequest (event type, payload)
    - Update PaymentController to handle new endpoints

11. REFUND LOGIC
    - Only allow refunds for CONFIRMED reservations
    - Call Stripe refund API
    - Update reservation to CANCELLED → REFUNDED
    - Restore availability if refund successful
    - Apply refund policy (full refund if > 24 hours before check-in, else 50%)

12. TESTING
    - Test all state transitions
    - Test payment intent creation
    - Test webhook handling (use Stripe test events)
    - Test expiration job
    - Test refund flow
    - Test idempotency (webhook called twice)
    - Integration test: full booking flow (draft → payment → confirmed)

FILES TO CREATE:
- src/main/kotlin/com/hotresvib/application/service/ReservationLifecycleService.kt
- src/main/kotlin/com/hotresvib/application/service/StripePaymentService.kt
- src/main/kotlin/com/hotresvib/application/web/WebhookController.kt
- src/main/kotlin/com/hotresvib/application/dto/PaymentIntentDtos.kt
- src/main/kotlin/com/hotresvib/application/job/ReservationExpirationJob.kt
- src/main/resources/db/migration/V3__add_reservation_states.sql
- src/test/kotlin/com/hotresvib/application/service/ReservationLifecycleServiceTest.kt
- src/test/kotlin/com/hotresvib/application/service/StripePaymentServiceTest.kt
- src/test/kotlin/com/hotresvib/application/web/WebhookControllerTest.kt
- src/test/kotlin/com/hotresvib/integration/PaymentFlowIntegrationTest.kt

FILES TO UPDATE:
- src/main/kotlin/com/hotresvib/domain/reservation/Reservation.kt (add new states)
- src/main/kotlin/com/hotresvib/domain/payment/Payment.kt (add paymentIntentId, metadata, idempotencyKey)
- src/main/kotlin/com/hotresvib/application/web/PaymentController.kt (add payment intent endpoint)
- src/main/kotlin/com/hotresvib/application/service/ReservationService.kt (integrate lifecycle)
- src/main/resources/application.yml (add Stripe config, timeout config)
- build.gradle.kts (add Stripe SDK dependency)

ACCEPTANCE CRITERIA:
✅ Reservation has 6 states: DRAFT, PENDING_PAYMENT, CONFIRMED, CANCELLED, EXPIRED, REFUNDED
✅ Payment intent created when booking initiated
✅ Stripe webhook confirms payment and updates reservation
✅ Reservations auto-expire after 30 minutes if unpaid
✅ Availability managed correctly through all states
✅ Refunds work correctly
✅ All state transitions validated
✅ 25+ tests pass
✅ Build succeeds
✅ Integration test: full booking flow works end-to-end

CONSTRAINTS:
- Use Stripe test mode keys only
- Ensure idempotent webhook handling
- All database operations must be transactional
- Log all state transitions for audit
- Payment timeout configurable via application.yml
```

---

## Phase 7: Search, Discovery & Filtering

### 🎯 Prompt for Phase 7

```
CONTEXT:
You are working on HotResvib, a hotel reservation system built with Kotlin and Spring Boot.
Phases 0-6 complete. Need to add search and filtering capabilities for hotels and rooms.

OBJECTIVE:
Implement comprehensive search, filtering, and discovery features for hotels and rooms
with pagination, sorting, and dynamic pricing.

REQUIREMENTS:

1. HOTEL SEARCH ENDPOINT
   - Create GET /api/search/hotels
   - Query parameters:
     * city (optional, case-insensitive partial match)
     * country (optional, case-insensitive partial match)
     * name (optional, case-insensitive partial match)
     * checkIn (optional, ISO date)
     * checkOut (optional, ISO date)
     * guests (optional, integer)
     * page (default 0)
     * size (default 20)
     * sort (default "name,asc")
   - Return: Page<HotelSearchResponse>
   - Filter hotels with available rooms if dates provided

2. ROOM SEARCH ENDPOINT
   - Create GET /api/search/rooms
   - Query parameters:
     * hotelId (optional)
     * type (optional, enum: SINGLE, DOUBLE, SUITE)
     * minPrice (optional, decimal)
     * maxPrice (optional, decimal)
     * checkIn (required if searching by availability)
     * checkOut (required if searching by availability)
     * guests (optional)
     * available (optional, boolean - filter only available rooms)
     * page (default 0)
     * size (default 20)
     * sort (default "baseRate,asc")
   - Return: Page<RoomSearchResponse>

3. AVAILABILITY-BASED SEARCH
   - Create GET /api/search/available-rooms
   - Required parameters: checkIn, checkOut
   - Optional: city, country, type, minPrice, maxPrice, guests
   - Returns only rooms with full availability for the date range
   - Include calculated total price for the stay
   - Include hotel information with each room

4. SEARCH SERVICE
   - Create HotelSearchService
   - Method: searchHotels(criteria: HotelSearchCriteria) → Page<Hotel>
   - Method: searchRooms(criteria: RoomSearchCriteria) → Page<Room>
   - Method: searchAvailableRooms(criteria: AvailabilitySearchCriteria) → Page<RoomAvailability>
   - Use JPA Criteria API or QueryDSL for dynamic queries
   - Implement efficient queries (avoid N+1 problem)

5. PRICE CALCULATION SERVICE
   - Create PriceCalculationService
   - Method: calculateTotalPrice(roomId, checkIn, checkOut) → Money
   - Apply pricing rules by date
   - Handle weekday vs weekend pricing
   - Apply seasonal pricing
   - Return breakdown: basePrice, pricingRules, total

6. REPOSITORY QUERY METHODS
   - Add to HotelRepository:
     * findByCityContainingIgnoreCase(city, pageable)
     * findByCountryContainingIgnoreCase(country, pageable)
     * searchByCriteria(city, country, name, pageable)
   - Add to RoomRepository:
     * findByTypeAndBaseRateBetween(type, minRate, maxRate, pageable)
     * findByHotelId(hotelId, pageable)
   - Use @Query annotations for complex queries

7. FEATURED HOTELS
   - Create GET /api/hotels/featured
   - Return top 10 hotels by criteria (configurable)
   - Criteria: most bookings, highest rating (future), manually featured
   - Add isFeatured boolean field to Hotel entity
   - Allow admins to mark hotels as featured

8. SEARCH DTO CLASSES
   - Create HotelSearchCriteria (data class with all search parameters)
   - Create RoomSearchCriteria
   - Create AvailabilitySearchCriteria
   - Create HotelSearchResponse (includes room count, min price)
   - Create RoomSearchResponse (includes hotel info, availability status)
   - Create RoomAvailability (room + hotel + calculated price)

9. PAGINATION & SORTING
   - Use Spring Data Page and Pageable
   - Support multiple sort fields (e.g., price,asc&name,desc)
   - Return metadata: totalElements, totalPages, currentPage, size
   - Validate page and size parameters (max size = 100)

10. SEARCH FILTERS VALIDATION
    - Validate date range (checkOut > checkIn)
    - Validate dates not in past
    - Validate price range (maxPrice >= minPrice)
    - Validate guests > 0
    - Return 400 Bad Request for invalid parameters

11. AUTOCOMPLETE ENDPOINT
    - Create GET /api/search/autocomplete
    - Parameter: query (min 2 characters)
    - Return: List of hotel names and cities matching query
    - Limit results to 10
    - Use case-insensitive search

12. TESTING
    - Test hotel search with various criteria
    - Test room search with filters
    - Test availability-based search
    - Test pagination and sorting
    - Test price calculation
    - Test featured hotels
    - Test autocomplete
    - Test validation errors

FILES TO CREATE:
- src/main/kotlin/com/hotresvib/application/web/SearchController.kt
- src/main/kotlin/com/hotresvib/application/service/HotelSearchService.kt
- src/main/kotlin/com/hotresvib/application/service/PriceCalculationService.kt
- src/main/kotlin/com/hotresvib/application/dto/SearchDtos.kt
- src/main/kotlin/com/hotresvib/application/dto/SearchCriteria.kt
- src/test/kotlin/com/hotresvib/application/web/SearchControllerTest.kt
- src/test/kotlin/com/hotresvib/application/service/HotelSearchServiceTest.kt
- src/test/kotlin/com/hotresvib/application/service/PriceCalculationServiceTest.kt

FILES TO UPDATE:
- src/main/kotlin/com/hotresvib/application/port/HotelRepository.kt (add search methods)
- src/main/kotlin/com/hotresvib/application/port/RoomRepository.kt (add search methods)
- src/main/kotlin/com/hotresvib/domain/hotel/Hotel.kt (add isFeatured field)
- src/main/kotlin/com/hotresvib/infrastructure/persistence/jpa/HotelJpaRepository.kt (implement queries)
- src/main/kotlin/com/hotresvib/infrastructure/persistence/jpa/RoomJpaRepository.kt (implement queries)

ACCEPTANCE CRITERIA:
✅ Hotel search works with city, country, name filters
✅ Room search works with type, price range filters
✅ Availability-based search returns only available rooms
✅ Price calculation includes pricing rules
✅ Pagination works correctly (page, size, sort)
✅ Featured hotels endpoint works
✅ Autocomplete returns relevant suggestions
✅ All validation errors return 400 with clear messages
✅ 20+ tests pass
✅ Build succeeds
✅ Performance: search responds within 500ms

CONSTRAINTS:
- Avoid N+1 queries (use JOIN FETCH or DTOs)
- Index database columns used in search (city, country, type, baseRate)
- Validate all user input
- Maximum page size is 100
- Autocomplete minimum query length is 2 characters
```

---

*[Phases 8-12 prompts in next file due to length...]*

---

## Quick Reference

### Phase Priority Matrix
| Phase | Priority | Blocking | Effort |
|-------|----------|----------|--------|
| 4: Auth | 🔴 Critical | 5,6,7,9 | 2-3 days |
| 5: JPA | 🔴 Critical | 6,7,8,9 | 3-4 days |
| 6: Payment | 🔴 High | 9 | 4-5 days |
| 7: Search | 🟡 Medium | 9 | 3-4 days |
| 8: Email | 🟢 Low | 10 | 2-3 days |
| 9: Frontend | 🔴 High | 10 | 7-10 days |
| 10: Admin | 🟢 Low | - | 5-7 days |
| 11: Security | 🔴 High | Prod | 3-4 days |
| 12: Performance | 🟡 Medium | Prod | 3-4 days |

### Recommended Order
1. Phase 4 (Auth) - Week 1
2. Phase 5 (JPA) - Week 2
3. Phase 6 (Payment) - Week 3
4. Phase 7 (Search) - Week 4
5. Phase 9 (Frontend) - Week 5-6
6. Phase 11 (Security) - Week 7

---

**Generated**: January 29, 2026  
**Last Updated**: January 29, 2026
