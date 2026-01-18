import { useEffect, useRef } from 'react';
import AuthService from '../services/AuthService';

// Hook personalizado para detectar inactividad
const useInactivityLogout = (timeoutMinutes = 15) => {
  const timeoutRef = useRef(null);
  const INACTIVITY_TIME = timeoutMinutes * 60 * 1000; // Convertir a milisegundos

  const resetTimer = () => {
    // Limpiar timer anterior
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }

    // Crear nuevo timer
    timeoutRef.current = setTimeout(() => {
      console.log('Sesión cerrada por inactividad');
      AuthService.logout();
    }, INACTIVITY_TIME);
  };

  useEffect(() => {
    // Solo activar si el usuario está autenticado
    if (!AuthService.isAuthenticated()) {
      return;
    }

    // Eventos que indican actividad del usuario
    const events = ['mousedown', 'keydown', 'scroll', 'touchstart', 'click'];

    // Iniciar timer
    resetTimer();

    // Agregar listeners
    events.forEach(event => {
      document.addEventListener(event, resetTimer);
    });

    // Cleanup
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
      events.forEach(event => {
        document.removeEventListener(event, resetTimer);
      });
    };
  }, [INACTIVITY_TIME]);
};

export default useInactivityLogout;
