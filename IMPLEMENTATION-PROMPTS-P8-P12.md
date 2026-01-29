# 📋 Detailed Implementation Prompts (Continued)
## HotResvib Development Phases 8-12

**Purpose**: Copy-paste ready prompts for AI assistant or development team  
**Generated**: January 29, 2026

---

## Phase 8: Email Notifications & Communication

### 🎯 Prompt for Phase 8

```
CONTEXT:
You are working on HotResvib, a hotel reservation system built with Kotlin and Spring Boot.
Phases 0-7 complete. Need to implement real email notifications for booking lifecycle events.

OBJECTIVE:
Replace placeholder EmailService with real email sending using SendGrid (or AWS SES/SMTP)
and implement HTML email templates for all booking events.

REQUIREMENTS:

1. EMAIL SERVICE INTEGRATION (SENDGRID)
   - Add SendGrid SDK dependency (com.sendgrid:sendgrid-java)
   - Create SendGridEmailService implementing EmailService
   - Method: sendBookingConfirmation(user, reservation, hotel, room)
   - Method: sendCancellationNotification(user, reservation)
   - Method: sendPaymentReceipt(user, reservation, payment)
   - Method: sendCheckInReminder(user, reservation, hotel)
   - Method: sendPasswordReset(user, resetLink)
   - Use SendGrid API key from application.yml

2. EMAIL TEMPLATES (HTML)
   - Create Thymeleaf templates for emails
   - Template: booking-confirmation.html
     * Include: reservation details, hotel/room info, check-in/check-out dates, total price
     * Include: cancellation policy, hotel contact info
     * Include: "View Booking" button linking to frontend
   - Template: cancellation-notification.html
     * Include: cancelled reservation details, refund information
   - Template: payment-receipt.html
     * Include: payment details, invoice number, billing info
   - Template: check-in-reminder.html
     * Include: reminder for upcoming check-in (24 hours before)
   - Template: password-reset.html
     * Include: reset link (expires in 1 hour)
   - Use consistent branding, logo, colors

3. TEMPLATE ENGINE CONFIGURATION
   - Add Thymeleaf dependency for email templates
   - Configure ThymeleafTemplateEngine for emails
   - Create EmailTemplateService to render templates
   - Method: renderTemplate(templateName, context) → HTML string
   - Support variable substitution (user name, hotel name, dates, etc.)

4. ASYNC EMAIL SENDING
   - Use @Async for non-blocking email sending
   - Configure async thread pool (min 5, max 20 threads)
   - Enable @EnableAsync in configuration
   - Handle exceptions gracefully (log errors, don't fail transaction)
   - Add retry mechanism (max 3 retries with exponential backoff)

5. EMAIL EVENTS
   - Create EmailEvent data class (to, subject, templateName, context)
   - Publish email events using Spring ApplicationEventPublisher
   - Create EmailEventListener to listen and send emails
   - Decouple email sending from business logic
   - Events: BookingConfirmedEvent, BookingCancelledEvent, PaymentReceivedEvent, etc.

6. EMAIL CONFIGURATION
   - Add properties: email.sendgrid.api-key, email.sendgrid.from-email, email.sendgrid.from-name
   - Add property: email.enabled (false in test, true in prod)
   - Add property: email.async.enabled (true)
   - Add property: email.retry.max-attempts (3)
   - Configure in application.yml

7. CHECK-IN REMINDER SCHEDULER
   - Create @Scheduled job: CheckInReminderJob
   - Run daily at 9 AM
   - Find all CONFIRMED reservations with check-in tomorrow
   - Send check-in reminder emails
   - Mark as reminder sent (add reminderSent boolean to Reservation)

8. EMAIL AUDIT LOG
   - Create EmailLog entity (id, recipientEmail, subject, templateName, sentAt, status, errorMessage)
   - Create EmailLogRepository
   - Log all email send attempts (success and failure)
   - Method: logEmailSent(to, subject, templateName, status, error)

9. FALLBACK MECHANISM
   - If SendGrid fails, fallback to SMTP (configurable)
   - Create SmtpEmailService as fallback
   - Configure SMTP settings in application.yml
   - Use circuit breaker pattern (resilience4j)

10. EMAIL TESTING
    - Use SendGrid sandbox mode for testing
    - Create mock EmailService for unit tests
    - Verify template rendering without sending
    - Test async sending
    - Test retry logic
    - Integration test: trigger booking → verify email sent

11. UNSUBSCRIBE MECHANISM
    - Add unsubscribeToken to User entity
    - Include unsubscribe link in all emails
    - Create GET /api/email/unsubscribe/{token} endpoint
    - Update user preferences to disable marketing emails
    - Still send transactional emails (booking confirmations)

12. LOCALIZATION (OPTIONAL)
    - Support multiple languages (en, id)
    - Create templates for each language
    - Use user's preferred language from profile
    - Default to English if language not supported

FILES TO CREATE:
- src/main/kotlin/com/hotresvib/application/notification/SendGridEmailService.kt
- src/main/kotlin/com/hotresvib/application/notification/EmailTemplateService.kt
- src/main/kotlin/com/hotresvib/application/notification/EmailEventListener.kt
- src/main/kotlin/com/hotresvib/application/event/EmailEvents.kt
- src/main/kotlin/com/hotresvib/application/job/CheckInReminderJob.kt
- src/main/kotlin/com/hotresvib/domain/notification/EmailLog.kt
- src/main/kotlin/com/hotresvib/application/port/EmailLogRepository.kt
- src/main/kotlin/com/hotresvib/infrastructure/persistence/jpa/EmailLogJpaRepository.kt
- src/main/resources/templates/email/booking-confirmation.html
- src/main/resources/templates/email/cancellation-notification.html
- src/main/resources/templates/email/payment-receipt.html
- src/main/resources/templates/email/check-in-reminder.html
- src/main/resources/templates/email/password-reset.html
- src/test/kotlin/com/hotresvib/application/notification/SendGridEmailServiceTest.kt
- src/test/kotlin/com/hotresvib/application/notification/EmailTemplateServiceTest.kt

FILES TO UPDATE:
- src/main/kotlin/com/hotresvib/application/service/ReservationLifecycleService.kt (publish email events)
- src/main/kotlin/com/hotresvib/application/web/AuthController.kt (send welcome email on registration)
- src/main/kotlin/com/hotresvib/domain/reservation/Reservation.kt (add reminderSent field)
- src/main/resources/application.yml (add email config)
- build.gradle.kts (add SendGrid, Thymeleaf dependencies)

ACCEPTANCE CRITERIA:
✅ Booking confirmation email sent on reservation confirmed
✅ Cancellation email sent on reservation cancelled
✅ Payment receipt sent on payment confirmed
✅ Check-in reminder sent 24 hours before check-in
✅ All emails use HTML templates with branding
✅ Emails sent asynchronously (don't block requests)
✅ Failed emails retried up to 3 times
✅ All email sends logged in database
✅ Unsubscribe mechanism works
✅ 15+ tests pass
✅ Build succeeds

CONSTRAINTS:
- Use SendGrid for production
- Fall back to SMTP if SendGrid fails
- All transactional emails must be sent even if user unsubscribed
- Email sending must not block HTTP requests
- Retry failed emails with exponential backoff
```

