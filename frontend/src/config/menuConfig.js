import { Home, Users, Shield, Settings } from 'lucide-react';

// Título de la aplicación — fuente única: variable de entorno VITE_APP_TITLE (.env)
export const APP_TITLE = import.meta.env.VITE_APP_TITLE ?? 'Sistema de Gestión';

/**
 * Definición del menú de navegación.
 *
 * Cada ítem puede tener:
 *   - id:      identificador único de la vista
 *   - label:   texto visible
 *   - icon:    componente de lucide-react
 *   - roles:   array de roles con acceso; [] significa acceso para todos
 *   - submenu: array de ítems hijo (misma estructura, sin submenu anidado)
 */
export const MENU_ITEMS = [
  {
    id: 'home',
    label: 'Inicio',
    icon: Home,
    roles: [],
  },
  {
    id: 'settings',
    label: 'Configuración',
    icon: Settings,
    roles: [],
    submenu: [
      { id: 'users', label: 'Gestión de Usuarios', icon: Users,  roles: ['ADMIN'] },
      { id: 'roles', label: 'Roles',               icon: Shield, roles: [] },
    ],
  },
];

/**
 * Etiquetas de breadcrumb para cada vista.
 * Centralizado aquí para mantener sincronizadas Sidebar y Dashboard.
 */
export const VIEW_LABELS = {
  home:     'Inicio',
  users:    'Configuración › Gestión de Usuarios',
  roles:    'Configuración › Roles del Sistema',
  settings: 'Configuración',
};
