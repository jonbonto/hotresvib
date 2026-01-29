package com.hotresvib.application.service

import com.hotresvib.application.port.PaymentRepository
import com.hotresvib.domain.payment.Payment
import com.hotresvib.domain.payment.PaymentStatus
import org.springframework.stereotype.Service

@Service
class PaymentApplicationService(
    private val paymentRepository: PaymentRepository
) {
    fun processPayment(payment: Payment): Payment {
        // Logic to interact with a payment gateway would go here
        val processedPayment = payment.copy(status = PaymentStatus.CAPTURED)
        return paymentRepository.save(processedPayment)
    }
}
