package com.hotresvib.infrastructure.persistence.inmemory

import com.hotresvib.application.port.HotelRepository
import com.hotresvib.domain.hotel.Hotel
import com.hotresvib.domain.shared.HotelId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.concurrent.ConcurrentHashMap

class InMemoryHotelRepository : HotelRepository {
    val hotels = ConcurrentHashMap<HotelId, Hotel>()

    override fun findById(id: HotelId): Hotel? = hotels[id]

    override fun findAll(): List<Hotel> = hotels.values.toList()

    override fun save(hotel: Hotel): Hotel {
        hotels[hotel.id] = hotel
        return hotel
    }
    
    override fun findByCityContainingIgnoreCase(city: String, pageable: Pageable): Page<Hotel> {
        val filtered = hotels.values.filter { it.city.contains(city, ignoreCase = true) }
        return paginateList(filtered, pageable)
    }
    
    override fun findByCountryContainingIgnoreCase(country: String, pageable: Pageable): Page<Hotel> {
        val filtered = hotels.values.filter { it.country.contains(country, ignoreCase = true) }
        return paginateList(filtered, pageable)
    }
    
    override fun searchByCriteria(city: String?, country: String?, name: String?, pageable: Pageable): Page<Hotel> {
        var filtered = hotels.values.toList()
        
        if (city != null) {
            filtered = filtered.filter { it.city.contains(city, ignoreCase = true) }
        }
        if (country != null) {
            filtered = filtered.filter { it.country.contains(country, ignoreCase = true) }
        }
        if (name != null) {
            filtered = filtered.filter { it.name.value.contains(name, ignoreCase = true) }
        }
        
        return paginateList(filtered, pageable)
    }
    
    override fun findByIsFeatured(isFeatured: Boolean, pageable: Pageable): Page<Hotel> {
        val filtered = hotels.values.filter { it.isFeatured == isFeatured }
        return paginateList(filtered, pageable)
    }
    
    override fun findAllPaged(pageable: Pageable): Page<Hotel> {
        return paginateList(hotels.values.toList(), pageable)
    }
    
    private fun paginateList(list: List<Hotel>, pageable: Pageable): Page<Hotel> {
        val start = pageable.offset.toInt()
        val end = minOf(start + pageable.pageSize, list.size)
        val pageContent = if (start < list.size) list.subList(start, end) else emptyList()
        return PageImpl(pageContent, pageable, list.size.toLong())
    }
}
