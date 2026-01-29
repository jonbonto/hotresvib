import Link from 'next/link';
import { Room } from '@/lib/types/hotel';
import { Card, CardContent, CardFooter } from './ui/card';
import { Button } from './ui/button';
import { Users, Bed } from 'lucide-react';

interface RoomCardProps {
  room: Room;
  hotelId: string;
  checkIn: string | null;
  checkOut: string | null;
}

export function RoomCard({ room, hotelId, checkIn, checkOut }: RoomCardProps) {
  const bookingParams = new URLSearchParams();
  if (checkIn) bookingParams.set('checkIn', checkIn);
  if (checkOut) bookingParams.set('checkOut', checkOut);

  return (
    <Card>
      <CardContent className="p-6">
        <div className="flex gap-6">
          <div className="w-48 h-32 bg-muted rounded-lg flex items-center justify-center flex-shrink-0">
            <p className="text-muted-foreground text-sm">Room Image</p>
          </div>
          
          <div className="flex-1">
            <div className="flex items-start justify-between mb-2">
              <div>
                <h3 className="text-xl font-semibold mb-1">{room.type}</h3>
                <p className="text-sm text-muted-foreground mb-3">
                  {room.description}
                </p>
              </div>
              <div className="text-right">
                <p className="text-2xl font-bold">${room.baseRate}</p>
                <p className="text-sm text-muted-foreground">per night</p>
              </div>
            </div>
            
            <div className="flex gap-6 text-sm text-muted-foreground mb-4">
              <div className="flex items-center gap-1">
                <Users className="w-4 h-4" />
                <span>Max {room.capacity} guests</span>
              </div>
              <div className="flex items-center gap-1">
                <Bed className="w-4 h-4" />
                <span>{room.bedType}</span>
              </div>
            </div>

            {room.amenities && room.amenities.length > 0 && (
              <div className="flex flex-wrap gap-2">
                {room.amenities.map((amenity, index) => (
                  <span 
                    key={index}
                    className="px-2 py-1 bg-muted rounded text-xs"
                  >
                    {amenity}
                  </span>
                ))}
              </div>
            )}
          </div>
        </div>
      </CardContent>
      
      <CardFooter className="bg-muted/30 px-6 py-4">
        <div className="flex items-center justify-between w-full">
          <div className="text-sm text-muted-foreground">
            {room.available !== undefined && room.available > 0 ? (
              <span className="text-green-600 font-medium">
                {room.available} rooms available
              </span>
            ) : (
              <span className="text-red-600 font-medium">
                No availability
              </span>
            )}
          </div>
          <Link 
            href={`/hotels/${hotelId}/rooms/${room.id}?${bookingParams.toString()}`}
          >
            <Button disabled={!room.available || room.available === 0}>
              {room.available && room.available > 0 ? 'Book Now' : 'Unavailable'}
            </Button>
          </Link>
        </div>
      </CardFooter>
    </Card>
  );
}
