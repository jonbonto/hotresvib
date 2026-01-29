package com.hotresvib.application.job

import com.hotresvib.application.service.ReservationLifecycleService
import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.domain.reservation.ReservationStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.Duration

@Component
class ReservationExpirationJob(
    private val reservationRepository: ReservationRepository,
    private val reservationLifecycleService: ReservationLifecycleService,
    @Value("\${reservation.payment-timeout-minutes:30}") private val timeoutMinutes: Long
) {
    
    private val logger = LoggerFactory.getLogger(ReservationExpirationJob::class.java)
    
    /**
     * Run every 5 minutes to check for expired reservations
     */
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    fun expireOldReservations() {
        logger.info("Running reservation expiration job")
        
        val cutoffTime = Instant.now().minus(Duration.ofMinutes(timeoutMinutes))
        
        try {
            // Find all PENDING_PAYMENT reservations older than timeout
            val expiredReservations = reservationRepository.findByStatus(ReservationStatus.PENDING_PAYMENT)
                .filter { reservation -> reservation.createdAt.isBefore(cutoffTime) }
            
            logger.info("Found ${expiredReservations.size} reservations to expire")
            
            expiredReservations.forEach { reservation ->
                try {
                    reservationLifecycleService.expireReservation(reservation.id)
                    logger.info("Expired reservation ${reservation.id}")
                } catch (e: Exception) {
                    logger.error("Failed to expire reservation ${reservation.id}: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            logger.error("Error in expiration job: ${e.message}", e)
        }
    }
}
