package com.hotresvib.application.web

import com.hotresvib.application.service.ReservationLifecycleService
import com.hotresvib.application.service.StripePaymentService
import com.hotresvib.application.port.PaymentRepository
import com.hotresvib.domain.shared.ReservationId
import com.stripe.model.Event
import com.stripe.model.PaymentIntent
import com.stripe.net.Webhook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/webhooks")
class WebhookController(
    private val reservationLifecycleService: ReservationLifecycleService,
    private val stripePaymentService: StripePaymentService,
    private val paymentRepository: PaymentRepository,
    @Value("\${stripe.webhook-secret:whsec_dummy}") private val webhookSecret: String
) {
    
    private val logger = LoggerFactory.getLogger(WebhookController::class.java)
    
    /**
     * Stripe webhook endpoint
     * Handles payment events
     */
    @PostMapping("/stripe")
    fun handleStripeWebhook(
        @RequestBody payload: String,
        @RequestHeader("Stripe-Signature") signature: String
    ): ResponseEntity<String> {
        logger.info("Received Stripe webhook")
        
        // Verify webhook signature
        val event = try {
            Webhook.constructEvent(payload, signature, webhookSecret)
        } catch (e: Exception) {
            logger.error("Invalid webhook signature: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature")
        }
        
        logger.info("Webhook event type: ${event.type}")
        
        // Handle event
        when (event.type) {
            "payment_intent.succeeded" -> handlePaymentSuccess(event)
            "payment_intent.payment_failed" -> handlePaymentFailure(event)
            else -> logger.info("Unhandled event type: ${event.type}")
        }
        
        return ResponseEntity.ok("Webhook received")
    }
    
    private fun handlePaymentSuccess(event: Event) {
        val paymentIntent = event.dataObjectDeserializer.`object`.get() as PaymentIntent
        val reservationId = paymentIntent.metadata["reservationId"]
        
        if (reservationId == null) {
            logger.error("Payment intent ${paymentIntent.id} missing reservationId in metadata")
            return
        }
        
        logger.info("Payment succeeded for reservation $reservationId")
        
        try {
            // Check idempotency
            val existingPayment = paymentRepository.findByIdempotencyKey(paymentIntent.id)
            if (existingPayment != null && existingPayment.status.name == "COMPLETED") {
                logger.info("Payment already processed (idempotency check)")
                return
            }
            
            // Find payment by payment intent ID
            val payment = paymentRepository.findByPaymentIntentId(paymentIntent.id)
            
            if (payment != null) {
                reservationLifecycleService.confirmPayment(
                    ReservationId(UUID.fromString(reservationId)),
                    payment.id
                )
                logger.info("Reservation $reservationId confirmed")
            } else {
                logger.error("Payment not found for payment intent ${paymentIntent.id}")
            }
        } catch (e: Exception) {
            logger.error("Error processing payment success: ${e.message}", e)
        }
    }
    
    private fun handlePaymentFailure(event: Event) {
        val paymentIntent = event.dataObjectDeserializer.`object`.get() as PaymentIntent
        val reservationId = paymentIntent.metadata["reservationId"]
        
        if (reservationId == null) {
            logger.error("Payment intent ${paymentIntent.id} missing reservationId in metadata")
            return
        }
        
        logger.info("Payment failed for reservation $reservationId")
        
        try {
            reservationLifecycleService.expireReservation(
                ReservationId(UUID.fromString(reservationId))
            )
            logger.info("Reservation $reservationId expired due to payment failure")
        } catch (e: Exception) {
            logger.error("Error processing payment failure: ${e.message}", e)
        }
    }
}
