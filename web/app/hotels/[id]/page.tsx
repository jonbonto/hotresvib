import { notFound } from 'next/navigation';
import { getHotel } from '@/lib/api/hotels';
import { RoomList } from '@/components/RoomList';
import { MapPin, Star } from 'lucide-react';
import { Badge } from '@/components/ui/badge';

export default async function HotelDetailsPage({
  params,
}: {
  params: { id: string };
}) {
  let hotel;
  
  try {
    hotel = await getHotel(params.id);
  } catch (error) {
    notFound();
  }

  return (
    <div className="min-h-screen">
      {/* Hotel Header */}
      <div className="bg-muted/30 py-8">
        <div className="container mx-auto px-4">
          <div className="flex items-start justify-between mb-4">
            <div>
              <h1 className="text-4xl font-bold mb-2">{hotel.name}</h1>
              <div className="flex items-center gap-2 text-muted-foreground">
                <MapPin className="w-4 h-4" />
                <span>{hotel.city}, {hotel.country}</span>
              </div>
            </div>
            {hotel.isFeatured && (
              <Badge variant="secondary" className="bg-yellow-100 text-yellow-800">
                <Star className="w-3 h-3 mr-1" />
                Featured
              </Badge>
            )}
          </div>
          
          {hotel.description && <p className="text-lg">{hotel.description}</p>}
        </div>
      </div>

      {/* Hotel Image Placeholder */}
      <div className="container mx-auto px-4 py-8">
        <div className="aspect-video bg-muted rounded-lg mb-8 flex items-center justify-center">
          <p className="text-muted-foreground">Hotel Image</p>
        </div>

        {/* Hotel Details */}
        <div className="grid md:grid-cols-3 gap-8 mb-12">
          {hotel.address && (
            <div>
              <h3 className="font-semibold mb-2">Address</h3>
              <p className="text-sm text-muted-foreground">
                {hotel.address.street}<br />
                {hotel.address.city}, {hotel.address.state}<br />
                {hotel.address.country} {hotel.address.zipCode}
              </p>
            </div>
          )}
          {hotel.contactInfo && (
            <div>
              <h3 className="font-semibold mb-2">Contact</h3>
              <p className="text-sm text-muted-foreground">
                Phone: {hotel.contactInfo.phone}<br />
                Email: {hotel.contactInfo.email}
              </p>
            </div>
          )}
          <div>
            <h3 className="font-semibold mb-2">Total Rooms</h3>
            <p className="text-sm text-muted-foreground">
              {hotel.totalRooms || hotel.roomCount || 0} rooms available
            </p>
          </div>
        </div>

        {/* Rooms Section */}
        <div>
          <h2 className="text-3xl font-bold mb-6">Available Rooms</h2>
          <RoomList hotelId={params.id} />
        </div>
      </div>
    </div>
  );
}
