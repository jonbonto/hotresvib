package com.hotresvib.infrastructure.persistence.jpa.adapters

import com.hotresvib.application.port.PaymentRepository
import com.hotresvib.domain.payment.Payment
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.infrastructure.persistence.jpa.PaymentJpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class PaymentJpaAdapter(private val repo: PaymentJpaRepository) : PaymentRepository {
    override fun findById(id: UUID): Payment? = repo.findById(id).orElse(null)

    override fun findByReservationId(reservationId: ReservationId): List<Payment> = repo.findByReservationId(reservationId)

    override fun save(payment: Payment): Payment = repo.save(payment)
}
