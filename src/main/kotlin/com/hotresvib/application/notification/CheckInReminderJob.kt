package com.hotresvib.application.notification

import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.application.port.HotelRepository
import com.hotresvib.application.port.UserRepository
import com.hotresvib.domain.reservation.ReservationStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDate

/**
 * Scheduled job for sending check-in reminder emails
 */
@Service
class CheckInReminderJob(
    private val reservationRepository: ReservationRepository,
    private val hotelRepository: HotelRepository,
    private val roomRepository: com.hotresvib.application.port.RoomRepository,
    private val userRepository: UserRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Run daily at 9:00 AM to send check-in reminders
     * Check-in reminders are sent for reservations with check-in date tomorrow
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "UTC")
    fun sendCheckInReminders() {
        try {
            logger.info("Starting check-in reminder job")
            
            val tomorrow = LocalDate.now().plusDays(1)
            val confirmedReservations = reservationRepository.findByStatus(ReservationStatus.CONFIRMED)
            
            val todayReminders = confirmedReservations.filter { reservation ->
                reservation.stay.startDate == tomorrow
            }

            if (todayReminders.isEmpty()) {
                logger.info("No reservations found for check-in reminder")
                return
            }

            todayReminders.forEach { reservation ->
                try {
                    val room = roomRepository.findById(reservation.roomId)
                    val hotel = room?.let { room -> hotelRepository.findById(room.hotelId) }
                    val user = userRepository.findById(reservation.userId)

                    if (hotel != null && user != null) {
                        val event = CheckInReminderEvent(
                            source2 = this,
                            bookingId = reservation.id.value.toString().hashCode().toLong(),
                            hotelName = hotel.name.value,
                            checkInDate = reservation.stay.startDate.toString(),
                            email = user.email.value,
                            guestName = user.displayName,
                            hotelAddress = "${hotel.city}, ${hotel.country}",
                            checkInTime = "15:00" // Default check-in time
                        )
                        eventPublisher.publishEvent(event)
                        logger.info("Published check-in reminder for reservation ${reservation.id}")
                    }
                } catch (e: Exception) {
                    logger.error("Error processing check-in reminder for reservation ${reservation.id}", e)
                }
            }

            logger.info("Check-in reminder job completed. Processed ${todayReminders.size} reservations")
        } catch (e: Exception) {
            logger.error("Error in check-in reminder job", e)
        }
    }

    /**
     * Run every 6 hours to resend failed reminders
     */
    @Scheduled(fixedDelay = 21600000, initialDelay = 3600000) // 6 hours
    fun retryFailedReminders() {
        try {
            logger.info("Starting retry failed reminders job")
            
            // Query for failed reminders that haven't been retried
            // This would require additional email log tracking
            logger.info("Completed retry failed reminders job")
        } catch (e: Exception) {
            logger.error("Error in retry failed reminders job", e)
        }
    }
}
