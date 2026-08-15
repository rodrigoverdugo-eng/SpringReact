import React, { useState, useEffect } from 'react';
import { Users, UserCheck, UserX, KeyRound } from 'lucide-react';
import UserService from '../../services/UserService';

function HomeView({ currentUser }) {
  const [stats, setStats] = useState(null);
  const isAdmin = currentUser?.role?.name === 'ADMIN';

  useEffect(() => {
    if (!isAdmin) return;
    UserService.getAllUsers()
      .then(users => {
        setStats({
          total: users.length,
          active: users.filter(u => u.vigencia).length,
          inactive: users.filter(u => !u.vigencia).length,
          pendingPassword: users.filter(u => u.requiresPasswordChange).length,
        });
      })
      .catch(() => setStats(null));
  }, [isAdmin]);

  return (
    <div className="home-view">
      {isAdmin && stats && (
        <div className="stats-grid">
          <div className="stat-card stat-card--total">
            <div className="stat-card__icon"><Users size={24} /></div>
            <div className="stat-card__content">
              <span className="stat-card__value">{stats.total}</span>
              <span className="stat-card__label">Total de Usuarios</span>
            </div>
          </div>
          <div className="stat-card stat-card--active">
            <div className="stat-card__icon"><UserCheck size={24} /></div>
            <div className="stat-card__content">
              <span className="stat-card__value">{stats.active}</span>
              <span className="stat-card__label">Usuarios Activos</span>
            </div>
          </div>
          <div className="stat-card stat-card--inactive">
            <div className="stat-card__icon"><UserX size={24} /></div>
            <div className="stat-card__content">
              <span className="stat-card__value">{stats.inactive}</span>
              <span className="stat-card__label">Usuarios Inactivos</span>
            </div>
          </div>
          <div className="stat-card stat-card--pending">
            <div className="stat-card__icon"><KeyRound size={24} /></div>
            <div className="stat-card__content">
              <span className="stat-card__value">{stats.pendingPassword}</span>
              <span className="stat-card__label">Cambio de Contraseña Pendiente</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default HomeView;
