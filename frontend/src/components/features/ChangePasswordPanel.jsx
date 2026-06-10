import React, { useState } from 'react';
import { Eye, EyeOff, CheckCircle, AlertCircle, X } from 'lucide-react';
import AuthService from '../../services/AuthService';
import '../../styles/main.css';

function ChangePasswordPanel() {
  const [formData, setFormData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [loading, setLoading] = useState(false);
  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [toasts, setToasts] = useState([]);

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

    if (formData.newPassword !== formData.confirmPassword) {
      showToast('error', 'Error de validación', 'Las contraseñas nuevas no coinciden');
      return;
    }

    if (formData.newPassword.length < 6) {
      showToast('error', 'Error de validación', 'La contraseña debe tener al menos 6 caracteres');
      return;
    }

    setLoading(true);
    try {
      await AuthService.changePassword(formData.currentPassword, formData.newPassword);
      showToast('success', 'Contraseña actualizada', 'Tu contraseña fue cambiada exitosamente');
      setFormData({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      showToast('error', 'Error al cambiar contraseña',
        err.response?.data?.message || 'Error al cambiar la contraseña. Intenta nuevamente.');
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

      <div className="card">
        <h2 className="card-header">Cambiar Contraseña</h2>
        <div className="card-body">
          <form onSubmit={handleSubmit} style={{ maxWidth: '420px' }}>
            <div className="form-group">
              <label>Contraseña Actual</label>
              <div className="password-wrapper">
                <input
                  type={showCurrent ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={formData.currentPassword}
                  onChange={(e) => setFormData({ ...formData, currentPassword: e.target.value })}
                  required
                  minLength={6}
                  maxLength={50}
                  autoComplete="current-password"
                />
                <button type="button" className="password-toggle" onClick={() => setShowCurrent(!showCurrent)} aria-label={showCurrent ? 'Ocultar' : 'Mostrar'}>
                  {showCurrent ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            <div className="form-group">
              <label>Nueva Contraseña</label>
              <div className="password-wrapper">
                <input
                  type={showNew ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={formData.newPassword}
                  onChange={(e) => setFormData({ ...formData, newPassword: e.target.value })}
                  required
                  minLength={6}
                  maxLength={50}
                  autoComplete="new-password"
                />
                <button type="button" className="password-toggle" onClick={() => setShowNew(!showNew)} aria-label={showNew ? 'Ocultar' : 'Mostrar'}>
                  {showNew ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            <div className="form-group">
              <label>Confirmar Nueva Contraseña</label>
              <div className="password-wrapper">
                <input
                  type={showConfirm ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={formData.confirmPassword}
                  onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                  required
                  minLength={6}
                  maxLength={50}
                  autoComplete="new-password"
                />
                <button type="button" className="password-toggle" onClick={() => setShowConfirm(!showConfirm)} aria-label={showConfirm ? 'Ocultar' : 'Mostrar'}>
                  {showConfirm ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            <button type="submit" className="btn btn-primary" disabled={loading} style={{ marginTop: '8px' }}>
              {loading ? 'Guardando...' : 'Cambiar Contraseña'}
            </button>
          </form>
        </div>
      </div>
    </>
  );
}

export default ChangePasswordPanel;
