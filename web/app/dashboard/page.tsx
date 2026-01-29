'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useQuery } from '@tanstack/react-query';
import { getUserReservations, cancelReservation } from '@/lib/api/reservations';
import { useAuth } from '@/lib/contexts/AuthContext';
import { Reservation } from '@/lib/types/reservation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { format } from 'date-fns';
import { toast } from 'sonner';
import { Calendar, MapPin, Users } from 'lucide-react';

export default function DashboardPage() {
  const router = useRouter();
  const { isAuthenticated, user } = useAuth();

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/auth/login?redirect=/dashboard');
    }
  }, [isAuthenticated, router]);

  const { data: reservations, isLoading, refetch } = useQuery({
    queryKey: ['user-reservations'],
    queryFn: () => getUserReservations(user!.id),
    enabled: isAuthenticated && !!user,
  });

  const handleCancelReservation = async (reservationId: string) => {
    if (!confirm('Are you sure you want to cancel this reservation?')) {
      return;
    }

    try {
      await cancelReservation(reservationId);
      toast.success('Reservation cancelled successfully');
      refetch();
    } catch (error: any) {
      toast.error(error.message || 'Failed to cancel reservation');
    }
  };

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">My Bookings</h1>
        <p className="text-muted-foreground">
          Welcome back, {user?.name || 'Guest'}
        </p>
      </div>

      {isLoading ? (
        <div className="space-y-4">
          {[...Array(3)].map((_, i) => (
            <Skeleton key={i} className="h-48" />
          ))}
        </div>
      ) : reservations && reservations.length > 0 ? (
        <div className="space-y-4">
          {reservations.map((reservation) => (
            <Card key={reservation.id}>
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div>
                    <CardTitle className="mb-1">
                      Reservation #{reservation.id.slice(0, 8)}
                    </CardTitle>
                    <div className="flex items-center gap-4 text-sm text-muted-foreground">
                      <div className="flex items-center gap-1">
                        <Calendar className="w-4 h-4" />
                        <span>
                          {format(new Date(reservation.checkInDate), 'MMM dd')} -{' '}
                          {format(new Date(reservation.checkOutDate), 'MMM dd, yyyy')}
                        </span>
                      </div>
                      <div className="flex items-center gap-1">
                        <Users className="w-4 h-4" />
                        <span>{reservation.guestCount} guests</span>
                      </div>
                    </div>
                  </div>
                  <div className="text-right">
                    <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                      reservation.status === 'CONFIRMED' ? 'bg-green-100 text-green-800' :
                      reservation.status === 'CANCELLED' ? 'bg-red-100 text-red-800' :
                      'bg-blue-100 text-blue-800'
                    }`}>
                      {reservation.status}
                    </span>
                    <p className="text-lg font-bold mt-2">${reservation.totalPrice}</p>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                {reservation.specialRequests && (
                  <p className="text-sm text-muted-foreground mb-4">
                    Special Requests: {reservation.specialRequests}
                  </p>
                )}
                <div className="flex gap-2">
                  {reservation.status === 'DRAFT' && (
                    <Button
                      variant="outline"
                      onClick={() => router.push(`/booking/payment?reservationId=${reservation.id}`)}
                    >
                      Complete Payment
                    </Button>
                  )}
                  {(reservation.status === 'CONFIRMED' || reservation.status === 'PENDING_PAYMENT') && (
                    <Button
                      variant="destructive"
                      onClick={() => handleCancelReservation(reservation.id)}
                    >
                      Cancel Booking
                    </Button>
                  )}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : (
        <Card>
          <CardContent className="py-16 text-center">
            <h2 className="text-xl font-semibold mb-2">No bookings yet</h2>
            <p className="text-muted-foreground mb-4">
              Start exploring and book your perfect stay
            </p>
            <Button onClick={() => router.push('/')}>
              Browse Hotels
            </Button>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
