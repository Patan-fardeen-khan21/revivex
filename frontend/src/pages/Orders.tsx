import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';

interface Order {
  id: number;
  status: string;
  totalAmount: number;
  razorpayOrderId: string;
  createdAt: string;
}

export const Orders: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      const response = await api.get('/orders');
      setOrders(response.data);
    } catch (error) {
      console.error('Failed to fetch orders', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div style={{ textAlign: 'center', marginTop: '4rem' }}>Loading orders...</div>;

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Your Orders</h1>
      {orders.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
          <p style={{ color: 'var(--text-secondary)' }}>You have no orders yet.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {orders.map(order => (
            <div key={order.id} className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <h3 style={{ marginBottom: '0.25rem' }}>Order #{order.id}</h3>
                <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                  Date: {new Date(order.createdAt).toLocaleString()}
                </p>
                <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                  Razorpay ID: {order.razorpayOrderId}
                </p>
              </div>
              <div style={{ textAlign: 'right', display: 'flex', flexDirection: 'column', alignItems: 'flex-end' }}>
                <div style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '0.25rem' }}>
                  ₹{order.totalAmount.toFixed(2)}
                </div>
                <span style={{ 
                  padding: '0.25rem 0.75rem', 
                  borderRadius: '9999px', 
                  fontSize: '0.75rem', 
                  fontWeight: 'bold',
                  backgroundColor: order.status === 'PAID' ? '#d1fae5' : order.status === 'FAILED' ? '#fee2e2' : '#fef3c7',
                  color: order.status === 'PAID' ? '#065f46' : order.status === 'FAILED' ? '#991b1b' : '#92400e'
                }}>
                  {order.status}
                </span>
                {order.status !== 'PAID' && order.status !== 'CANCELLED' && (
                  <button 
                    onClick={() => navigate(`/checkout?orderId=${order.id}`)}
                    className="btn" 
                    style={{ fontSize: '0.75rem', padding: '0.25rem 0.5rem', marginTop: '0.5rem', width: '100%' }}
                  >
                    {order.status === 'FAILED' ? 'Retry Payment' : 'Pay Now'}
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
