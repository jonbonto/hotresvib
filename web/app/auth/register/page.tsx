import { Suspense } from 'react';
import RegisterForm from '@/components/auth/RegisterForm';

export default function RegisterPage() {
  return (
    <Suspense fallback={<div className="p-8 text-center">Loading registration...</div>}>
      <RegisterForm />
    </Suspense>
  );
}
