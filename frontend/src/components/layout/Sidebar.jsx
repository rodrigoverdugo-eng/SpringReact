import React, { useState, useEffect } from 'react';
import AuthService from '../../services/AuthService';
import InfoService from '../../services/InfoService';
import { Menu, Building2, ChevronDown, LogOut } from 'lucide-react';
import { MENU_ITEMS, APP_TITLE } from '../../config/menuConfig';
import '../../styles/main.css';
import './Sidebar.css';

function Sidebar({ currentView, onViewChange, currentUser, isOpen, setIsOpen }) {
  const [expandedMenu, setExpandedMenu] = useState(null);
  const [appVersion, setAppVersion] = useState(null);
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);

  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth <= 768);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  useEffect(() => {
    InfoService.getVersion()
      .then(setAppVersion)
      .catch(() => setAppVersion('?'));
  }, []);

  const handleLogout = () => {
    AuthService.logout();
  };

  const userRole = currentUser?.role?.name;
  const hasAccess = (item) => item.roles.length === 0 || item.roles.includes(userRole);

  // Filtrar menú según permisos (incluyendo submenús)
  const filteredMenuItems = MENU_ITEMS.map(item => {
    if (item.submenu) {
      const filteredSubmenu = item.submenu.filter(hasAccess);
      return { ...item, submenu: filteredSubmenu };
    }
    return item;
  }).filter(item => {
    if (hasAccess(item)) {
      if (item.submenu) return item.submenu.length > 0;
      return true;
    }
    return false;
  });

  const toggleSubmenu = (menuId) => {
    setExpandedMenu(expandedMenu === menuId ? null : menuId);
  };

  const handleMenuClick = (item) => {
    if (item.submenu && item.submenu.length > 0) {
      if (!isOpen) {
        setIsOpen(true);
        setExpandedMenu(item.id);
      } else {
        toggleSubmenu(item.id);
      }
    } else {
      onViewChange(item.id);
      if (isMobile) {
        setIsOpen(false);
      }
    }
  };

  return (
    <>
      {isOpen && isMobile && (
        <button className="sidebar-backdrop" aria-label="Cerrar menú" onClick={() => setIsOpen(false)} />
      )}
      {/* Botón flotante solo visible en móvil */}
      <button
        className="sidebar-toggle-mobile"
        onClick={() => setIsOpen(!isOpen)}
        aria-label={isOpen ? 'Cerrar menú' : 'Abrir menú'}
      >
        <Menu size={22} />
      </button>

      <aside className={`sidebar ${isOpen ? 'open' : 'closed'}`}>

        {/* 1. CABECERA: toggle integrado + logo + tema */}
        <div className="sidebar-header">
          <button
            className="sidebar-toggle-btn"
            onClick={() => setIsOpen(!isOpen)}
            aria-label={isOpen ? 'Cerrar menú' : 'Abrir menú'}
          >
            <Menu size={22} />
          </button>
          {isOpen && (
            <div className="sidebar-logo">
              <Building2 size={28} />
              <h2>{APP_TITLE}</h2>
            </div>
          )}

        </div>

        <nav className="sidebar-nav">
          {filteredMenuItems.map((item) => {
            const Icon = item.icon;
            return (
            <div key={item.id} className="nav-item-wrapper">
              <button
                className={`nav-item ${currentView === item.id ? 'active' : ''} ${item.submenu ? 'has-submenu' : ''}`}
                onClick={() => handleMenuClick(item)}
                title={!isOpen ? item.label : ''}
              >
              <Icon size={20} />
              {isOpen && (
                <>
                  <span>{item.label}</span>
                  {item.submenu && item.submenu.length > 0 && (
                    <ChevronDown size={16} className={`submenu-arrow ${expandedMenu === item.id ? 'expanded' : ''}`} />
                  )}
                </>
              )}
            </button>
            {item.submenu && expandedMenu === item.id && isOpen && (
              <div className="submenu">
                {item.submenu.map((subItem) => {
                  const SubIcon = subItem.icon;
                  return (
                  <button
                    key={subItem.id}
                    className={`submenu-item ${currentView === subItem.id ? 'active' : ''}`}
                    onClick={() => {
                      onViewChange(subItem.id);
                      if (isMobile) {
                        setIsOpen(false);
                      }
                    }}
                  >
                    <SubIcon size={18} />
                    <span>{subItem.label}</span>
                  </button>
                  );
                })}
              </div>
            )}
            </div>
            );
          })}
        </nav>

        <div className="sidebar-footer">          
          {/* Cerrar sesión */}
          <button className="btn-logout-sidebar" onClick={handleLogout} title={!isOpen ? 'Cerrar sesión' : ''}>
            <LogOut size={20} />
            {isOpen && <span>Cerrar Sesión</span>}
          </button>
          {/* Versión */}
          {isOpen && appVersion && (
            <p className="sidebar-version">v{appVersion}</p>
          )}
        </div>
      </aside>
    </>
  );
}

export default Sidebar;
