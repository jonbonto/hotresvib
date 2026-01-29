package com.hotresvib.infrastructure.persistence.jpa.converters

import com.hotresvib.domain.availability.AvailableQuantity
import com.hotresvib.domain.availability.AvailabilityId
import java.util.UUID
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class AvailableQuantityConverter : AttributeConverter<AvailableQuantity, Int> {
    override fun convertToDatabaseColumn(attribute: AvailableQuantity?): Int? = attribute?.value
    override fun convertToEntityAttribute(dbData: Int?): AvailableQuantity? = dbData?.let { AvailableQuantity(it) }
}

@Converter(autoApply = true)
class AvailabilityIdConverter : AttributeConverter<AvailabilityId, UUID> {
    override fun convertToDatabaseColumn(attribute: AvailabilityId?): UUID? = attribute?.value
    override fun convertToEntityAttribute(dbData: UUID?): AvailabilityId? = dbData?.let { AvailabilityId(it) }
}
