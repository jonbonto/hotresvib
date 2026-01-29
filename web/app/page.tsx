import { SearchForm } from '@/components/SearchForm';
import { HotelCard } from '@/components/HotelCard';
import { getFeaturedHotels } from '@/lib/api/hotels';
import { Hotel } from '@/lib/types/hotel';

export default async function HomePage() {
  let featuredHotels: Hotel[] = [];
  
  try {
    const response = await getFeaturedHotels();
    featuredHotels = response.content;
  } catch (error) {
    console.error('Failed to load featured hotels:', error);
  }

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-primary/10 to-primary/5 py-20">
        <div className="container mx-auto px-4">
          <div className="text-center mb-12">
            <h1 className="text-4xl md:text-5xl font-bold mb-4">
              Find Your Perfect Stay
            </h1>
            <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
              Discover amazing hotels worldwide at the best prices
            </p>
          </div>
          
          <div className="flex justify-center">
            <SearchForm />
          </div>
        </div>
      </section>

      {/* Featured Hotels */}
      {featuredHotels.length > 0 && (
        <section className="py-16">
          <div className="container mx-auto px-4">
            <h2 className="text-3xl font-bold mb-8">Featured Hotels</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {featuredHotels.map((hotel) => (
                <HotelCard key={hotel.id} hotel={hotel} />
              ))}
            </div>
          </div>
        </section>
      )}

      {/* Popular Destinations */}
      <section className="py-16 bg-muted/30">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl font-bold mb-8">Popular Destinations</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {['Paris, France', 'Tokyo, Japan', 'New York, USA'].map((city) => (
              <div
                key={city}
                className="aspect-video bg-muted rounded-lg flex items-center justify-center cursor-pointer hover:shadow-lg transition-shadow"
              >
                <h3 className="text-xl font-semibold">{city}</h3>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}
