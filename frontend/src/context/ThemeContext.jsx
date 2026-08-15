import React, { createContext, useContext, useState } from 'react';
import ProfileService from '../services/ProfileService';

const ThemeContext = createContext();

function readInitialTheme() {
  // Prioridad: preferencia del perfil del usuario (viene del servidor al hacer login)
  try {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      if (user?.themePreference) {
        return user.themePreference === 'dark';
      }
    }
  } catch (_) {}
  // Fallback: clave 'theme' independiente en localStorage
  return localStorage.getItem('theme') === 'dark';
}

export function ThemeProvider({ children }) {
  const [isDark, setIsDark] = useState(() => {
    const dark = readInitialTheme();
    document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
    return dark;
  });

  const applyTheme = (dark) => {
    document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
    localStorage.setItem('theme', dark ? 'dark' : 'light');
    // Sincronizar preferencia dentro del objeto usuario en localStorage
    try {
      const userStr = localStorage.getItem('user');
      if (userStr) {
        const user = JSON.parse(userStr);
        user.themePreference = dark ? 'dark' : 'light';
        localStorage.setItem('user', JSON.stringify(user));
      }
    } catch (_) {}
  };

  const toggleTheme = () => {
    const newDark = !isDark;
    setIsDark(newDark);
    applyTheme(newDark);
    // Persistir en el servidor solo si hay sesión activa (sin esperar respuesta)
    if (localStorage.getItem('user')) {
      ProfileService.updateTheme(newDark ? 'dark' : 'light').catch(() => {});
    }
  };

  // Llamado tras el login para aplicar la preferencia guardada en el servidor
  const setTheme = (dark) => {
    setIsDark(dark);
    applyTheme(dark);
  };

  return (
    <ThemeContext.Provider value={{ isDark, toggleTheme, setTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const context = useContext(ThemeContext);
  if (!context) throw new Error('useTheme must be used inside ThemeProvider');
  return context;
}
