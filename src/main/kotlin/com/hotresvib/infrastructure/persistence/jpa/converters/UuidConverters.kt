package com.hotresvib.infrastructure.persistence.jpa.converters

import com.hotresvib.domain.shared.*
import java.util.UUID
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class HotelIdConverter : AttributeConverter<HotelId, UUID> {
    override fun convertToDatabaseColumn(attribute: HotelId?): UUID? = attribute?.value
    override fun convertToEntityAttribute(dbData: UUID?): HotelId? = dbData?.let { HotelId(it) }
}

@Converter(autoApply = true)
class RoomIdConverter : AttributeConverter<RoomId, UUID> {
    override fun convertToDatabaseColumn(attribute: RoomId?): UUID? = attribute?.value
    override fun convertToEntityAttribute(dbData: UUID?): RoomId? = dbData?.let { RoomId(it) }
}

@Converter(autoApply = true)
class UserIdConverter : AttributeConverter<UserId, UUID> {
    override fun convertToDatabaseColumn(attribute: UserId?): UUID? = attribute?.value
    override fun convertToEntityAttribute(dbData: UUID?): UserId? = dbData?.let { UserId(it) }
}

@Converter(autoApply = true)
class ReservationIdConverter : AttributeConverter<ReservationId, UUID> {
    override fun convertToDatabaseColumn(attribute: ReservationId?): UUID? = attribute?.value
    override fun convertToEntityAttribute(dbData: UUID?): ReservationId? = dbData?.let { ReservationId(it) }
}
