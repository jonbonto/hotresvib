package com.hotresvib.infrastructure.persistence.jpa.converters

import com.hotresvib.domain.availability.AvailabilityId
import com.hotresvib.domain.availability.BlockoutReason
import java.util.UUID
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class BlockoutReasonConverter : AttributeConverter<BlockoutReason, String> {
    override fun convertToDatabaseColumn(attribute: BlockoutReason?): String? = attribute?.value
    override fun convertToEntityAttribute(dbData: String?): BlockoutReason? = dbData?.let { BlockoutReason(it) }
}

@Converter(autoApply = true)
class AvailabilityIdConverter : AttributeConverter<AvailabilityId, UUID> {
    override fun convertToDatabaseColumn(attribute: AvailabilityId?): UUID? = attribute?.value
    override fun convertToEntityAttribute(dbData: UUID?): AvailabilityId? = dbData?.let { AvailabilityId(it) }
}
