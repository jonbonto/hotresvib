'use client';

import { useEffect, useState } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { loadStripe } from '@stripe/stripe-js';
import { Elements } from '@stripe/react-stripe-js';
import { PaymentForm } from '@/components/PaymentForm';
import { getReservation } from '@/lib/api/reservations';
import { Reservation } from '@/lib/types/reservation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { format } from 'date-fns';

const stripePromise = loadStripe(process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY || '');

export default function PaymentPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const reservationId = searchParams.get('reservationId');

  const [reservation, setReservation] = useState<Reservation | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!reservationId) {
      router.push('/');
      return;
    }

    getReservation(reservationId)
      .then(setReservation)
      .catch((error) => {
        console.error('Failed to load reservation:', error);
        router.push('/');
      })
      .finally(() => setLoading(false));
  }, [reservationId, router]);

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <Skeleton className="h-96" />
      </div>
    );
  }

  if (!reservation) {
    return null;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-2xl mx-auto">
        <h1 className="text-3xl font-bold mb-8">Complete Payment</h1>

        <div className="grid gap-6 mb-8">
          {/* Reservation Summary */}
          <Card>
            <CardHeader>
              <CardTitle>Reservation Summary</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Reservation ID</span>
                <span className="font-mono">{reservation.id}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Check-in</span>
                <span>{format(new Date(reservation.checkInDate), 'MMM dd, yyyy')}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Check-out</span>
                <span>{format(new Date(reservation.checkOutDate), 'MMM dd, yyyy')}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Guests</span>
                <span>{reservation.guestCount}</span>
              </div>
              <div className="border-t pt-3 flex justify-between font-bold text-lg">
                <span>Total Amount</span>
                <span>${reservation.totalPrice}</span>
              </div>
            </CardContent>
          </Card>

          {/* Payment Form */}
          <Card>
            <CardHeader>
              <CardTitle>Payment Details</CardTitle>
            </CardHeader>
            <CardContent>
              <Elements stripe={stripePromise}>
                <PaymentForm 
                  reservationId={reservation.id} 
                  amount={reservation.totalPrice}
                />
              </Elements>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
