import React, { createContext, useContext, useState } from 'react';
import ProfileService from '../services/ProfileService';

const ThemeContext = createContext();

function readInitialTheme() {
  // Priority: themePreference stored in user profile data (comes from server on login)
  try {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      if (user?.themePreference) {
        return user.themePreference === 'dark';
      }
    }
  } catch (_) {}
  // Fallback to standalone 'theme' key
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
    // Keep user object in localStorage in sync
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
    // Persist to server only when logged in (fire and forget)
    if (localStorage.getItem('user')) {
      ProfileService.updateTheme(newDark ? 'dark' : 'light').catch(() => {});
    }
  };

  // Called after login to apply the server-stored preference immediately
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
  return useContext(ThemeContext);
}
