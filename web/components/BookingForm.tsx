'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { format, differenceInDays } from 'date-fns';
import { toast } from 'sonner';
import { createReservation } from '@/lib/api/reservations';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';

const bookingSchema = z.object({
  checkInDate: z.string().min(1, 'Check-in date is required'),
  checkOutDate: z.string().min(1, 'Check-out date is required'),
  guestCount: z.number().min(1).max(10),
  specialRequests: z.string().optional(),
}).refine((data) => {
  const checkIn = new Date(data.checkInDate);
  const checkOut = new Date(data.checkOutDate);
  return checkOut > checkIn;
}, {
  message: 'Check-out must be after check-in',
  path: ['checkOutDate'],
});

type BookingFormData = z.infer<typeof bookingSchema>;

interface BookingFormProps {
  roomId: string;
  hotelId: string;
  defaultCheckIn?: string;
  defaultCheckOut?: string;
  baseRate: number;
}

export function BookingForm({
  roomId,
  hotelId,
  defaultCheckIn,
  defaultCheckOut,
  baseRate,
}: BookingFormProps) {
  const router = useRouter();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [totalPrice, setTotalPrice] = useState<number | null>(null);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<BookingFormData>({
    resolver: zodResolver(bookingSchema),
    defaultValues: {
      checkInDate: defaultCheckIn || '',
      checkOutDate: defaultCheckOut || '',
      guestCount: 2,
      specialRequests: '',
    },
  });

  const checkInDate = watch('checkInDate');
  const checkOutDate = watch('checkOutDate');

  // Calculate estimated price
  const nights = checkInDate && checkOutDate 
    ? differenceInDays(new Date(checkOutDate), new Date(checkInDate))
    : 0;
  const estimatedTotal = nights > 0 ? nights * baseRate : 0;

  const onSubmit = async (data: BookingFormData) => {
    setIsSubmitting(true);

    try {
      const reservation = await createReservation({
        roomId,
        checkInDate: data.checkInDate,
        checkOutDate: data.checkOutDate,
        guestCount: data.guestCount,
        specialRequests: data.specialRequests,
      });

      toast.success('Reservation created! Proceeding to payment...');
      
      // Redirect to payment page
      router.push(`/booking/payment?reservationId=${reservation.id}`);
    } catch (error: any) {
      toast.error(error.message || 'Failed to create reservation');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      <div className="grid grid-cols-2 gap-4">
        <div>
          <Label htmlFor="checkInDate">Check-in Date</Label>
          <Input
            id="checkInDate"
            type="date"
            {...register('checkInDate')}
            min={format(new Date(), 'yyyy-MM-dd')}
          />
          {errors.checkInDate && (
            <p className="text-sm text-red-500 mt-1">{errors.checkInDate.message}</p>
          )}
        </div>

        <div>
          <Label htmlFor="checkOutDate">Check-out Date</Label>
          <Input
            id="checkOutDate"
            type="date"
            {...register('checkOutDate')}
            min={checkInDate || format(new Date(), 'yyyy-MM-dd')}
          />
          {errors.checkOutDate && (
            <p className="text-sm text-red-500 mt-1">{errors.checkOutDate.message}</p>
          )}
        </div>
      </div>

      <div>
        <Label htmlFor="guestCount">Number of Guests</Label>
        <Input
          id="guestCount"
          type="number"
          min={1}
          max={10}
          {...register('guestCount', { valueAsNumber: true })}
        />
        {errors.guestCount && (
          <p className="text-sm text-red-500 mt-1">{errors.guestCount.message}</p>
        )}
      </div>

      <div>
        <Label htmlFor="specialRequests">Special Requests (Optional)</Label>
        <Textarea
          id="specialRequests"
          {...register('specialRequests')}
          placeholder="Any special requests or preferences..."
          rows={3}
        />
      </div>

      {nights > 0 && (
        <div className="bg-muted p-4 rounded-lg space-y-2">
          <div className="flex justify-between text-sm">
            <span>Base rate per night</span>
            <span>${baseRate}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span>Number of nights</span>
            <span>{nights}</span>
          </div>
          <div className="border-t pt-2 flex justify-between font-bold">
            <span>Estimated Total</span>
            <span>${estimatedTotal}</span>
          </div>
          <p className="text-xs text-muted-foreground">
            Final price may vary based on seasonal rates and applicable taxes
          </p>
        </div>
      )}

      <Button type="submit" className="w-full" disabled={isSubmitting || nights <= 0}>
        {isSubmitting ? 'Creating reservation...' : 'Continue to Payment'}
      </Button>
    </form>
  );
}
