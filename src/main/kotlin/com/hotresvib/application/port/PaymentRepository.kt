package com.hotresvib.application.port

import com.hotresvib.domain.payment.Payment
import com.hotresvib.domain.payment.PaymentId
import com.hotresvib.domain.shared.ReservationId

interface PaymentRepository {
    fun findById(id: PaymentId): Payment?
    fun findByReservationId(reservationId: ReservationId): List<Payment>
    fun save(payment: Payment): Payment
}
