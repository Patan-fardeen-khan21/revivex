import React, { useEffect, useState } from 'react';
import api from '../api';

interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
}

export const Products: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      const response = await api.get('/products');
      setProducts(response.data);
    } catch (error) {
      console.error('Failed to fetch products', error);
    } finally {
      setLoading(false);
    }
  };

  const addToCart = async (productId: number) => {
    try {
      await api.post('/cart/items', { productId, quantity: 1 });
      alert('Added to cart!');
    } catch (error) {
      console.error('Failed to add to cart', error);
    }
  };

  if (loading) return <div style={{ textAlign: 'center', marginTop: '4rem' }}>Loading products...</div>;

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Available Products</h1>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '2rem' }}>
        {products.map(product => (
          <div key={product.id} className="card" style={{ display: 'flex', flexDirection: 'column' }}>
            <img src={product.imageUrl} alt={product.name} style={{ width: '100%', height: '200px', objectFit: 'cover', borderRadius: '0.375rem', marginBottom: '1rem' }} />
            <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>{product.name}</h3>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '1rem', flex: 1 }}>{product.description}</p>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 'auto' }}>
              <span style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>₹{product.price}</span>
              <button onClick={() => addToCart(product.id)} className="btn">Add to Cart</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
