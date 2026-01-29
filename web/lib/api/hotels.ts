import { fetcher } from './client';
import type { 
  Hotel,
  Room,
  HotelSearchCriteria,
  RoomSearchCriteria,
  PaginatedResponse,
  RoomSearchResponse,
  RoomAvailability
} from '../types/hotel';

export async function searchHotels(
  criteria: HotelSearchCriteria
): Promise<PaginatedResponse<Hotel>> {
  const params = new URLSearchParams();
  
  if (criteria.city) params.append('city', criteria.city);
  if (criteria.country) params.append('country', criteria.country);
  if (criteria.name) params.append('name', criteria.name);
  if (criteria.checkInDate) params.append('checkInDate', criteria.checkInDate);
  if (criteria.checkOutDate) params.append('checkOutDate', criteria.checkOutDate);
  if (criteria.guests) params.append('guests', criteria.guests.toString());
  if (criteria.page !== undefined) params.append('page', criteria.page.toString());
  if (criteria.size) params.append('size', criteria.size.toString());
  if (criteria.sort) params.append('sort', criteria.sort);

  return fetcher<PaginatedResponse<Hotel>>(`/search/hotels?${params.toString()}`);
}

export async function getFeaturedHotels(): Promise<PaginatedResponse<Hotel>> {
  return fetcher<PaginatedResponse<Hotel>>('/search/featured?size=4');
}

export async function getHotel(id: string): Promise<Hotel> {
  return fetcher<Hotel>(`/hotels/${id}`);
}

export async function getRoomById(roomId: string): Promise<Room> {
  return fetcher<Room>(`/rooms/${roomId}`);
}

export async function searchRooms(criteria: RoomSearchCriteria): Promise<PaginatedResponse<RoomSearchResponse>> {
  const params = new URLSearchParams();
  
  if (criteria.hotelId) params.append('hotelId', criteria.hotelId);
  if (criteria.type) params.append('type', criteria.type);
  if (criteria.minPrice) params.append('minPrice', criteria.minPrice.toString());
  if (criteria.maxPrice) params.append('maxPrice', criteria.maxPrice.toString());
  if (criteria.checkInDate) params.append('checkInDate', criteria.checkInDate);
  if (criteria.checkOutDate) params.append('checkOutDate', criteria.checkOutDate);
  if (criteria.page !== undefined) params.append('page', criteria.page.toString());
  if (criteria.size) params.append('size', criteria.size.toString());

  return fetcher<PaginatedResponse<RoomSearchResponse>>(`/search/rooms?${params.toString()}`);
}

export async function searchAvailableRooms(criteria: {
  checkIn: string;
  checkOut: string;
  city?: string;
  country?: string;
  type?: string;
  minPrice?: number;
  maxPrice?: number;
  guests?: number;
  page?: number;
  size?: number;
}): Promise<PaginatedResponse<RoomAvailability>> {
  const params = new URLSearchParams();
  
  params.append('checkIn', criteria.checkIn);
  params.append('checkOut', criteria.checkOut);
  if (criteria.city) params.append('city', criteria.city);
  if (criteria.country) params.append('country', criteria.country);
  if (criteria.type) params.append('type', criteria.type);
  if (criteria.minPrice) params.append('minPrice', criteria.minPrice.toString());
  if (criteria.maxPrice) params.append('maxPrice', criteria.maxPrice.toString());
  if (criteria.guests) params.append('guests', criteria.guests.toString());
  if (criteria.page !== undefined) params.append('page', criteria.page.toString());
  if (criteria.size) params.append('size', criteria.size.toString());

  return fetcher<PaginatedResponse<RoomAvailability>>(`/search/available-rooms?${params.toString()}`);
}

export async function calculatePrice(
  roomId: string,
  checkIn: string,
  checkOut: string
): Promise<{
  basePrice: number;
  currency: string;
  nights: number;
  subtotal: number;
  pricingRulesApplied: string[];
  total: number;
}> {
  const params = new URLSearchParams({
    roomId,
    checkIn,
    checkOut,
  });

  return fetcher(`/search/price?${params.toString()}`);
}
