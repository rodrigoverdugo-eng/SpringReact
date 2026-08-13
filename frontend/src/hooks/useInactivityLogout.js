import { useEffect, useRef } from 'react';
import AuthService from '../services/AuthService';
import { INACTIVITY_TIMEOUT_MINUTES } from '../config/sessionTimeout';

// Hook personalizado para detectar inactividad
const useInactivityLogout = (timeoutMinutes = INACTIVITY_TIMEOUT_MINUTES) => {
  const timeoutRef = useRef(null);
  const INACTIVITY_TIME = timeoutMinutes * 60 * 1000; // Convertir a milisegundos

  const scheduleLogout = (delay) => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }
    timeoutRef.current = setTimeout(() => {
      AuthService.logout();
    }, delay);
  };

  const resetTimer = () => {
    AuthService.recordActivity();
    scheduleLogout(INACTIVITY_TIME);
  };

  const syncTimerWithStoredActivity = () => {
    if (AuthService.hasInactivityExpired(timeoutMinutes)) {
      AuthService.logout();
      return;
    }

    const lastActivity = AuthService.getLastActivityTimestamp() ?? Date.now();
    const elapsed = Date.now() - lastActivity;
    const remaining = Math.max(INACTIVITY_TIME - elapsed, 0);

    scheduleLogout(remaining);
  };

  useEffect(() => {
    // Solo activar si el usuario está autenticado
    if (!AuthService.isAuthenticated()) {
      return;
    }

    // Eventos que indican actividad del usuario
    const events = ['pointerdown', 'keydown', 'scroll', 'touchstart', 'click', 'focus'];

    // Iniciar timer
    resetTimer();

    // Agregar listeners de actividad
    events.forEach(event => {
      document.addEventListener(event, resetTimer);
    });

    // Manejar visibilidad de la página (bloqueo de pantalla / cambio de app en móvil)
    const handleVisibilityChange = () => {
      if (document.hidden) {
        if (timeoutRef.current) {
          clearTimeout(timeoutRef.current);
        }
      } else {
        syncTimerWithStoredActivity();
      }
    };

    const handlePageShow = () => {
      syncTimerWithStoredActivity();
    };

    const handlePageHide = () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    window.addEventListener('pageshow', handlePageShow);
    window.addEventListener('pagehide', handlePageHide);

    // Cleanup
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
      events.forEach(event => {
        document.removeEventListener(event, resetTimer);
      });
      document.removeEventListener('visibilitychange', handleVisibilityChange);
      window.removeEventListener('pageshow', handlePageShow);
      window.removeEventListener('pagehide', handlePageHide);
    };
  }, [INACTIVITY_TIME]);
};

export default useInactivityLogout;
