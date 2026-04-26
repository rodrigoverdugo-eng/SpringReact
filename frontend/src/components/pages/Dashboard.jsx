import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '../../styles/main.css';
import './Dashboard.css';
import Sidebar from '../layout/Sidebar';
import UserManagement from '../features/UserManagement';
import RoleManagement from '../features/RoleManagement';
import AuthService from '../../services/AuthService';
import useInactivityLogout from '../../hooks/useInactivityLogout';

function Dashboard() {
  const [currentUser, setCurrentUser] = useState(null);
  const [currentView, setCurrentView] = useState('home');
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const navigate = useNavigate();

  // Control de inactividad: logout automático después de 15 minutos sin actividad
  useInactivityLogout(15);

  // Configuración de vistas
  const viewConfig = {
    home: 'Inicio',
    users: 'Configuración › Gestión de Usuarios',
    roles: 'Configuración › Roles del Sistema',
    settings: 'Configuración'
  };

  useEffect(() => {
    console.log('Dashboard montado');
    const user = AuthService.getCurrentUser();
    console.log('Usuario actual:', user);
    setCurrentUser(user);
    
    // Verificar si requiere cambio de contraseña
    if (user && user.requiresPasswordChange) {
      console.log('Usuario requiere cambio de contraseña, redirigiendo...');
      navigate('/change-password');
      return;
    }
    
    // Configurar interceptor de axios al montar
    AuthService.setupAxiosInterceptor();
  }, [navigate]);

  return (
    <div className="dashboard">
      {console.log('Renderizando Dashboard', { currentUser, currentView, sidebarOpen })}
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
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="breadcrumb-icon">
              <polyline points="9 18 15 12 9 6"></polyline>
            </svg>
            <h1>{viewConfig[currentView] || 'Dashboard'}</h1>
          </div>
          <div className="header-user">
            <div className="topbar-avatar">
              {currentUser?.name?.charAt(0).toUpperCase() || 'U'}
            </div>
            <div className="topbar-user-info">
              <span className="topbar-name">{currentUser?.name}</span>
              <span className="topbar-role">{currentUser?.role?.descripcion}</span>
            </div>
          </div>
        </div>

        <div className="container">
          {currentView === 'users' && currentUser?.role?.name === 'ADMIN' && <UserManagement />}
          {currentView === 'users' && currentUser?.role?.name !== 'ADMIN' && (
            <div className="card">
              <h2 className="card-header">Acceso Denegado</h2>
              <div className="card-body">
                <p className="text-muted">No tienes permisos para acceder a esta sección.</p>
              </div>
            </div>
          )}
          {currentView === 'roles' && <RoleManagement />}

          {currentView === 'home' && (
            <div className="card">
              <h2 className="card-header">Bienvenido al Sistema de Gestión</h2>
              <div className="card-body">
                <div className="profile-info">
                  <p><strong>Nombre:</strong> {currentUser?.name}</p>
                  <p><strong>Email:</strong> {currentUser?.email}</p>
                  <p><strong>Rol:</strong> {currentUser?.role?.descripcion}</p>                  
                </div>
              </div>
            </div>
          )}

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
