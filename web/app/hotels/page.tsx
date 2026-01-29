'use client';

import { useSearchParams } from 'next/navigation';
import { useQuery } from '@tanstack/react-query';
import { searchHotels } from '@/lib/api/hotels';
import { HotelCard } from '@/components/HotelCard';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';

export default function HotelsPage() {
  const searchParams = useSearchParams();
  
  const city = searchParams.get('city') || '';
  const checkIn = searchParams.get('checkIn') || '';
  const checkOut = searchParams.get('checkOut') || '';
  const guests = searchParams.get('guests') || '';
  const page = parseInt(searchParams.get('page') || '0');

  const { data, isLoading, error } = useQuery({
    queryKey: ['hotels', city, checkIn, checkOut, guests, page],
    queryFn: () => searchHotels({ 
      city, 
      checkInDate: checkIn, 
      checkOutDate: checkOut,
      page,
      size: 12 
    }),
    enabled: !!city,
  });

  if (!city) {
    return (
      <div className="container mx-auto px-4 py-16 text-center">
        <h1 className="text-2xl font-bold mb-4">No search criteria</h1>
        <p className="text-muted-foreground">Please use the search form to find hotels</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto px-4 py-16 text-center">
        <h1 className="text-2xl font-bold mb-4">Error loading hotels</h1>
        <p className="text-muted-foreground">Please try again later</p>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">
          Hotels in {city}
        </h1>
        {checkIn && checkOut && (
          <p className="text-muted-foreground">
            {checkIn} - {checkOut} · {guests} {guests === '1' ? 'guest' : 'guests'}
          </p>
        )}
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[...Array(6)].map((_, i) => (
            <Skeleton key={i} className="h-80" />
          ))}
        </div>
      ) : (
        <>
          {data?.content && data.content.length > 0 ? (
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                {data.content.map((hotel) => (
                  <HotelCard key={hotel.id} hotel={hotel} />
                ))}
              </div>

              {/* Pagination */}
              {data.totalPages > 1 && (
                <div className="flex justify-center gap-2">
                  <Button
                    variant="outline"
                    disabled={page === 0}
                    onClick={() => {
                      const newParams = new URLSearchParams(searchParams.toString());
                      newParams.set('page', (page - 1).toString());
                      window.location.href = `/hotels?${newParams.toString()}`;
                    }}
                  >
                    Previous
                  </Button>
                  <span className="flex items-center px-4">
                    Page {page + 1} of {data.totalPages}
                  </span>
                  <Button
                    variant="outline"
                    disabled={page >= data.totalPages - 1}
                    onClick={() => {
                      const newParams = new URLSearchParams(searchParams.toString());
                      newParams.set('page', (page + 1).toString());
                      window.location.href = `/hotels?${newParams.toString()}`;
                    }}
                  >
                    Next
                  </Button>
                </div>
              )}
            </>
          ) : (
            <div className="text-center py-16">
              <h2 className="text-xl font-semibold mb-2">No hotels found</h2>
              <p className="text-muted-foreground">
                Try adjusting your search criteria
              </p>
            </div>
          )}
        </>
      )}
    </div>
  );
}
