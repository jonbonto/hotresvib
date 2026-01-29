# HotResvib Frontend

Next.js 14 frontend application for the HotResvib hotel reservation system.

## Tech Stack

- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **Styling**: TailwindCSS v4
- **UI Components**: shadcn/ui
- **State Management**: React Query, React Context
- **Forms**: React Hook Form with Zod validation
- **Payment**: Stripe Elements
- **Notifications**: Sonner

## Getting Started

### Prerequisites
- Node.js 18+
- Backend API running on `http://localhost:8080`

### Installation

```bash
# Install dependencies
npm install

# Configure environment variables
cp .env.local.example .env.local
# Edit .env.local with your API URL and Stripe key

# Run development server
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

## Environment Variables

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY=pk_test_your_key_here
```

## Features

### ✅ Implemented
- User authentication (login, register, JWT)
- Hotel search and discovery
- Room booking flow
- Stripe payment integration
- User dashboard (view/cancel bookings)
- Profile management
- Responsive design

### 🚧 Future Enhancements
- Dark mode
- Image uploads
- Reviews and ratings
- Multi-language support
- Advanced filtering
- Map integration

## Project Structure

```
web/
├── app/                 # Next.js pages
│   ├── auth/           # Login & register
│   ├── booking/        # Payment & confirmation
│   ├── hotels/         # Search, details, rooms
│   ├── dashboard/      # User bookings
│   └── profile/        # User profile
├── components/         # React components
│   ├── ui/            # shadcn/ui components
│   └── *.tsx          # Custom components
├── lib/
│   ├── api/           # API client
│   ├── contexts/      # React contexts
│   ├── types/         # TypeScript types
│   └── validations/   # Zod schemas
└── public/            # Static assets
```

## API Integration

Communicates with backend via REST API:
- Authentication endpoints
- Hotel search and room availability
- Reservation management
- Stripe payment processing

All requests include JWT authentication token.

## Build & Deploy

```bash
# Production build
npm run build

# Start production server
npm start
```

## License

MIT License - HotResvib Team © 2026
