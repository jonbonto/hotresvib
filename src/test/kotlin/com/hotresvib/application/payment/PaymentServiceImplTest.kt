package com.hotresvib.application.payment

import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.domain.payment.PaymentStatus
import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class PaymentServiceImplTest {

    private val reservationRepository: ReservationRepository = mock()
    private val paymentService = PaymentServiceImpl(reservationRepository)

    private fun dummyReservation(id: ReservationId, userId: UserId): Reservation {
        return Reservation(
            id = id,
            userId = userId,
            roomId = RoomId(UUID.randomUUID()),
            stay = DateRange(LocalDate.now(), LocalDate.now().plusDays(1)),
            totalAmount = Money(BigDecimal("100"), "USD"),
            status = ReservationStatus.DRAFT,
            createdAt = Instant.now()
        )
    }

    @Test
    fun `processPayment should return pending payment and mark reservation pending_payment`() {
        val reservationId = ReservationId(UUID.randomUUID())
        val userId = UserId(UUID.randomUUID())
        val reservation = dummyReservation(reservationId, userId)
        whenever(reservationRepository.findById(reservationId)).thenReturn(reservation)

        val amount = Money(BigDecimal("50"), "USD")
        val payment = paymentService.processPayment(reservationId, amount, "card")

        // payment object must be pending
        assert(payment.status == PaymentStatus.PENDING)
        assert(payment.reservationId == reservationId)
        assert(payment.paymentMethod == "card")

        // verify that reservation save was invoked with PENDING_PAYMENT status
        verify(reservationRepository).save(reservation.copy(status = ReservationStatus.PENDING_PAYMENT))
    }
}
