import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthService from '../../services/AuthService';
import { CheckCircle, AlertCircle, X, Home, Eye, EyeOff } from 'lucide-react';
import { APP_TITLE } from '../../config/menuConfig';
import '../../styles/main.css';
import './Login.css';

function Login() {
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [toasts, setToasts] = useState([]);
  const navigate = useNavigate();

  const showToast = (type, title, text) => {
    const id = Date.now();
    setToasts(prev => [...prev, { id, type, title, text, hiding: false }]);
    setTimeout(() => {
      setToasts(prev => prev.map(t => t.id === id ? { ...t, hiding: true } : t));
      setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 300);
    }, 4000);
  };

  const dismissToast = (id) => {
    setToasts(prev => prev.map(t => t.id === id ? { ...t, hiding: true } : t));
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 300);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await AuthService.login(formData.email, formData.password);
      
      if (response.requiresPasswordChange) {
        navigate('/change-password');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      showToast('error', 'Error al iniciar sesión',
        err.response?.data?.message || 'Credenciales incorrectas. Intenta nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <div className="toast-container">
        {toasts.map(toast => (
          <div key={toast.id} className={`toast toast-${toast.type}${toast.hiding ? ' toast-hiding' : ''}`}>
            <span className={`toast-icon toast-icon-${toast.type}`}>
              {toast.type === 'success' ? (
                <CheckCircle size={20} strokeWidth={2.5} />
              ) : (
                <AlertCircle size={20} strokeWidth={2.5} />
              )}
            </span>
            <div className="toast-body">
              <div className={`toast-title toast-title-${toast.type}`}>{toast.title}</div>
              <div className="toast-text">{toast.text}</div>
            </div>
            <button className="toast-close" onClick={() => dismissToast(toast.id)} aria-label="Cerrar">
              <X size={16} />
            </button>
          </div>
        ))}
      </div>

      <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <Home className="company-icon" size={48} />
          <h1>{APP_TITLE}</h1>
        </div>
        <h2>Iniciar Sesión</h2>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              placeholder="tu@email.com"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              maxLength={100}
              required
            />
          </div>

          <div className="form-group">
            <label>Contraseña</label>
            <div className="password-wrapper">
              <input
                type={showPassword ? 'text' : 'password'}
                placeholder="••••••••"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                required
                minLength={6}
                maxLength={50}
              />
              <button
                type="button"
                className="password-toggle"
                onClick={() => setShowPassword(!showPassword)}
                aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          <button type="submit" className="btn btn-primary btn-block btn-lg" disabled={loading}>
            {loading ? 'Cargando...' : 'Iniciar Sesión'}
          </button>
        </form>
      </div>
      </div>
    </>
  );
}

export default Login;
