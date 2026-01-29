'use client';

import { useEffect, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { getReservation } from '@/lib/api/reservations';
import { Reservation } from '@/lib/types/reservation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { CheckCircle } from 'lucide-react';
import { format } from 'date-fns';

export default function ConfirmationPage() {
  const searchParams = useSearchParams();
  const reservationId = searchParams.get('reservationId');
  const [reservation, setReservation] = useState<Reservation | null>(null);

  useEffect(() => {
    if (reservationId) {
      getReservation(reservationId)
        .then(setReservation)
        .catch(console.error);
    }
  }, [reservationId]);

  if (!reservation) {
    return (
      <div className="container mx-auto px-4 py-8 text-center">
        <p>Loading confirmation...</p>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-2xl mx-auto">
        <div className="text-center mb-8">
          <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" />
          <h1 className="text-3xl font-bold mb-2">Booking Confirmed!</h1>
          <p className="text-muted-foreground">
            Your reservation has been successfully confirmed
          </p>
        </div>

        <Card className="mb-6">
          <CardHeader>
            <CardTitle>Reservation Details</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex justify-between">
              <span className="text-muted-foreground">Confirmation Number</span>
              <span className="font-mono font-bold">{reservation.id}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Check-in</span>
              <span>{format(new Date(reservation.checkInDate), 'EEEE, MMMM dd, yyyy')}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Check-out</span>
              <span>{format(new Date(reservation.checkOutDate), 'EEEE, MMMM dd, yyyy')}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Guests</span>
              <span>{reservation.guestCount}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Status</span>
              <span className="px-2 py-1 bg-green-100 text-green-800 rounded text-sm">
                {reservation.status}
              </span>
            </div>
            {reservation.specialRequests && (
              <div>
                <span className="text-muted-foreground block mb-1">Special Requests</span>
                <p className="text-sm">{reservation.specialRequests}</p>
              </div>
            )}
            <div className="border-t pt-3 flex justify-between font-bold text-lg">
              <span>Total Paid</span>
              <span>${reservation.totalPrice}</span>
            </div>
          </CardContent>
        </Card>

        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
          <p className="text-sm text-blue-900">
            A confirmation email has been sent to your registered email address 
            with all the details of your reservation.
          </p>
        </div>

        <div className="flex gap-4">
          <Link href="/dashboard" className="flex-1">
            <Button variant="outline" className="w-full">
              View My Bookings
            </Button>
          </Link>
          <Link href="/" className="flex-1">
            <Button className="w-full">
              Book Another Stay
            </Button>
          </Link>
        </div>
      </div>
    </div>
  );
}
