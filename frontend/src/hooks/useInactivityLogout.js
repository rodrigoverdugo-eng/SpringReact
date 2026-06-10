import { useEffect, useRef } from 'react';
import AuthService from '../services/AuthService';

// Hook personalizado para detectar inactividad
const useInactivityLogout = (timeoutMinutes = 15) => {
  const timeoutRef = useRef(null);
  const lastActivityRef = useRef(Date.now());
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
    lastActivityRef.current = Date.now();
    scheduleLogout(INACTIVITY_TIME);
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

    // Agregar listeners de actividad
    events.forEach(event => {
      document.addEventListener(event, resetTimer);
    });

    // Manejar visibilidad de la página (bloqueo de pantalla / cambio de app en móvil)
    const handleVisibilityChange = () => {
      if (document.hidden) {
        // Página oculta: pausar el timer para no cerrar sesión mientras el teléfono está bloqueado
        if (timeoutRef.current) {
          clearTimeout(timeoutRef.current);
        }
      } else {
        // Página visible de nuevo: verificar si el tiempo inactivo superó el límite
        const elapsed = Date.now() - lastActivityRef.current;
        if (elapsed >= INACTIVITY_TIME) {
          AuthService.logout();
        } else {
          // Reanudar el timer con el tiempo restante
          scheduleLogout(INACTIVITY_TIME - elapsed);
        }
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);

    // Cleanup
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
      events.forEach(event => {
        document.removeEventListener(event, resetTimer);
      });
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [INACTIVITY_TIME]);
};

export default useInactivityLogout;