---

## Phase 9: Frontend - Customer Booking Interface (Next.js)

### 🎯 Prompt for Phase 9

```
CONTEXT:
You are working on HotResvib, a hotel reservation system. Backend (Spring Boot) is complete.
Need to create customer-facing web application for hotel search and booking.

OBJECTIVE:
Build a complete Next.js 14+ App Router application for customers to search hotels,
view rooms, make bookings, and manage their reservations.

REQUIREMENTS:

1. PROJECT SETUP
   - Initialize Next.js 14+ with App Router
   - TypeScript configuration
   - Install: React 18, TailwindCSS, shadcn/ui, React Query, Zod, React Hook Form
   - Folder structure: app/, components/, lib/, types/, hooks/
   - Configure API base URL (environment variable)

2. HOME PAGE (/)
   - Hero section with search form
   - Search inputs: City, Check-in date, Check-out date, Guests
   - Featured hotels grid (3-4 hotels)
   - Popular destinations section
   - Responsive design (mobile-first)
   - Server components for initial data

3. HOTEL SEARCH RESULTS (/hotels)
   - Search query parameters: city, checkIn, checkOut, guests
   - Hotel cards grid (image, name, location, price from)
   - Filters sidebar: Price range, Star rating (future), Amenities (future)
   - Sorting: Price (low-high), Name (A-Z)
   - Pagination (20 hotels per page)
   - Client component for filters/sorting
   - Server component for data fetching

4. HOTEL DETAILS PAGE (/hotels/[id])
   - Hotel information (name, location, description)
   - Image gallery (carousel or grid)
   - Available rooms list with pricing
   - Each room: type, price, capacity, amenities
   - "Book Now" button for each room
   - Server component for SEO

5. ROOM DETAILS & BOOKING PAGE (/hotels/[hotelId]/rooms/[roomId])
   - Room details (type, description, amenities, images)
   - Booking form:
     * Check-in date (date picker)
     * Check-out date (date picker)
     * Number of guests (select)
   - Price calculation (show per night and total)
   - "Proceed to Payment" button
   - Real-time availability check
   - Show pricing breakdown (base price, taxes, total)

6. PAYMENT PAGE (/booking/payment)
   - Review booking summary
   - Stripe payment element integration
   - Guest information form (if not logged in)
   - Payment form (Stripe Elements)
   - Terms and conditions checkbox
   - "Confirm and Pay" button
   - Loading states during payment
   - Handle payment errors

7. BOOKING CONFIRMATION PAGE (/booking/confirmation)
   - Show after successful payment
   - Booking reference number
   - Hotel and room details
   - Check-in/check-out dates
   - Total paid
   - Download PDF receipt button (optional)
   - "View My Bookings" link

8. USER AUTHENTICATION PAGES
   - /auth/login: Login form (email, password)
   - /auth/register: Registration form (email, password, name)
   - JWT token storage (httpOnly cookie or localStorage)
   - Redirect to original page after login
   - Protected routes (dashboard, profile)

9. USER DASHBOARD (/dashboard)
   - Authentication required
   - Upcoming bookings list
   - Past bookings list
   - Booking details modal
   - Cancel booking button (with confirmation)
   - Filter: All, Upcoming, Past, Cancelled

10. USER PROFILE PAGE (/profile)
    - Display user info (name, email)
    - Edit profile form
    - Change password form
    - Email preferences (opt-in/out)

11. API CLIENT LIBRARY
    - Create lib/api/ folder
    - API functions:
      * searchHotels(criteria)
      * getHotel(id)
      * searchRooms(criteria)
      * checkAvailability(roomId, checkIn, checkOut)
      * createReservation(data)
      * getReservations(userId)
      * cancelReservation(id)
      * login(email, password)
      * register(data)
    - Use fetch with TypeScript types
    - Error handling (401, 404, 500)
    - Loading states

12. AUTHENTICATION CONTEXT
    - Create AuthContext with provider
    - State: user, isAuthenticated, loading
    - Methods: login(), logout(), register(), refreshToken()
    - Persist token in localStorage/cookie
    - Auto-refresh token before expiry
    - Wrap app with AuthProvider

13. FORM VALIDATION
    - Use Zod for schema validation
    - Use React Hook Form for form state
    - Validation: email format, password strength, required fields
    - Show error messages below inputs
    - Disable submit button if form invalid

14. ERROR HANDLING
    - Create error boundary component
    - Create error pages: 404, 500
    - Toast notifications for API errors
    - Retry mechanism for failed requests

15. LOADING STATES
    - Skeleton loaders for pages
    - Spinner for buttons during submit
    - Progress bar for page navigation
    - Optimistic UI updates

16. RESPONSIVE DESIGN
    - Mobile-first approach
    - Breakpoints: mobile (< 640px), tablet (640-1024px), desktop (> 1024px)
    - Hamburger menu for mobile navigation
    - Touch-friendly UI elements

17. ACCESSIBILITY
    - Semantic HTML
    - ARIA labels
    - Keyboard navigation
    - Focus states
    - Alt text for images

18. PERFORMANCE
    - Image optimization (Next.js Image component)
    - Code splitting (dynamic imports)
    - Caching (React Query)
    - Prefetching (Next.js Link prefetch)

FILES TO CREATE:
- app/page.tsx (home page)
- app/hotels/page.tsx (search results)
- app/hotels/[id]/page.tsx (hotel details)
- app/hotels/[hotelId]/rooms/[roomId]/page.tsx (room booking)
- app/booking/payment/page.tsx (payment)
- app/booking/confirmation/page.tsx (confirmation)
- app/auth/login/page.tsx (login)
- app/auth/register/page.tsx (register)
- app/dashboard/page.tsx (user dashboard)
- app/profile/page.tsx (user profile)
- app/layout.tsx (root layout with nav, footer)
- components/HotelCard.tsx
- components/RoomCard.tsx
- components/SearchForm.tsx
- components/BookingForm.tsx
- components/PaymentForm.tsx (Stripe Elements)
- components/Navbar.tsx
- components/Footer.tsx
- lib/api/hotels.ts
- lib/api/rooms.ts
- lib/api/reservations.ts
- lib/api/auth.ts
- lib/contexts/AuthContext.tsx
- lib/types/hotel.ts
- lib/types/reservation.ts
- lib/validations/booking.ts (Zod schemas)
- hooks/useAuth.ts
- hooks/useBooking.ts

ACCEPTANCE CRITERIA:
✅ User can search hotels by city and dates
✅ User can view hotel details and available rooms
✅ User can book a room with payment
✅ User can register and login
✅ User can view their bookings in dashboard
✅ User can cancel a booking
✅ All pages are responsive (mobile, tablet, desktop)
✅ Loading states shown during API calls
✅ Error messages displayed appropriately
✅ Authentication persists across page refreshes
✅ Payment integration works with Stripe

CONSTRAINTS:
- Use Next.js 14+ App Router (not Pages Router)
- Use Server Components by default, Client Components only when needed
- Use TypeScript for all files
- Use TailwindCSS for styling
- Use shadcn/ui for UI components
- API calls use React Query for caching
- Forms validated with Zod + React Hook Form
```

