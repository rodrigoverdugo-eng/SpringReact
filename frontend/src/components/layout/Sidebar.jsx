import React, { useState } from 'react';
import AuthService from '../../services/AuthService';
import '../../styles/main.css';
import './Sidebar.css';

function Sidebar({ currentView, onViewChange, currentUser, isOpen, setIsOpen }) {
  const [expandedMenu, setExpandedMenu] = useState(null);

  const handleLogout = () => {
    AuthService.logout();
  };

  const isAdmin = currentUser?.role?.name === 'ADMIN';

  const menuItems = [
    { id: 'home', label: 'Inicio', icon: 'home', adminOnly: false },
    { 
      id: 'settings', 
      label: 'Configuración', 
      icon: 'settings', 
      adminOnly: true,
      submenu: [
        { id: 'users', label: 'Gestión de Usuarios', icon: 'users', adminOnly: true },
        { id: 'roles', label: 'Roles', icon: 'shield', adminOnly: false },
      ]
    },
  ];

  // Filtrar menú según permisos (incluyendo submenús)
  const filteredMenuItems = menuItems.map(item => {
    if (item.submenu) {
      const filteredSubmenu = item.submenu.filter(subItem => !subItem.adminOnly || isAdmin);
      return { ...item, submenu: filteredSubmenu };
    }
    return item;
  }).filter(item => !item.adminOnly || isAdmin);

  const toggleSubmenu = (menuId) => {
    setExpandedMenu(expandedMenu === menuId ? null : menuId);
  };

  const handleMenuClick = (item) => {
    if (item.submenu && item.submenu.length > 0) {
      toggleSubmenu(item.id);
    } else {
      onViewChange(item.id);
    }
  };

  return (
    <>
      <button 
        className={`sidebar-toggle ${isOpen ? 'sidebar-open' : 'sidebar-closed'}`}
        onClick={() => setIsOpen(!isOpen)}
        aria-label={isOpen ? 'Cerrar menú' : 'Abrir menú'}
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <line x1="3" y1="12" x2="21" y2="12"></line>
          <line x1="3" y1="6" x2="21" y2="6"></line>
          <line x1="3" y1="18" x2="21" y2="18"></line>
        </svg>
      </button>

      <aside className={`sidebar ${isOpen ? 'open' : 'closed'}`}>
        <div className="sidebar-header">
          <div className="sidebar-logo">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <rect x="3" y="3" width="7" height="7"></rect>
              <rect x="14" y="3" width="7" height="7"></rect>
              <rect x="14" y="14" width="7" height="7"></rect>
              <rect x="3" y="14" width="7" height="7"></rect>
            </svg>
            {isOpen && <h2>Panel de Control</h2>}
          </div>
        </div>

        <div className="sidebar-user">
          {isOpen ? (
            <>
              <div className="user-avatar">
                {currentUser?.name?.charAt(0).toUpperCase() || 'U'}
              </div>
              <div className="user-details">
                <p className="user-name">{currentUser?.name}</p>
                <p className="user-email">{currentUser?.email}</p>
              </div>
            </>
          ) : (
            <div className="user-avatar-small">
              {currentUser?.name?.charAt(0).toUpperCase() || 'U'}
            </div>
          )}
        </div>

        <nav className="sidebar-nav">
          {filteredMenuItems.map((item) => (
            <div key={item.id} className="nav-item-wrapper">
              <button
                className={`nav-item ${currentView === item.id ? 'active' : ''} ${item.submenu ? 'has-submenu' : ''}`}
                onClick={() => handleMenuClick(item)}
                title={!isOpen ? item.label : ''}
              >
              {item.icon === 'home' && (
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                  <polyline points="9 22 9 12 15 12 15 22"></polyline>
                </svg>
              )}
              {item.icon === 'users' && (
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                </svg>
              )}
              {item.icon === 'shield' && (
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                </svg>
              )}
              {item.icon === 'user' && (
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
              )}
              {item.icon === 'settings' && (
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="12" cy="12" r="3"></circle>
                  <path d="M12 1v6m0 6v6m0-6h6m-6 0H6"></path>
                  <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
                </svg>
              )}
              {isOpen && (
                <>
                  <span>{item.label}</span>
                  {item.submenu && item.submenu.length > 0 && (
                    <svg 
                      className={`submenu-arrow ${expandedMenu === item.id ? 'expanded' : ''}`}
                      xmlns="http://www.w3.org/2000/svg" 
                      width="16" 
                      height="16" 
                      viewBox="0 0 24 24" 
                      fill="none" 
                      stroke="currentColor" 
                      strokeWidth="2" 
                      strokeLinecap="round" 
                      strokeLinejoin="round"
                    >
                      <polyline points="6 9 12 15 18 9"></polyline>
                    </svg>
                  )}
                </>
              )}
            </button>
            {item.submenu && expandedMenu === item.id && isOpen && (
              <div className="submenu">
                {item.submenu.map((subItem) => (
                  <button
                    key={subItem.id}
                    className={`submenu-item ${currentView === subItem.id ? 'active' : ''}`}
                    onClick={() => onViewChange(subItem.id)}
                  >
                    {subItem.icon === 'users' && (
                      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                        <circle cx="9" cy="7" r="4"></circle>
                        <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                      </svg>
                    )}
                    {subItem.icon === 'shield' && (
                      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                      </svg>
                    )}
                    <span>{subItem.label}</span>
                  </button>
                ))}
              </div>
            )}
            </div>
          ))}
        </nav>

        <div className="sidebar-footer">
          <button className="btn-logout-sidebar" onClick={handleLogout} title={!isOpen ? 'Cerrar sesión' : ''}>
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
              <polyline points="16 17 21 12 16 7"></polyline>
              <line x1="21" y1="12" x2="9" y2="12"></line>
            </svg>
            {isOpen && <span>Cerrar Sesión</span>}
          </button>
        </div>
      </aside>
    </>
  );
}

export default Sidebar;
