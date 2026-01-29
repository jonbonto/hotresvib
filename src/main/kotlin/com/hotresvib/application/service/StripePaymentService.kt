package com.hotresvib.application.service

import com.hotresvib.application.dto.PaymentIntentRequest
import com.hotresvib.application.dto.PaymentIntentResponse
import com.stripe.Stripe
import com.stripe.model.PaymentIntent
import com.stripe.model.Refund
import com.stripe.param.PaymentIntentCreateParams
import com.stripe.param.RefundCreateParams
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class StripePaymentService(
    @Value("\${stripe.api-key-secret:sk_test_dummy}") private val apiKey: String
) {
    
    init {
        Stripe.apiKey = apiKey
    }
    
    /**
     * Create a Stripe PaymentIntent
     */
    fun createPaymentIntent(request: PaymentIntentRequest): PaymentIntentResponse {
        // Convert amount to cents (Stripe requires smallest currency unit)
        val amountInCents = request.amount.multiply(BigDecimal(100)).toLong()
        
        val params = PaymentIntentCreateParams.builder()
            .setAmount(amountInCents)
            .setCurrency(request.currency.lowercase())
            .putMetadata("reservationId", request.reservationId.toString())
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build()
            )
            .build()
        
        val paymentIntent = PaymentIntent.create(params)
        
        return PaymentIntentResponse(
            paymentIntentId = paymentIntent.id,
            clientSecret = paymentIntent.clientSecret ?: "",
            amount = request.amount,
            currency = request.currency,
            status = paymentIntent.status
        )
    }
    
    /**
     * Retrieve a PaymentIntent by ID
     */
    fun getPaymentIntent(paymentIntentId: String): PaymentIntent {
        return PaymentIntent.retrieve(paymentIntentId)
    }
    
    /**
     * Create a refund for a payment
     */
    fun createRefund(paymentIntentId: String, amount: BigDecimal? = null): Refund {
        val params = RefundCreateParams.builder()
            .setPaymentIntent(paymentIntentId)
            .apply {
                if (amount != null) {
                    setAmount(amount.multiply(BigDecimal(100)).toLong())
                }
            }
            .build()
        
        return Refund.create(params)
    }
    
    /**
     * Verify webhook signature
     */
    fun verifyWebhookSignature(payload: String, signature: String, secret: String): Boolean {
        return try {
            com.stripe.net.Webhook.constructEvent(payload, signature, secret)
            true
        } catch (e: Exception) {
            false
        }
    }
}
