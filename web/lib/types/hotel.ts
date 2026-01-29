export interface Hotel {
  id: string;
  name: string;
  city: string;
  country: string;
  roomCount?: number;
  minPrice?: number;
  isFeatured: boolean;
  description?: string;
  totalRooms?: number;
  address?: {
    street: string;
    city: string;
    state: string;
    country: string;
    zipCode: string;
  };
  contactInfo?: {
    phone: string;
    email: string;
  };
}

export interface Room {
  id: string;
  hotelId: string;
  number: string;
  type: 'SINGLE' | 'DOUBLE' | 'SUITE';
  baseRate: number;
  currency: string;
  description?: string;
  capacity?: number;
  bedType?: string;
  amenities?: string[];
  available?: number;
}

export interface RoomSearchResponse extends Room {
  hotelName: string;
  city: string;
  country: string;
  isAvailable?: boolean;
}

export interface RoomAvailability extends Room {
  hotelName: string;
  city: string;
  country: string;
  totalPrice: number;
  nights: number;
}

export interface HotelSearchCriteria {
  city?: string;
  country?: string;
  name?: string;
  checkInDate?: string;
  checkOutDate?: string;
  guests?: number;
  page?: number;
  size?: number;
  sort?: string;
}
export interface RoomSearchCriteria {
  hotelId?: string;
  type?: string;
  minPrice?: number;
  maxPrice?: number;
  checkInDate?: string;
  checkOutDate?: string;
  page?: number;
  size?: number;
}
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
