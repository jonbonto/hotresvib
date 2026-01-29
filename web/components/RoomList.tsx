'use client';

import { useQuery } from '@tanstack/react-query';
import { useSearchParams } from 'next/navigation';
import { searchRooms } from '@/lib/api/hotels';
import { RoomCard } from './RoomCard';
import { Skeleton } from './ui/skeleton';

interface RoomListProps {
  hotelId: string;
}

export function RoomList({ hotelId }: RoomListProps) {
  const searchParams = useSearchParams();
  const checkIn = searchParams.get('checkIn');
  const checkOut = searchParams.get('checkOut');

  const { data, isLoading, error } = useQuery({
    queryKey: ['rooms', hotelId, checkIn, checkOut],
    queryFn: () => searchRooms({ 
      hotelId,
      checkInDate: checkIn || undefined,
      checkOutDate: checkOut || undefined,
    }),
  });

  if (isLoading) {
    return (
      <div className="space-y-6">
        {[...Array(3)].map((_, i) => (
          <Skeleton key={i} className="h-48" />
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-8">
        <p className="text-muted-foreground">Failed to load rooms</p>
      </div>
    );
  }

  if (!data?.content || data.content.length === 0) {
    return (
      <div className="text-center py-8">
        <p className="text-muted-foreground">No rooms available</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {data.content.map((room) => (
        <RoomCard 
          key={room.id} 
          room={room} 
          hotelId={hotelId}
          checkIn={checkIn}
          checkOut={checkOut}
        />
      ))}
    </div>
  );
}
