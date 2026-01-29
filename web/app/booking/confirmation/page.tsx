import React, { Suspense } from 'react';
import ConfirmationClient from '@/components/confirmation/ConfirmationClient';

export default function ConfirmationPage() {
  return (
    <Suspense fallback={
      <div className="container mx-auto px-4 py-8 text-center">Loading confirmation...</div>
    }>
      <ConfirmationClient />
    </Suspense>
  );
}
