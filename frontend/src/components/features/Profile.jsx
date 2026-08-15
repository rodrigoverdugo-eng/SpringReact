import React from 'react';
import { Mail, Shield, Clock, Sun, Moon } from 'lucide-react';
import { useTheme } from '../../context/ThemeContext';
import { APP_LOCALE } from '../../config/menuConfig';
import './Profile.css';

function Profile({ currentUser }) {
  const { isDark, toggleTheme } = useTheme();

  return (
    <div className="profile-view">

      <div className="card">
        <h2 className="card-header">Mi Perfil</h2>
        <div className="card-body">
          <div className="profile-grid">
            <div className="profile-avatar-large">
              {currentUser?.name?.charAt(0).toUpperCase() || 'U'}
            </div>
            <div className="profile-details">
              <h3 className="profile-name">{currentUser?.name}</h3>
              <div className="profile-field">
                <Mail size={16} />
                <span>{currentUser?.email}</span>
              </div>
              <div className="profile-field">
                <Shield size={16} />
                <span>{currentUser?.role?.descripcion}</span>
              </div>
              {currentUser?.lastLoginAt && (
                <div className="profile-field">
                  <Clock size={16} />
                  <span>
                    Último acceso:{' '}
                    {new Date(currentUser.lastLoginAt).toLocaleString(APP_LOCALE)}
                  </span>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="card">
        <h2 className="card-header">Preferencias</h2>
        <div className="card-body">
          <div className="preference-section">
            <p className="preference-label">Tema de la interfaz</p>
            <div className="theme-selector">
              <button
                className={`theme-option ${!isDark ? 'theme-option--active' : ''}`}
                onClick={() => isDark && toggleTheme()}
                aria-pressed={!isDark}
              >
                <Sun size={22} />
                <span>Claro</span>
              </button>
              <button
                className={`theme-option ${isDark ? 'theme-option--active' : ''}`}
                onClick={() => !isDark && toggleTheme()}
                aria-pressed={isDark}
              >
                <Moon size={22} />
                <span>Oscuro</span>
              </button>
            </div>
            <p className="preference-hint">
              La preferencia se guarda automáticamente en tu cuenta.
            </p>
          </div>
        </div>
      </div>

    </div>
  );
}

export default Profile;