---

## Phase 10: Admin & Staff Dashboard

### 🎯 Prompt for Phase 10

```
CONTEXT:
You are working on HotResvib, a hotel reservation system. Backend and customer frontend complete.
Need to create admin dashboard for hotel management, staff operations, and analytics.

OBJECTIVE:
Build admin and staff dashboard pages within Next.js app for managing hotels, rooms,
reservations, users, and viewing reports.

REQUIREMENTS:

1. ADMIN LAYOUT & NAVIGATION
   - Create /admin layout with sidebar navigation
   - Menu items: Dashboard, Hotels, Rooms, Reservations, Users, Reports, Settings
   - Restrict access to ADMIN and STAFF roles
   - Redirect unauthorized users to login
   - Show current user info in header

2. ADMIN DASHBOARD PAGE (/admin)
   - Statistics cards: Total Hotels, Total Rooms, Active Bookings, Revenue This Month
   - Recent bookings table (last 10)
   - Occupancy chart (current week)
   - Revenue chart (last 30 days)
   - Quick actions: Add Hotel, Add Room, View Reports

3. HOTEL MANAGEMENT PAGE (/admin/hotels)
   - Table: ID, Name, City, Country, Rooms Count, Actions
   - Actions: View, Edit, Delete
   - "Add Hotel" button → modal/page
   - Search by name, city, country
   - Pagination

4. ADD/EDIT HOTEL MODAL
   - Form fields: Name, City, Country, Description, Star Rating, Amenities
   - Image upload (single main image)
   - Validation: Name required, City required
   - Submit to POST /api/admin/hotels or PUT /api/admin/hotels/{id}
   - Show success/error toast

5. ROOM MANAGEMENT PAGE (/admin/rooms)
   - Table: ID, Hotel, Room Number, Type, Base Rate, Status, Actions
   - Actions: View, Edit, Delete, Set Availability
   - "Add Room" button → modal/page
   - Filter by hotel, type, price range
   - Pagination

6. ADD/EDIT ROOM MODAL
   - Form fields: Hotel (dropdown), Room Number, Type, Base Rate, Capacity, Description, Amenities
   - Image upload (multiple images)
   - Validation: All required
   - Submit to POST /api/admin/rooms or PUT /api/admin/rooms/{id}

7. AVAILABILITY MANAGEMENT PAGE (/admin/availability)
   - Select hotel and room
   - Calendar view showing availability by date
   - Click date to set/update availability
   - Bulk update: Set availability for date range
   - Color coding: Available (green), Limited (yellow), Unavailable (red)

8. RESERVATION MANAGEMENT PAGE (/admin/reservations)
   - Table: Booking ID, Guest Name, Hotel, Room, Check-in, Check-out, Status, Total, Actions
   - Actions: View Details, Cancel, Modify (change dates)
   - Filter by: Status, Date range, Hotel
   - Search by: Guest name, Booking ID
   - Pagination
   - Export to CSV

9. USER MANAGEMENT PAGE (/admin/users)
   - Table: ID, Email, Name, Role, Registered Date, Status, Actions
   - Actions: View Details, Change Role, Suspend/Activate
   - Filter by role (Customer, Staff, Admin)
   - Search by email or name
   - Pagination

10. REPORTS PAGE (/admin/reports)
    - Revenue report (by date range, hotel, room type)
    - Occupancy report (by date range, hotel)
    - Booking trends (by month)
    - Top performing hotels
    - Export reports to PDF or CSV
    - Date range selector
    - Charts: Bar, Line, Pie

11. STAFF CHECK-IN/CHECK-OUT
    - Create /staff/checkin page
    - Search reservation by booking ID or guest name
    - Show guest details, room, dates
    - "Check In" button (mark as checked in)
    - "Check Out" button (mark as checked out)
    - Record check-in/check-out timestamps

12. PRICING RULES MANAGEMENT
    - Create /admin/pricing page
    - List pricing rules by room
    - Add pricing rule form: Room, Date Range, Price Override
    - Edit/Delete pricing rules
    - Seasonal pricing presets (holidays, weekends)

13. SETTINGS PAGE (/admin/settings)
    - System settings: Tax rate, Cancellation policy hours, Booking timeout
    - Email settings: From address, Templates
    - Payment settings: Stripe keys (masked)
    - General settings: Site name, Contact email

14. ADMIN API ENDPOINTS (BACKEND)
    - POST /api/admin/hotels (create hotel)
    - PUT /api/admin/hotels/{id} (update hotel)
    - DELETE /api/admin/hotels/{id} (delete hotel)
    - POST /api/admin/rooms (create room)
    - PUT /api/admin/rooms/{id} (update room)
    - DELETE /api/admin/rooms/{id} (delete room)
    - PUT /api/admin/reservations/{id}/cancel
    - PUT /api/admin/users/{id}/role
    - GET /api/admin/reports/revenue
    - GET /api/admin/reports/occupancy
    - All require ADMIN role

15. ROLE-BASED ACCESS
    - ADMIN: Full access to all pages
    - STAFF: Access to reservations, check-in/check-out only
    - CUSTOMER: No access to /admin
    - Implement route guards in Next.js

16. AUDIT LOG
    - Log all admin actions (create, update, delete)
    - Show audit log page: Who, What, When
    - Filter by user, action type, date

FILES TO CREATE (FRONTEND):
- app/admin/layout.tsx (admin layout with sidebar)
- app/admin/page.tsx (dashboard)
- app/admin/hotels/page.tsx (hotel list)
- app/admin/rooms/page.tsx (room list)
- app/admin/reservations/page.tsx (reservation list)
- app/admin/users/page.tsx (user list)
- app/admin/reports/page.tsx (reports)
- app/admin/availability/page.tsx (availability calendar)
- app/admin/pricing/page.tsx (pricing rules)
- app/admin/settings/page.tsx (settings)
- app/staff/checkin/page.tsx (check-in/out)
- components/admin/HotelForm.tsx
- components/admin/RoomForm.tsx
- components/admin/StatCard.tsx
- components/admin/RevenueChart.tsx
- components/admin/OccupancyChart.tsx
- lib/api/admin.ts (admin API calls)

FILES TO CREATE (BACKEND):
- src/main/kotlin/com/hotresvib/application/web/AdminController.kt
- src/main/kotlin/com/hotresvib/application/web/StaffController.kt
- src/main/kotlin/com/hotresvib/application/service/AdminService.kt
- src/main/kotlin/com/hotresvib/application/service/ReportService.kt
- src/main/kotlin/com/hotresvib/domain/audit/AuditLog.kt
- src/test/kotlin/com/hotresvib/application/web/AdminControllerTest.kt

ACCEPTANCE CRITERIA:
✅ Admin can create, edit, delete hotels
✅ Admin can create, edit, delete rooms
✅ Admin can view all reservations
✅ Admin can cancel reservations
✅ Admin can manage users and change roles
✅ Admin can view revenue and occupancy reports
✅ Staff can check-in and check-out guests
✅ Admin can set availability for rooms
✅ Admin can create pricing rules
✅ All admin actions logged in audit log
✅ Role-based access enforced (ADMIN vs STAFF)
✅ 20+ tests pass (backend + frontend)

CONSTRAINTS:
- All admin endpoints require @PreAuthorize("hasRole('ADMIN')")
- Staff endpoints require @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
- Audit all create, update, delete operations
- Validate all form inputs
- Show confirmation dialogs for destructive actions (delete)
```

