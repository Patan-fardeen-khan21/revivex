import React, { useEffect, useState } from 'react';
import api from '../api';

export const AdminDashboard: React.FC = () => {
  const [orders, setOrders] = useState<any[]>([]);
  const [payments, setPayments] = useState<any[]>([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [ordersRes, paymentsRes] = await Promise.all([
          api.get('/admin/orders'),
          api.get('/admin/payments')
        ]);
        setOrders(ordersRes.data);
        setPayments(paymentsRes.data);
      } catch (err) {
        console.error('Failed to fetch admin data', err);
      }
    };
    fetchData();
  }, []);

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Admin Dashboard</h1>
      
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
        <div className="card">
          <h2 style={{ marginBottom: '1rem' }}>All Orders ({orders.length})</h2>
          <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
            {orders.map(order => (
              <div key={order.id} style={{ padding: '0.5rem 0', borderBottom: '1px solid var(--border-color)' }}>
                <div><strong>Order #{order.id}</strong> - User: {order.user.email}</div>
                <div style={{ fontSize: '0.875rem' }}>Amount: ₹{order.totalAmount} | Status: {order.status}</div>
              </div>
            ))}
          </div>
        </div>
        
        <div className="card">
          <h2 style={{ marginBottom: '1rem' }}>All Payments ({payments.length})</h2>
          <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
            {payments.map(payment => (
              <div key={payment.id} style={{ padding: '0.5rem 0', borderBottom: '1px solid var(--border-color)' }}>
                <div><strong>Payment #{payment.id}</strong> - Order #{payment.order.id}</div>
                <div style={{ fontSize: '0.875rem' }}>Amount: ₹{payment.amount} | Status: {payment.status}</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>RZP: {payment.razorpayPaymentId}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
