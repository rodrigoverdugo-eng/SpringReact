import { Home, Users, KeyRound, Settings, UserCircle } from 'lucide-react';

// Título de la aplicación — fuente única: variable de entorno VITE_APP_TITLE (.env)
export const APP_TITLE = import.meta.env.VITE_APP_TITLE ?? 'Sistema de Gestión';

export const APP_TAGLINE = 'Plataforma centralizada para la gestión de su organización.';

export const APP_LOCALE = 'es-ES';

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
    id: 'profile',
    label: 'Mi Perfil',
    icon: UserCircle,
    roles: [],
  },
  {
    id: 'settings',
    label: 'Configuración',
    icon: Settings,
    roles: [],
    submenu: [
      { id: 'users',            label: 'Gestión de Usuarios', icon: Users,    roles: ['ADMIN'] },
      { id: 'change-password', label: 'Cambiar Contraseña',  icon: KeyRound, roles: [] },
    ],
  },
];

/**
 * Devuelve la etiqueta de breadcrumb para una vista, derivada de MENU_ITEMS.
 * Para submenús incluye el nombre del padre: "Padre › Hijo".
 */
export function getLabelForView(viewId) {
  for (const item of MENU_ITEMS) {
    if (item.id === viewId) return item.label;
    const sub = item.submenu?.find(s => s.id === viewId);
    if (sub) return `${item.label} › ${sub.label}`;
  }
  return viewId;
}

/**
 * Indica si un rol tiene acceso a una vista, según las reglas de MENU_ITEMS.
 * roles: [] significa acceso universal.
 */
export function hasMenuAccess(viewId, userRole) {
  for (const item of MENU_ITEMS) {
    if (item.id === viewId) {
      return item.roles.length === 0 || item.roles.includes(userRole);
    }
    const sub = item.submenu?.find(s => s.id === viewId);
    if (sub) return sub.roles.length === 0 || sub.roles.includes(userRole);
  }
  return true;
}
