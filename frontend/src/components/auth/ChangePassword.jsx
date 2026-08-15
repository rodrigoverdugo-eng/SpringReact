import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Eye, EyeOff, CheckCircle, AlertCircle, X } from 'lucide-react';
import AuthService from '../../services/AuthService';
import { validatePassword, PASSWORD_RULES } from '../../utils/passwordValidation';
import '../../styles/main.css';
import './ChangePassword.css';

function ChangePassword() {
  const [formData, setFormData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
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
    setError('');

    if (formData.newPassword !== formData.confirmPassword) {
      showToast('error', 'Error de validación', 'Las contraseñas nuevas no coinciden');
      return;
    }

    const { isValid: pwValid, errors: pwErrors } = validatePassword(formData.newPassword);
    if (!pwValid) {
      showToast('error', 'Contraseña insegura', pwErrors[0]);
      return;
    }

    setLoading(true);

    try {
      await AuthService.changePassword(
        formData.currentPassword,
        formData.newPassword
      );
      
      const user = JSON.parse(localStorage.getItem('user'));
      user.requiresPasswordChange = false;
      localStorage.setItem('user', JSON.stringify(user));
      
      showToast('success', 'Contraseña actualizada', 'Tu contraseña fue cambiada exitosamente');
      setTimeout(() => navigate('/dashboard'), 1500);
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

      <div className="change-password-container">
      <div className="change-password-card">
        <h1>🔒 Cambio de Contraseña Obligatorio</h1>
        <p className="warning-text">
          Por seguridad, debes cambiar tu contraseña antes de continuar.
        </p>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Contraseña Actual</label>
            <div className="password-wrapper">
              <input
                type={showCurrentPassword ? 'text' : 'password'}
                placeholder="••••••••"
                value={formData.currentPassword}
                onChange={(e) => setFormData({ ...formData, currentPassword: e.target.value })}
                required
                minLength={6}
                maxLength={50}
              />
              <button type="button" className="password-toggle" onClick={() => setShowCurrentPassword(!showCurrentPassword)} aria-label={showCurrentPassword ? 'Ocultar' : 'Mostrar'}>
                {showCurrentPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          <div className="form-group">
            <label>Nueva Contraseña</label>
            <div className="password-wrapper">
              <input
                type={showNewPassword ? 'text' : 'password'}
                placeholder="••••••••"
                value={formData.newPassword}
                onChange={(e) => setFormData({ ...formData, newPassword: e.target.value })}
                required
                minLength={8}
                maxLength={50}
                autoComplete="new-password"
              />
              <button type="button" className="password-toggle" onClick={() => setShowNewPassword(!showNewPassword)} aria-label={showNewPassword ? 'Ocultar' : 'Mostrar'}>
                {showNewPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
            {formData.newPassword && (
              <ul className="password-requirements">
                {PASSWORD_RULES.map(rule => (
                  <li key={rule.id} className={rule.test(formData.newPassword) ? 'req-met' : 'req-unmet'}>
                    {rule.test(formData.newPassword) ? '✓' : '✗'} {rule.label}
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="form-group">
            <label>Confirmar Nueva Contraseña</label>
            <div className="password-wrapper">
              <input
                type={showConfirmPassword ? 'text' : 'password'}
                placeholder="••••••••"
                value={formData.confirmPassword}
                onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                required
                minLength={8}
                maxLength={50}
                autoComplete="new-password"
              />
              <button type="button" className="password-toggle" onClick={() => setShowConfirmPassword(!showConfirmPassword)} aria-label={showConfirmPassword ? 'Ocultar' : 'Mostrar'}>
                {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          <button type="submit" className="btn btn-primary btn-block btn-lg" disabled={loading}>
            {loading ? 'Cambiando...' : 'Cambiar Contraseña'}
          </button>
        </form>
      </div>
      </div>
    </>
  );
}

export default ChangePassword;
