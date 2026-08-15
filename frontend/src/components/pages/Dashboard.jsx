import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronRight } from 'lucide-react';
import '../../styles/main.css';
import './Dashboard.css';
import Sidebar from '../layout/Sidebar';
import HomeView from '../features/HomeView';
import UserManagement from '../features/UserManagement';
import ChangePasswordPanel from '../features/ChangePasswordPanel';
import Profile from '../features/Profile';
import AuthService from '../../services/AuthService';
import useInactivityLogout from '../../hooks/useInactivityLogout';
import { INACTIVITY_TIMEOUT_MINUTES } from '../../config/sessionTimeout';
import { getLabelForView, hasMenuAccess, APP_LOCALE } from '../../config/menuConfig';

function SettingsPlaceholder() {
  return (
    <div className="card">
      <h2 className="card-header">Configuración</h2>
      <div className="card-body">
        <p className="text-muted">Sección en desarrollo...</p>
      </div>
    </div>
  );
}

function AccessDenied() {
  return (
    <div className="card">
      <h2 className="card-header">Acceso Denegado</h2>
      <div className="card-body">
        <p className="text-muted">No tienes permisos para acceder a esta sección.</p>
      </div>
    </div>
  );
}

// Registro de vistas: agregar aquí para registrar una nueva vista sin tocar el render
const VIEW_REGISTRY = {
  home:              HomeView,
  users:             UserManagement,
  'change-password': ChangePasswordPanel,
  profile:           Profile,
  settings:          SettingsPlaceholder,
};

function Dashboard() {
  const [currentUser, setCurrentUser] = useState(null);
  const [currentView, setCurrentView] = useState('home');
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [isHeaderScrolled, setIsHeaderScrolled] = useState(false);
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

  useEffect(() => {
    const handleScroll = () => {
      setIsHeaderScrolled(window.scrollY > 20);
    };

    handleScroll();
    window.addEventListener('scroll', handleScroll, { passive: true });

    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

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
        <div className={`dashboard-header ${isHeaderScrolled ? 'is-scrolled' : ''}`}>
          <div className="header-breadcrumb">
            <ChevronRight size={20} className="breadcrumb-icon" />
            <h1>{getLabelForView(currentView)}</h1>
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
          {(() => {
            const ViewComponent = VIEW_REGISTRY[currentView];
            if (!ViewComponent) return null;
            if (!hasMenuAccess(currentView, currentUser?.role?.name)) return <AccessDenied />;
            return <ViewComponent currentUser={currentUser} />;
          })()}
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
