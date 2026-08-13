import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronRight, Users, UserCheck, UserX, KeyRound } from 'lucide-react';
import '../../styles/main.css';
import './Dashboard.css';
import Sidebar from '../layout/Sidebar';
import UserManagement from '../features/UserManagement';
import ChangePasswordPanel from '../features/ChangePasswordPanel';
import Profile from '../features/Profile';
import AuthService from '../../services/AuthService';
import UserService from '../../services/UserService';
import useInactivityLogout from '../../hooks/useInactivityLogout';
import { INACTIVITY_TIMEOUT_MINUTES } from '../../config/sessionTimeout';
import { VIEW_LABELS, APP_LOCALE } from '../../config/menuConfig';

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

function Dashboard() {
  const [currentUser, setCurrentUser] = useState(null);
  const [currentView, setCurrentView] = useState('home');
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const navigate = useNavigate();

  useInactivityLogout(INACTIVITY_TIMEOUT_MINUTES);

  useEffect(() => {
    const user = AuthService.getCurrentUser();
    setCurrentUser(user);

    if (user && user.requiresPasswordChange) {
      navigate('/change-password');
      return;
    }

    AuthService.setupAxiosInterceptor();
  }, [navigate]);

  return (
    <div className="dashboard">
      <Sidebar
        currentView={currentView}
        onViewChange={setCurrentView}
        currentUser={currentUser}
        isOpen={sidebarOpen}
        setIsOpen={setSidebarOpen}
      />

      <div className={`dashboard-content ${sidebarOpen ? 'sidebar-open' : 'sidebar-closed'}`}>
        <div className="dashboard-header">
          <div className="header-breadcrumb">
            <ChevronRight size={20} className="breadcrumb-icon" />
            <h1>{VIEW_LABELS[currentView] || 'Dashboard'}</h1>
          </div>
          <div className="header-user">
            <div className="topbar-avatar">
              {currentUser?.name?.charAt(0).toUpperCase() || 'U'}
            </div>
            <div className="topbar-user-info">
              <span className="topbar-name">{currentUser?.name}</span>
              <span className="topbar-role">{currentUser?.role?.descripcion}</span>
              {currentUser?.lastLoginAt && (
                <span className="topbar-last-login" title="Último acceso">
                  Último acceso: {new Date(currentUser.lastLoginAt).toLocaleString(APP_LOCALE)}
                </span>
              )}
            </div>
          </div>
        </div>

        <div className="container">
          {currentView === 'home' && <HomeView currentUser={currentUser} />}

          {currentView === 'users' && currentUser?.role?.name === 'ADMIN' && <UserManagement />}
          {currentView === 'users' && currentUser?.role?.name !== 'ADMIN' && (
            <div className="card">
              <h2 className="card-header">Acceso Denegado</h2>
              <div className="card-body">
                <p className="text-muted">No tienes permisos para acceder a esta sección.</p>
              </div>
            </div>
          )}

          {currentView === 'change-password' && <ChangePasswordPanel />}

          {currentView === 'profile' && <Profile currentUser={currentUser} />}

          {currentView === 'settings' && (
            <div className="card">
              <h2 className="card-header">Configuración</h2>
              <div className="card-body">
                <p className="text-muted">Sección en desarrollo...</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
