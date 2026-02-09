package com.hotresvib.infrastructure.metrics

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

/**
 * Custom Business Metrics for Phase 12
 *
 * Tracks business-level events:
 * - Reservations created per day
 * - Reservations cancelled per day
 * - Payments successful/failed
 * - Revenue per day
 *
 * Exposed to Prometheus for dashboard visualization
 */
@Component
class CustomMetrics(private val meterRegistry: MeterRegistry) {

    /**
     * Record a reservation creation event
     */
    fun recordReservationCreated() {
        meterRegistry.counter("reservation.created.total").increment()
    }

    /**
     * Record a reservation cancellation event
     */
    fun recordReservationCancelled() {
        meterRegistry.counter("reservation.cancelled.total").increment()
    }

    /**
     * Record a successful payment
     */
    fun recordPaymentSuccess() {
        meterRegistry.counter("payment.success.total").increment()
    }

    /**
     * Record a failed payment
     */
    fun recordPaymentFailure() {
        meterRegistry.counter("payment.failure.total").increment()
    }

    /**
     * Record API response time
     */
    fun recordApiLatency(endpoint: String, duration: Long) {
        meterRegistry.timer("api.latency", "endpoint", endpoint).record(java.time.Duration.ofMillis(duration))
    }

    /**
     * Record cache hit
     */
    fun recordCacheHit(cacheName: String) {
        meterRegistry.counter("cache.hits", "cache", cacheName).increment()
    }

    /**
     * Record cache miss
     */
    fun recordCacheMiss(cacheName: String) {
        meterRegistry.counter("cache.misses", "cache", cacheName).increment()
    }
}
