package com.hotresvib.application.web

import com.hotresvib.application.dto.*
import com.hotresvib.application.payment.PaymentServiceImpl
import com.hotresvib.application.port.PaymentRepository
import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.Money
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = ["*"])
class PaymentController(
    private val paymentService: PaymentServiceImpl,
    private val paymentRepository: PaymentRepository,
    private val reservationRepository: ReservationRepository
) {

    @PostMapping
    fun processPayment(
        @RequestBody request: PaymentRequest,
        authentication: Authentication
    ): ResponseEntity<PaymentResponse> {
        return try {
            val userId = UUID.fromString(authentication.principal as String)
            val reservation = reservationRepository.findById(ReservationId(request.reservationId))
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)

            // Check authorization
            if (reservation.userId.value != userId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null)
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
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @GetMapping("/{id}")
    fun getPayment(
        @PathVariable id: UUID,
        authentication: Authentication
    ): ResponseEntity<PaymentResponse> {
        return try {
            val userId = UUID.fromString(authentication.principal as String)
            
            val payment = paymentRepository.findById(id)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)

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
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }
}