---

## Phase 11: Security Hardening & Edge Cases

### 🎯 Prompt for Phase 11

```
CONTEXT:
You are working on HotResvib, a hotel reservation system. All features implemented.
Need to harden security and handle edge cases before production.

OBJECTIVE:
Implement comprehensive security measures, handle edge cases, and ensure system robustness.

REQUIREMENTS:

1. RATE LIMITING
   - Add rate limiting to all public endpoints
   - Limits:
     * /api/auth/login: 5 requests per minute per IP
     * /api/auth/register: 3 requests per hour per IP
     * /api/search/*: 100 requests per minute per user
     * /api/reservations: 10 requests per minute per user
   - Return 429 Too Many Requests with Retry-After header
   - Use bucket4j library for rate limiting

2. CSRF PROTECTION
   - Enable Spring Security CSRF for state-changing operations
   - Generate CSRF token for forms
   - Include CSRF token in all POST/PUT/DELETE requests
   - Validate CSRF token on backend

3. INPUT SANITIZATION
   - Sanitize all user inputs to prevent XSS
   - Use OWASP Java HTML Sanitizer
   - Sanitize: displayName, hotel description, room description
   - Block script tags, iframes, event handlers

4. SQL INJECTION PREVENTION
   - Ensure all queries use parameterized statements
   - Audit all @Query annotations
   - Use JPA Criteria API for dynamic queries
   - No string concatenation in queries

5. PESSIMISTIC LOCKING FOR BOOKINGS
   - Add @Lock(LockModeType.PESSIMISTIC_WRITE) to availability queries
   - Ensure no double-booking race condition
   - Test concurrent booking attempts
   - Use database transactions (@Transactional)

6. AUDIT LOGGING
   - Log all authentication attempts (success and failure)
   - Log all reservation operations (create, cancel, modify)
   - Log all admin operations (create hotel, delete user)
   - Log all payment operations
   - Include: timestamp, userId, action, IP address, user agent
   - Store logs in database (AuditLog entity)

7. SECURITY HEADERS
   - Add security headers to all responses:
     * X-Content-Type-Options: nosniff
     * X-Frame-Options: DENY
     * X-XSS-Protection: 1; mode=block
     * Content-Security-Policy: default-src 'self'
     * Strict-Transport-Security: max-age=31536000
   - Configure in Spring Security

8. TIMEZONE HANDLING
   - Store all dates in UTC in database
   - Convert to user's timezone in frontend
   - Use ZonedDateTime or Instant (not LocalDateTime)
   - Add timezone field to User entity
   - Display dates in hotel's local timezone

9. EDGE CASE: SAME-DAY BOOKING
   - Allow same-day booking if check-in time not passed
   - Hotel check-in time: configurable (default 3 PM)
   - Validate check-in time in reservation service
   - Return error if too late for same-day booking

10. EDGE CASE: PAST DATES
    - Reject reservations with check-in date in the past
    - Validate in frontend (disable past dates)
    - Validate in backend (return 400 Bad Request)
    - Error message: "Check-in date must be in the future"

11. EDGE CASE: MAXIMUM STAY DURATION
    - Set maximum stay duration (e.g., 30 nights)
    - Validate in reservation service
    - Error message: "Maximum stay duration is 30 nights"

12. EDGE CASE: MINIMUM STAY DURATION
    - Set minimum stay duration (e.g., 1 night)
    - Already enforced in DateRange validation
    - Ensure consistent error messages

13. EDGE CASE: OVERBOOKING PREVENTION
    - Double-check availability before confirming payment
    - Use pessimistic locking during reservation
    - Handle case: payment succeeds but room no longer available
    - Auto-refund if overbooking detected

14. EDGE CASE: PAYMENT TIMEOUT
    - Handle case: user doesn't complete payment within timeout
    - Ensure reservation expires after 30 minutes
    - Ensure availability restored
    - Send timeout notification email

15. EDGE CASE: DUPLICATE PAYMENT
    - Use idempotency key for payment API calls
    - Detect duplicate webhook events
    - Don't double-confirm reservation
    - Log duplicate payment attempts

16. EDGE CASE: CONCURRENT MODIFICATIONS
    - Use optimistic locking for entities (add @Version field)
    - Handle OptimisticLockException gracefully
    - Retry failed operations with updated data
    - Show user-friendly error: "This reservation was modified, please try again"

17. PASSWORD POLICY
    - Enforce strong password: min 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char
    - Validate in frontend and backend
    - Reject common passwords (use list of top 10k passwords)
    - Show password strength indicator in UI

18. ACCOUNT LOCKOUT
    - Lock account after 5 failed login attempts
    - Lockout duration: 30 minutes
    - Send email notification on lockout
    - Add /api/auth/unlock endpoint (admin only)

19. SESSION MANAGEMENT
    - Limit concurrent sessions per user (max 5)
    - Allow user to view active sessions
    - Allow user to revoke sessions
    - Expire sessions after 7 days of inactivity

20. PENETRATION TESTING
    - Test for: SQL injection, XSS, CSRF, authentication bypass
    - Use OWASP ZAP or similar tool
    - Fix all critical and high vulnerabilities
    - Document findings and fixes

FILES TO CREATE:
- src/main/kotlin/com/hotresvib/infrastructure/security/RateLimitingFilter.kt
- src/main/kotlin/com/hotresvib/infrastructure/security/InputSanitizer.kt
- src/main/kotlin/com/hotresvib/infrastructure/security/SecurityHeadersFilter.kt
- src/main/kotlin/com/hotresvib/infrastructure/audit/AuditLogService.kt
- src/main/kotlin/com/hotresvib/domain/audit/AuditLog.kt
- src/main/kotlin/com/hotresvib/application/port/AuditLogRepository.kt
- src/main/kotlin/com/hotresvib/application/validation/PasswordValidator.kt
- src/test/kotlin/com/hotresvib/security/SecurityHardeningTest.kt
- src/test/kotlin/com/hotresvib/security/EdgeCasesTest.kt

FILES TO UPDATE:
- src/main/kotlin/com/hotresvib/infrastructure/config/SecurityConfig.kt (add headers, CSRF)
- src/main/kotlin/com/hotresvib/application/service/ReservationService.kt (add locking)
- src/main/kotlin/com/hotresvib/application/service/AvailabilityApplicationService.kt (add locking)
- src/main/kotlin/com/hotresvib/application/web/AuthController.kt (add rate limiting, account lockout)
- src/main/kotlin/com/hotresvib/domain/user/User.kt (add failedLoginAttempts, lockedUntil)
- All entities (add @Version for optimistic locking)

ACCEPTANCE CRITERIA:
✅ Rate limiting prevents abuse
✅ CSRF protection enabled
✅ All inputs sanitized (XSS prevention)
✅ No SQL injection vulnerabilities
✅ Pessimistic locking prevents double-booking
✅ All security-sensitive actions audited
✅ Security headers present in all responses
✅ Timezone handling correct (UTC in DB)
✅ Same-day booking validation works
✅ Past date bookings rejected
✅ Maximum stay duration enforced
✅ Overbooking impossible
✅ Payment timeout handled correctly
✅ Duplicate payments detected
✅ Strong password policy enforced
✅ Account lockout after failed logins
✅ Penetration test passes
✅ 30+ security tests pass

CONSTRAINTS:
- All dates stored in UTC
- Password must meet complexity requirements
- Rate limits configurable via application.yml
- Audit log retention: 90 days minimum
- Security headers mandatory in production
```

