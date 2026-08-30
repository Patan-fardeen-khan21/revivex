import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ShoppingCart, LogOut, User as UserIcon } from 'lucide-react';

export const Navbar: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <Link to="/" className="nav-link" style={{ fontSize: '1.25rem', fontWeight: 'bold', color: 'var(--primary-color)' }}>
        REVIVEX
      </Link>
      
      <div className="nav-links">
        {user ? (
          <>
            <Link to="/products" className="nav-link">Products</Link>
            <Link to="/cart" className="nav-link">
              <ShoppingCart size={20} />
            </Link>
            <Link to="/orders" className="nav-link">Orders</Link>
            <Link to="/admin/recovery" className="nav-link" style={{ color: 'var(--success-color)', fontWeight: 'bold' }}>AI Recovery Demo</Link>
            {user.role === 'ADMIN' && (
              <>
                <Link to="/admin" className="nav-link">Admin</Link>
              </>
            )}
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginLeft: '1rem' }}>
              <UserIcon size={20} />
              <span style={{ fontSize: '0.875rem' }}>{user.name}</span>
            </div>
            <button onClick={handleLogout} className="btn btn-secondary" style={{ padding: '0.25rem 0.5rem', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
              <LogOut size={16} /> Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className="nav-link">Login</Link>
            <Link to="/register" className="btn" style={{ color: 'white' }}>Register</Link>
          </>
        )}
      </div>
    </nav>
  );
};
