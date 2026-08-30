import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';

declare global {
  interface Window {
    Razorpay: any;
  }
}

export const Checkout: React.FC = () => {
  const navigate = useNavigate();
  const [intervention, setIntervention] = useState<any>(null);
  const [orderAmount, setOrderAmount] = useState<number | null>(null);
  const [razorpayOrderId, setRazorpayOrderId] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // Parse orderId from query parameter
  const orderId = new URLSearchParams(window.location.search).get('orderId');

  useEffect(() => {
    // 1. Fetch pending interventions for this user
    api.get('/orders/intervention').then(res => {
      if (res.data) setIntervention(res.data);
    }).catch(console.error);

    // 2. Fetch existing order details if orderId is provided in URL
    if (orderId) {
      api.get(`/orders/${orderId}`).then(res => {
        setOrderAmount(res.data.totalAmount);
        setRazorpayOrderId(res.data.razorpayOrderId);
      }).catch(console.error);
    }
  }, [orderId]);

  const handlePayment = async (simulateSuccess: boolean) => {
    setLoading(true);
    try {
      let currentOrderId = orderId;
      let currentRazorpayOrderId = razorpayOrderId;

      // 1. If checking out from Cart (new order)
      if (!currentOrderId) {
        const orderRes = await api.post('/orders', {
          discountCode: (intervention && intervention.interventionType === '15%_DISCOUNT_OFFER') 
            ? intervention.discountCode 
            : null
        });
        currentOrderId = orderRes.data.id;
        currentRazorpayOrderId = orderRes.data.razorpayOrderId;
      } else {
        // If retrying an existing order and we have a valid failure/overdue code
        if (intervention && (intervention.interventionType === 'PAYMENT_FAILURE_RECOVERY' || intervention.interventionType === 'OVERDUE_RECEIVABLE_REMINDER')) {
          const discountRes = await api.post(`/orders/${currentOrderId}/apply-discount`, {
            discountCode: intervention.discountCode
          });
          setOrderAmount(discountRes.data.totalAmount);
        }
      }

      // 2. Fake network processing delay
      await new Promise(resolve => setTimeout(resolve, 1500));

      // 3. Complete payment verification (Success vs Failure)
      if (simulateSuccess) {
        await api.post('/payments/verify', {
          razorpayOrderId: currentRazorpayOrderId,
          razorpayPaymentId: 'pay_MOCK' + Date.now(),
          razorpaySignature: 'mock_signature'
        });
        alert('Payment Successful!');
      } else {
        await api.post('/payments/fail', {
          razorpayOrderId: currentRazorpayOrderId,
          razorpayPaymentId: 'pay_MOCK_FAIL_' + Date.now(),
          razorpaySignature: 'failed_signature'
        });
        alert('Payment Failed! Card declined by issuer.');
      }

      navigate('/orders');
    } catch (error) {
      console.error('Payment processing failed', error);
      alert('Checkout failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const getInterventionBanner = () => {
    if (!intervention) return null;

    switch (intervention.interventionType) {
      case '15%_DISCOUNT_OFFER':
        return (
          <div style={{ padding: '1.25rem', background: '#d1fae5', color: '#065f46', borderRadius: '8px', marginBottom: '2rem', border: '1px solid #10b981', textAlign: 'left' }}>
            <strong>🤖 AI Recovery Agent: checkout abandonment detected</strong>
            <p style={{ marginTop: '0.25rem', fontSize: '0.95rem' }}>
              We noticed you hesitated! Apply code <strong style={{ textDecoration: 'underline' }}>{intervention.discountCode}</strong> right now to get <strong>15% off</strong> your entire cart!
            </p>
          </div>
        );
      case 'PAYMENT_FAILURE_RECOVERY':
        return (
          <div style={{ padding: '1.25rem', background: '#fee2e2', color: '#991b1b', borderRadius: '8px', marginBottom: '2rem', border: '1px solid #f87171', textAlign: 'left' }}>
            <strong>🤖 AI Recovery Agent: payment degradation detected</strong>
            <p style={{ marginTop: '0.25rem', fontSize: '0.95rem' }}>
              Your previous transaction failed! Use code <strong style={{ textDecoration: 'underline' }}>{intervention.discountCode}</strong> to get <strong>10% off</strong> and retry this payment securely.
            </p>
          </div>
        );
      case 'OVERDUE_RECEIVABLE_REMINDER':
        return (
          <div style={{ padding: '1.25rem', background: '#fef3c7', color: '#92400e', borderRadius: '8px', marginBottom: '2rem', border: '1px solid #fbbf24', textAlign: 'left' }}>
            <strong>🤖 AI Recovery Agent: overdue receivable reminder</strong>
            <p style={{ marginTop: '0.25rem', fontSize: '0.95rem' }}>
              Invoice is outstanding! Complete payment using code <strong style={{ textDecoration: 'underline' }}>{intervention.discountCode}</strong> for an early-settlement incentive of <strong>5% off</strong>.
            </p>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div style={{ maxWidth: '600px', margin: '4rem auto', textAlign: 'center' }}>
      <div className="card">
        <h1 style={{ marginBottom: '1.5rem' }}>{orderId ? `Pay Order #${orderId}` : 'Checkout'}</h1>
        
        {getInterventionBanner()}

        <p style={{ marginBottom: '2rem', color: 'var(--text-secondary)' }}>
          {orderId 
            ? `You are paying an existing order in your account. Total amount: ₹${orderAmount?.toFixed(2) || '...'}`
            : 'You are about to place an order. Click a button below to pay securely.'
          }
        </p>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <button 
            onClick={() => handlePayment(true)} 
            disabled={loading}
            className="btn" 
            style={{ fontSize: '1.25rem', padding: '1rem 2rem', backgroundColor: 'var(--success-color)' }}
          >
            {loading ? 'Processing...' : 'Pay with Razorpay (Success Demo)'}
          </button>

          <button 
            onClick={() => handlePayment(false)} 
            disabled={loading}
            className="btn" 
            style={{ fontSize: '1.25rem', padding: '1rem 2rem', backgroundColor: 'var(--error-color)' }}
          >
            {loading ? 'Processing...' : 'Simulate Payment Failure'}
          </button>
        </div>
      </div>
    </div>
  );
};
