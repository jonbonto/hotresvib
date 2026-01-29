import { Card, CardContent, CardFooter } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Hotel } from '@/lib/types/hotel';
import Link from 'next/link';
import { MapPin, Star } from 'lucide-react';

interface HotelCardProps {
  hotel: Hotel;
}

export function HotelCard({ hotel }: HotelCardProps) {
  return (
    <Card className="overflow-hidden hover:shadow-lg transition-shadow">
      <div className="aspect-video bg-muted relative">
        {/* Placeholder for hotel image */}
        <div className="absolute inset-0 flex items-center justify-center text-muted-foreground">
          <Star className="h-12 w-12" />
        </div>
        {hotel.isFeatured && (
          <div className="absolute top-2 right-2 bg-primary text-primary-foreground px-2 py-1 rounded text-xs font-semibold">
            Featured
          </div>
        )}
      </div>
      
      <CardContent className="p-4">
        <h3 className="font-semibold text-lg mb-2">{hotel.name}</h3>
        <div className="flex items-center text-sm text-muted-foreground mb-2">
          <MapPin className="h-4 w-4 mr-1" />
          {hotel.city}, {hotel.country}
        </div>
        
        {hotel.minPrice && (
          <p className="text-sm">
            From <span className="font-bold text-lg">${hotel.minPrice}</span>/night
          </p>
        )}
        
        {hotel.roomCount && (
          <p className="text-xs text-muted-foreground mt-1">
            {hotel.roomCount} rooms available
          </p>
        )}
      </CardContent>
      
      <CardFooter className="p-4 pt-0">
        <Link href={`/hotels/${hotel.id}`} className="w-full">
          <Button className="w-full" variant="outline">
            View Details
          </Button>
        </Link>
      </CardFooter>
    </Card>
  );
}
