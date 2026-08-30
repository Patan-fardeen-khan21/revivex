import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';
import { Trash2 } from 'lucide-react';

interface CartItem {
  id: number;
  product: {
    id: number;
    name: string;
    price: number;
    imageUrl: string;
  };
  quantity: number;
}

interface CartDto {
  id: number;
  items: CartItem[];
}

export const Cart: React.FC = () => {
  const [cart, setCart] = useState<CartDto | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchCart();
  }, []);

  const fetchCart = async () => {
    try {
      const response = await api.get('/cart');
      setCart(response.data);
    } catch (error) {
      console.error('Failed to fetch cart', error);
    } finally {
      setLoading(false);
    }
  };

  const removeItem = async (itemId: number) => {
    try {
      const response = await api.delete(`/cart/items/${itemId}`);
      setCart(response.data);
    } catch (error) {
      console.error('Failed to remove item', error);
    }
  };

  const calculateTotal = () => {
    if (!cart) return 0;
    return cart.items.reduce((total, item) => total + (item.product.price * item.quantity), 0);
  };

  if (loading) return <div style={{ textAlign: 'center', marginTop: '4rem' }}>Loading cart...</div>;

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Your Cart</h1>
      
      {!cart || cart.items.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
          <p style={{ marginBottom: '1rem', color: 'var(--text-secondary)' }}>Your cart is empty.</p>
          <button className="btn" onClick={() => navigate('/products')}>Browse Products</button>
        </div>
      ) : (
        <div style={{ display: 'flex', gap: '2rem', flexWrap: 'wrap' }}>
          <div style={{ flex: '1 1 60%' }}>
            {cart.items.map(item => (
              <div key={item.id} className="card" style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                <img src={item.product.imageUrl} alt={item.product.name} style={{ width: '80px', height: '80px', objectFit: 'cover', borderRadius: '0.25rem' }} />
                <div style={{ flex: 1 }}>
                  <h3 style={{ fontSize: '1.125rem' }}>{item.product.name}</h3>
                  <p style={{ color: 'var(--text-secondary)' }}>₹{item.product.price} x {item.quantity}</p>
                </div>
                <div style={{ fontWeight: 'bold' }}>
                  ₹{(item.product.price * item.quantity).toFixed(2)}
                </div>
                <button onClick={() => removeItem(item.id)} className="btn-secondary" style={{ padding: '0.5rem', color: 'var(--error-color)', border: 'none' }}>
                  <Trash2 size={20} />
                </button>
              </div>
            ))}
          </div>
          
          <div style={{ flex: '1 1 30%' }}>
            <div className="card" style={{ position: 'sticky', top: '2rem' }}>
              <h2 style={{ marginBottom: '1.5rem' }}>Order Summary</h2>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
                <span>Subtotal</span>
                <span>₹{calculateTotal().toFixed(2)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem', fontWeight: 'bold', fontSize: '1.25rem', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
                <span>Total</span>
                <span>₹{calculateTotal().toFixed(2)}</span>
              </div>
              <button onClick={() => navigate('/checkout')} className="btn" style={{ width: '100%', fontSize: '1.125rem', padding: '0.75rem' }}>
                Proceed to Checkout
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
