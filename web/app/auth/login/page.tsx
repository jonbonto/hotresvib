import { Suspense } from 'react';
import LoginForm from '@/components/auth/LoginForm';

export default function LoginPage() {
  return (
    <Suspense fallback={<div className="p-8 text-center">Loading login...</div>}>
      <LoginForm />
    </Suspense>
  );
}
