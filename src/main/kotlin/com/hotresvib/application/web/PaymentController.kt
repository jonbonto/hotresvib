package com.hotresvib.application.web

import com.hotresvib.application.dto.*
import com.hotresvib.application.payment.PaymentServiceImpl
import com.hotresvib.application.port.PaymentRepository
import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.application.service.StripePaymentService
import com.hotresvib.application.service.ReservationApplicationService
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.payment.Payment
import com.hotresvib.domain.payment.PaymentStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID
import org.springframework.security.access.prepost.PreAuthorize

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = ["*"])
class PaymentController(
    private val paymentService: PaymentServiceImpl,
    private val paymentRepository: PaymentRepository,
    private val reservationRepository: ReservationRepository,
    private val stripePaymentService: StripePaymentService,
    private val reservationService: ReservationApplicationService
) {

    /**
     * Create a payment intent for a reservation
     */
    @PostMapping("/intent")
    fun createPaymentIntent(
        @RequestBody request: PaymentIntentRequest,
        authentication: Authentication?
    ): ResponseEntity<PaymentIntentResponse> {
        return try {
            val auth = authentication ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")
            val userId = try {
                java.util.UUID.fromString(auth.principal as String)
            } catch (e: IllegalArgumentException) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID")
            }
            val reservation = reservationRepository.findById(ReservationId(request.reservationId))
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found")

            // Check authorization
            if (reservation.userId.value != userId) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "No permission to access this reservation")
            }

            // Create Stripe payment intent
            val paymentIntentResponse = stripePaymentService.createPaymentIntent(request)
            
            // Create payment record
            val payment = Payment(
                id = UUID.randomUUID(),
                reservationId = reservation.id,
                amount = Money(request.amount, request.currency),
                status = PaymentStatus.PENDING,
                paymentMethod = "stripe",
                transactionId = null,
                paymentIntentId = paymentIntentResponse.paymentIntentId,
                metadata = null,
                idempotencyKey = paymentIntentResponse.paymentIntentId,
                createdAt = Instant.now()
            )
            paymentRepository.save(payment)
            
            // Update reservation to PENDING_PAYMENT
            reservationService.initiatePayment(reservation.id)

            ResponseEntity.ok(paymentIntentResponse)
        } catch (e: ResponseStatusException) {
            println("Error creating payment intent: ${e.message}")
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create payment intent")
        }
    }

    @PostMapping
    fun processPayment(
        @RequestBody request: PaymentRequest,
        authentication: Authentication?
    ): ResponseEntity<PaymentResponse> {
        return try {
            val auth = authentication ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")
            val userId = try {
                UUID.fromString(auth.principal as String)
            } catch (e: IllegalArgumentException) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID")
            }
            val reservation = reservationRepository.findById(ReservationId(request.reservationId))
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found")

            // Check authorization
            if (reservation.userId.value != userId) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "No permission to pay for this reservation")
            }

            val amount = Money(request.amount.toBigDecimal(), request.currency)
            val payment = paymentService.processPayment(
                reservation.id,
                amount,
                request.paymentMethod
            )
            paymentRepository.save(payment)

            ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse(
                id = payment.id,
                reservationId = payment.reservationId.value,
                amount = payment.amount.amount.toDouble(),
                currency = payment.amount.currency,
                status = payment.status.toString(),
                paymentMethod = payment.paymentMethod,
                transactionId = payment.transactionId
            ))
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process payment")
        }
    }

    @GetMapping("/{id}")
    fun getPayment(
        @PathVariable id: UUID,
        authentication: Authentication?
    ): ResponseEntity<PaymentResponse> {
        return try {
            val auth = authentication ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")
            try {
                UUID.fromString(auth.principal as String)
            } catch (e: IllegalArgumentException) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID")
            }
            
            val payment = paymentRepository.findById(id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found")

            // In a real implementation, verify authorization
            ResponseEntity.ok(PaymentResponse(
                id = payment.id,
                reservationId = payment.reservationId.value,
                amount = payment.amount.amount.toDouble(),
                currency = payment.amount.currency,
                status = payment.status.toString(),
                paymentMethod = payment.paymentMethod,
                transactionId = payment.transactionId
            ))
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve payment")
        }
    }
}
