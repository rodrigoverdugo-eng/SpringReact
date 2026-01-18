import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthService from '../../services/AuthService';
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
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // Validar que las contraseñas coincidan
    if (formData.newPassword !== formData.confirmPassword) {
      setError('Las contraseñas nuevas no coinciden');
      return;
    }

    // Validar longitud mínima
    if (formData.newPassword.length < 6) {
      setError('La contraseña debe tener al menos 6 caracteres');
      return;
    }

    setLoading(true);

    try {
      await AuthService.changePassword(
        formData.currentPassword,
        formData.newPassword
      );
      
      // Actualizar el usuario en localStorage para marcar que ya cambió la contraseña
      const user = JSON.parse(localStorage.getItem('user'));
      user.requiresPasswordChange = false;
      localStorage.setItem('user', JSON.stringify(user));
      
      alert('Contraseña cambiada exitosamente');
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 
               'Error al cambiar la contraseña. Intenta nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="change-password-container">
      <div className="change-password-card">
        <h1>🔒 Cambio de Contraseña Obligatorio</h1>
        <p className="warning-text">
          Por seguridad, debes cambiar tu contraseña antes de continuar.
        </p>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Contraseña Actual</label>
            <input
              type="password"
              placeholder="••••••••"
              value={formData.currentPassword}
              onChange={(e) => setFormData({ ...formData, currentPassword: e.target.value })}
              required
              minLength={6}
              maxLength={50}
            />
          </div>

          <div className="form-group">
            <label>Nueva Contraseña</label>
            <input
              type="password"
              placeholder="••••••••"
              value={formData.newPassword}
              onChange={(e) => setFormData({ ...formData, newPassword: e.target.value })}
              required
              minLength={6}
              maxLength={50}
              autoComplete="new-password"
            />
          </div>

          <div className="form-group">
            <label>Confirmar Nueva Contraseña</label>
            <input
              type="password"
              placeholder="••••••••"
              value={formData.confirmPassword}
              onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
              required
              minLength={6}
              maxLength={50}
              autoComplete="new-password"
            />
          </div>

          <button type="submit" className="btn btn-primary btn-block btn-lg" disabled={loading}>
            {loading ? 'Cambiando...' : 'Cambiar Contraseña'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default ChangePassword;
