export type ReservationStatus = 
  | 'DRAFT' 
  | 'PENDING_PAYMENT' 
  | 'CONFIRMED' 
  | 'CANCELLED' 
  | 'EXPIRED' 
  | 'REFUNDED';

export interface Reservation {
  id: string;
  userId: string;
  roomId: string;
  checkInDate: string;
  checkOutDate: string;
  guestCount: number;
  status: ReservationStatus;
  totalPrice: number;
  currency: string;
  specialRequests?: string;
  createdAt: string;
}

export interface CreateReservationRequest {
  roomId: string;
  checkInDate: string;
  checkOutDate: string;
  guestCount: number;
  specialRequests?: string;
}

export interface PaymentIntentRequest {
  reservationId: string;
  amount: number;
  currency: string;
}

export interface PaymentIntentResponse {
  clientSecret: string;
  paymentIntentId: string;
}
