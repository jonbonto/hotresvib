package com.hotresvib.application.port

import com.hotresvib.domain.payment.Payment
import com.hotresvib.domain.shared.ReservationId
import java.util.UUID

interface PaymentRepository {
    fun findById(id: UUID): Payment?
    fun findByReservationId(reservationId: ReservationId): List<Payment>
    fun save(payment: Payment): Payment
}
