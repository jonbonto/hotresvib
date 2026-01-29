'use client';

import { useState, FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import { CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import { createPaymentIntent } from '@/lib/api/reservations';
import { Button } from '@/components/ui/button';
import { toast } from 'sonner';

interface PaymentFormProps {
  reservationId: string;
  amount: number;
}

export function PaymentForm({ reservationId, amount }: PaymentFormProps) {
  const router = useRouter();
  const stripe = useStripe();
  const elements = useElements();
  const [isProcessing, setIsProcessing] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    if (!stripe || !elements) {
      return;
    }

    setIsProcessing(true);

    try {
      // Create payment intent
      const payment = await createPaymentIntent({
        reservationId,
        amount,        currency: 'USD',      });

      // Confirm payment with Stripe
      const cardElement = elements.getElement(CardElement);
      if (!cardElement) {
        throw new Error('Card element not found');
      }

      const { error, paymentIntent } = await stripe.confirmCardPayment(
        payment.clientSecret,
        {
          payment_method: {
            card: cardElement,
          },
        }
      );

      if (error) {
        throw new Error(error.message);
      }

      // Payment confirmed automatically via webhook

      toast.success('Payment successful!');
      router.push(`/booking/confirmation?reservationId=${reservationId}`);
    } catch (error: any) {
      toast.error(error.message || 'Payment failed');
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div className="p-4 border rounded-lg">
        <CardElement
          options={{
            style: {
              base: {
                fontSize: '16px',
                color: '#424770',
                '::placeholder': {
                  color: '#aab7c4',
                },
              },
              invalid: {
                color: '#9e2146',
              },
            },
          }}
        />
      </div>

      <Button
        type="submit"
        className="w-full"
        disabled={!stripe || isProcessing}
      >
        {isProcessing ? 'Processing...' : `Pay $${amount}`}
      </Button>

      <p className="text-xs text-muted-foreground text-center">
        Your payment is secured by Stripe. We never store your card details.
      </p>
    </form>
  );
}
