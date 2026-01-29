package com.hotresvib.infrastructure.persistence.inmemory

import com.hotresvib.application.port.PaymentRepository
import com.hotresvib.domain.payment.Payment
import com.hotresvib.domain.shared.ReservationId
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryPaymentRepository : PaymentRepository {
    val payments = ConcurrentHashMap<UUID, Payment>()
    private val lock = Any()

    override fun findById(id: UUID): Payment? =
        synchronized(lock) {
            payments[id]
        }

    override fun findByReservationId(reservationId: ReservationId): List<Payment> =
        synchronized(lock) {
            payments.values.filter { it.reservationId == reservationId }
        }

    override fun save(payment: Payment): Payment {
        synchronized(lock) {
            payments[payment.id] = payment
        }
        return payment
    }
}
