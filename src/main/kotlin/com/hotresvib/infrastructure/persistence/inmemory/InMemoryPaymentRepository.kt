package com.hotresvib.infrastructure.persistence.inmemory

import com.hotresvib.application.port.PaymentRepository
import com.hotresvib.domain.payment.Payment
import com.hotresvib.domain.payment.PaymentId
import com.hotresvib.domain.shared.ReservationId
import java.util.concurrent.ConcurrentHashMap

class InMemoryPaymentRepository : PaymentRepository {
    private val storage = ConcurrentHashMap<PaymentId, Payment>()

    override fun findById(id: PaymentId): Payment? = storage[id]

    override fun findByReservationId(reservationId: ReservationId): List<Payment> =
        storage.values.filter { it.reservationId == reservationId }

    override fun save(payment: Payment): Payment {
        storage[payment.id] = payment
        return payment
    }
}
