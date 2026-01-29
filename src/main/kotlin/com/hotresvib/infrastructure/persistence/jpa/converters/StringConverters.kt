package com.hotresvib.infrastructure.persistence.jpa.converters

import com.hotresvib.domain.hotel.HotelName
import com.hotresvib.domain.hotel.RoomNumber
import com.hotresvib.domain.user.EmailAddress
import com.hotresvib.domain.pricing.PricingRuleId
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class HotelNameConverter : AttributeConverter<HotelName, String> {
    override fun convertToDatabaseColumn(attribute: HotelName?): String? = attribute?.value
    override fun convertToEntityAttribute(dbData: String?): HotelName? = dbData?.let { HotelName(it) }
}

@Converter(autoApply = true)
class RoomNumberConverter : AttributeConverter<RoomNumber, String> {
    override fun convertToDatabaseColumn(attribute: RoomNumber?): String? = attribute?.value
    override fun convertToEntityAttribute(dbData: String?): RoomNumber? = dbData?.let { RoomNumber(it) }
}

@Converter(autoApply = true)
class EmailAddressConverter : AttributeConverter<EmailAddress, String> {
    override fun convertToDatabaseColumn(attribute: EmailAddress?): String? = attribute?.value
    override fun convertToEntityAttribute(dbData: String?): EmailAddress? = dbData?.let { EmailAddress(it) }
}

@Converter(autoApply = true)
class PricingRuleIdConverter : AttributeConverter<PricingRuleId, String> {
    override fun convertToDatabaseColumn(attribute: PricingRuleId?): String? = attribute?.value
    override fun convertToEntityAttribute(dbData: String?): PricingRuleId? = dbData?.let { PricingRuleId(it) }
}
