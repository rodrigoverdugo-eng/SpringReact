import React, { useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import AuthService from '../../services/AuthService';

function PrivateRoute({ children }) {
  // 'checking': intentando restaurar sesión via cookie
  // 'auth': autenticado
  // 'unauth': no autenticado
  const [status, setStatus] = useState('checking');

  useEffect(() => {
    if (AuthService.isAuthenticated()) {
      setStatus('auth');
    } else {
      // Al recargar la página, _accessToken es null pero la cookie puede ser válida
      AuthService.refreshAccessToken().then(token => {
        setStatus(token ? 'auth' : 'unauth');
      });
    }
  }, []);

  if (status === 'checking') return null;
  if (status === 'unauth') return <Navigate to="/login" replace />;
  return children;
}

export default PrivateRoute;