---

## Phase 12: Performance & Production Readiness

### 🎯 Prompt for Phase 12

```
CONTEXT:
You are working on HotResvib, a hotel reservation system. All features and security complete.
Need to optimize performance and prepare for production deployment.

OBJECTIVE:
Optimize application performance, add caching, monitoring, and create production deployment pipeline.

REQUIREMENTS:

1. REDIS CACHING
   - Add Redis dependency (spring-boot-starter-data-redis)
   - Cache frequently accessed data:
     * Hotel list (TTL: 1 hour)
     * Room details (TTL: 30 minutes)
     * Availability (TTL: 5 minutes)
     * Pricing rules (TTL: 1 hour)
   - Use @Cacheable, @CacheEvict, @CachePut annotations
   - Cache key strategy: e.g., "hotel:${hotelId}"
   - Configure Redis connection in application.yml

2. DATABASE INDEXES
   - Add indexes on frequently queried columns:
     * hotels(city)
     * hotels(country)
     * rooms(hotel_id, type)
     * rooms(base_rate)
     * reservations(user_id, status)
     * reservations(room_id, status)
     * availability(room_id, date_range_start)
     * pricing_rules(room_id, date_range_start)
   - Create Flyway migration: V10__add_indexes.sql
   - Analyze query performance with EXPLAIN

3. QUERY OPTIMIZATION
   - Fix N+1 queries using JOIN FETCH
   - Use DTO projections for read-only queries
   - Batch fetch collections where appropriate
   - Enable Hibernate query plan cache
   - Set fetch size for large result sets

4. API RESPONSE COMPRESSION
   - Enable GZIP compression for responses
   - Compress responses > 1KB
   - Configure in Spring Boot application.yml
   - Test compression with curl

5. CONNECTION POOL TUNING
   - Configure HikariCP pool size:
     * minimum-idle: 10
     * maximum-pool-size: 50
   - Set connection timeout: 30 seconds
   - Set idle timeout: 10 minutes
   - Monitor pool metrics

6. MONITORING WITH ACTUATOR
   - Add spring-boot-starter-actuator dependency
   - Enable endpoints: /actuator/health, /actuator/metrics, /actuator/prometheus
   - Expose custom metrics:
     * reservation_created_total (counter)
     * reservation_cancellation_total (counter)
     * payment_success_total (counter)
     * payment_failure_total (counter)
   - Use Micrometer for metrics

7. PROMETHEUS INTEGRATION
   - Add Prometheus metrics endpoint
   - Configure scrape interval
   - Export JVM metrics (heap, GC, threads)
   - Export database metrics (connections, queries)
   - Export HTTP metrics (requests, latency)

8. GRAFANA DASHBOARDS
   - Create Docker Compose with Prometheus + Grafana
   - Dashboard: Application Overview
     * Request rate (req/sec)
     * Response time (p50, p95, p99)
     * Error rate (%)
   - Dashboard: Business Metrics
     * Reservations per hour
     * Revenue per day
     * Occupancy rate
   - Dashboard: System Health
     * JVM heap usage
     * Database connections
     * Redis cache hit rate

9. LOGGING CONFIGURATION
   - Use Logback for structured logging
   - Log levels: INFO in production, DEBUG in dev
   - Log to file: /var/log/hotresvib/app.log
   - Rotate logs daily, keep 30 days
   - Include correlation ID in all logs
   - Send logs to ELK stack or CloudWatch

10. ERROR TRACKING (SENTRY)
    - Add Sentry SDK dependency
    - Configure Sentry DSN
    - Capture all unhandled exceptions
    - Add user context to errors
    - Set environment: dev, staging, production

11. LOAD TESTING
    - Use JMeter or Gatling
    - Test scenarios:
      * 100 concurrent users searching hotels
      * 50 concurrent users making bookings
      * Sustained load: 500 req/sec for 10 minutes
      * Spike test: 1000 req/sec for 1 minute
    - Measure: response time, error rate, throughput
    - Target: p95 response time < 500ms

12. DOCKER CONTAINERIZATION
    - Create Dockerfile for Spring Boot app
    - Multi-stage build (build + runtime)
    - Use JDK 17+ base image
    - Optimize layer caching
    - Set JVM memory limits
    - Health check endpoint

13. DOCKER COMPOSE SETUP
    - Services: app, postgres, redis, prometheus, grafana
    - Networks: frontend, backend
    - Volumes: postgres-data, redis-data
    - Environment variables from .env file
    - Startup order with depends_on

14. CI/CD PIPELINE (GITHUB ACTIONS)
    - Trigger on push to main
    - Jobs:
      1. Build: Compile, run tests
      2. Security: Dependency check, SAST
      3. Docker: Build and push image
      4. Deploy: Deploy to staging/production
    - Use secrets for credentials
    - Notify on Slack/Discord on failure

15. PRODUCTION DEPLOYMENT GUIDE
    - Document infrastructure requirements
    - Database: PostgreSQL 14+, 2 vCPU, 4GB RAM
    - Redis: 1GB RAM
    - App: 2 instances, 2 vCPU, 4GB RAM each
    - Load balancer: Nginx or AWS ALB
    - SSL certificate setup (Let's Encrypt)
    - Environment variables configuration
    - Database backup strategy (daily)

16. HEALTH CHECKS
    - Implement /actuator/health endpoint
    - Check: Database connection, Redis connection
    - Check: Disk space, Memory usage
    - Return 200 OK if healthy, 503 if not
    - Use in load balancer health checks

17. GRACEFUL SHUTDOWN
    - Configure graceful shutdown timeout: 30 seconds
    - Finish processing in-flight requests
    - Stop accepting new requests
    - Close database connections
    - Flush caches

18. BACKUP & RECOVERY
    - Automated daily PostgreSQL backups
    - Backup retention: 30 days
    - Test restore procedure
    - Document recovery steps
    - Store backups in S3 or cloud storage

19. PERFORMANCE BENCHMARKS
    - Document baseline performance:
      * Search hotels: < 200ms
      * Create reservation: < 500ms
      * Payment confirmation: < 1000ms
    - Set up performance regression tests
    - Alert if performance degrades > 20%

FILES TO CREATE:
- Dockerfile
- docker-compose.yml
- .github/workflows/ci-cd.yml
- src/main/resources/logback-spring.xml
- src/main/kotlin/com/hotresvib/infrastructure/config/CacheConfig.kt
- src/main/kotlin/com/hotresvib/infrastructure/config/MonitoringConfig.kt
- src/main/kotlin/com/hotresvib/infrastructure/metrics/CustomMetrics.kt
- src/main/resources/db/migration/V10__add_indexes.sql
- load-tests/hotel-search.jmx (JMeter)
- docs/DEPLOYMENT.md
- docs/PERFORMANCE.md

FILES TO UPDATE:
- src/main/resources/application.yml (add cache, monitoring config)
- src/main/resources/application-prod.yml (production config)
- build.gradle.kts (add actuator, redis, micrometer dependencies)

ACCEPTANCE CRITERIA:
✅ Redis caching reduces database load by 50%
✅ All queries optimized (no N+1)
✅ API responses compressed with GZIP
✅ Database indexes improve query speed
✅ Prometheus metrics exported
✅ Grafana dashboards visualize metrics
✅ Load test: 500 req/sec with p95 < 500ms
✅ Docker image builds successfully
✅ Docker Compose starts all services
✅ CI/CD pipeline runs tests and builds
✅ Production deployment documented
✅ Health checks return correct status
✅ Graceful shutdown works correctly

CONSTRAINTS:
- Cache invalidation strategy must be correct
- Production config uses environment variables
- Secrets not committed to repository
- Database backups automated and tested
- Monitoring data retained for 30 days minimum
```

---

## Summary Matrix

| Phase | Feature | Backend | Frontend | Days | Priority |
|-------|---------|---------|----------|------|----------|
| 4 | Authentication | ✅ | ✅ | 2-3 | 🔴 Critical |
| 5 | JPA Persistence | ✅ | - | 3-4 | 🔴 Critical |
| 6 | Payment & Lifecycle | ✅ | ✅ | 4-5 | 🔴 High |
| 7 | Search & Filtering | ✅ | ✅ | 3-4 | 🟡 Medium |
| 8 | Email Notifications | ✅ | - | 2-3 | 🟢 Low |
| 9 | Customer Frontend | - | ✅ | 7-10 | 🔴 High |
| 10 | Admin Dashboard | ✅ | ✅ | 5-7 | 🟢 Low |
| 11 | Security Hardening | ✅ | ✅ | 3-4 | 🔴 High |
| 12 | Performance | ✅ | - | 3-4 | 🟡 Medium |

**Total Estimated Time**: 32-47 days (6-9 weeks)

---

**Generated**: January 29, 2026  
**Last Updated**: January 29, 2026
