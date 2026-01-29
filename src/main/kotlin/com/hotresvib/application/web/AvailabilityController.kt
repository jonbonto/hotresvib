package com.hotresvib.application.web

import com.hotresvib.application.service.AvailabilityApplicationService
import com.hotresvib.domain.shared.RoomId
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/v1/availability")
class AvailabilityController(
    private val availabilityApplicationService: AvailabilityApplicationService
) {
    @GetMapping
    fun checkAvailability(
        @RequestParam roomId: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<Boolean> {
        val isAvailable = availabilityApplicationService.checkAvailability(RoomId(roomId), startDate, endDate)
        return ResponseEntity.ok(isAvailable)
    }
}
