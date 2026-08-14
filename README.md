# 🚀 Spring Boot + React Enterprise Application

Aplicación web empresarial completa con autenticación JWT, sistema de roles y gestión de usuarios. Backend desarrollado con Spring Boot y frontend moderno con React 18.

## ✨ Características Principales

### 🔐 Autenticación y Seguridad
- **JWT Authentication**: Tokens de acceso y refresh
- **Sistema de Roles**: ADMIN y USER con permisos diferenciados
- **Cambio de Contraseña Obligatorio**: En el primer inicio de sesión
- **Logout Automático**: Por inactividad (10 minutos, válido en web y móvil)
- **Rutas Protegidas**: Componentes protegidos con PrivateRoute
- **Validación de Usuario Activo**: Campo "vigencia" para activar/desactivar usuarios
- **Registro de Actividad**: Cada login registra fecha/hora e IP del cliente
- **Último Acceso en Login**: La respuesta del login incluye la fecha del acceso anterior (`lastLoginAt`)
- **Preferencia de Tema por Usuario**: La respuesta del login incluye `themePreference` y se aplica automáticamente al iniciar sesión
- **Logging Estructurado**: Logs en formato JSON (Logstash) con campos MDC: evento, email, IP, rol

### 👥 Gestión de Usuarios
- **CRUD Completo**: Crear, leer, actualizar y eliminar usuarios
- **Asignación de Roles**: Admin puede asignar roles a usuarios
- **Email Único**: Validación frontend y backend
- **Estados de Usuario**: Activo/Inactivo con badges visuales
- **Restricción de Acceso**: Solo ADMIN puede gestionar usuarios
- **Historial de Actividad**: Modal para ver los últimos 20 logins de cada usuario (fecha + IP)
- **Último Acceso Global**: Columna de último acceso visible en la tabla de usuarios
- **Tabla Responsive Mejorada**: En móvil, la lista de usuarios se renderiza en tarjetas con pares etiqueta/valor estables (Nombre, Email, etc.) y acciones alineadas
- **Eliminación en Cascada**: Al eliminar un usuario se eliminan previamente sus registros de `user_login_history` para respetar la restricción de clave foránea

### 🎨 Interfaz de Usuario
- **Diseño Empresarial Profesional**: UI moderna y limpia
- **Sistema de Diseño Modular**: Variables CSS centralizadas con jerarquía visual clara
- **Login de Dos Paneles**: Panel izquierdo de marca (color corporativo, ícono, tagline) + panel derecho con el formulario; en móvil se adapta a layout vertical
- **Sidebar Colapsable**: Navegación responsive con toggle integrado en la cabecera
- **Layout de Gestión**: Sidebar (logo+nav+pie usuario) + Topbar (breadcrumb + usuario/rol)
- **Topbar Compacto en Scroll (Móvil)**: Al desplazarse, la cabecera reduce contenido y muestra solo la ruta/vista actual para mejorar legibilidad
- **Versión en Pie del Sidebar**: Número de versión del `pom.xml` mostrado en tiempo de ejecución
- **Dashboard con Métricas**: Vista de inicio con tarjetas de estadísticas en tiempo real (total de usuarios, activos, inactivos, cambios de contraseña pendientes) para ADMIN
- **Badges de Estado**: Visualización clara del estado del usuario
- **Toast Notifications**: Notificaciones animadas de éxito/error con auto-dismiss (4s)
- **Diálogo de Confirmación Personalizado**: Reemplaza `window.confirm()` con modal estilizado
- **Validación en Español**: Mensajes de validación de formularios en español
- **Toggle Contraseña**: Botón para mostrar/ocultar la contraseña
- **Accesibilidad de formularios**: Atributos `name` y `autocomplete` en los campos del login para compatibilidad con gestores de contraseñas y el autocompletado del navegador
- **Generador de Contraseña Segura**: Genera contraseña aleatoria de 12 caracteres con botón de copiar al portapapeles
- **Política de Contraseña con Checklist Visual**: Al escribir una nueva contraseña se muestra una lista en tiempo real de los requisitos cumplidos/pendientes (longitud, mayúscula, minúscula, número, símbolo)
- **Página de Perfil Dedicada**: Vista "Mi Perfil" accesible desde el menú lateral con datos del usuario y selector de tema (claro/oscuro)
- **Modo Oscuro / Claro**: Selector de tema en la página de perfil con persistencia por usuario vía `ProfileService`; sincronizado con el servidor y aplicado automáticamente al iniciar sesión según la preferencia almacenada en la base de datos
- **Arquitectura Componetizada**: Organización por responsabilidades

## 🛠️ Stack Tecnológico

### Backend
- **Java 21**
- **Spring Boot 3.4.5** (v1.0.1)
- **Spring Security** con JWT
- **Spring Data JPA**
- **PostgreSQL** (única base de datos soportada)
- **BCrypt** para encriptación de contraseñas
- **Maven** para gestión de dependencias
- **Flyway** para migraciones de base de datos
- **Logging estructurado** (formato Logstash JSON, nativo Spring Boot 3.4)

### Frontend
- **React 18.3.1**
- **React Router DOM 7.17.0** para navegación
- **Vite 7.3.2** como build tool
- **Axios 1.17.0** para peticiones HTTP
- **lucide-react 1.17.0** para iconografía SVG (stroke-based, tree-shaking automático)
- **Inter** (Google Fonts) como tipografía principal
- **CSS Modular** con sistema de diseño
- **Hooks Personalizados** (useInactivityLogout)

## 📁 Estructura del Proyecto

```
SpringReact/
├── backend/                    # Aplicación Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/springreact/
│   │   │   │   ├── SpringReactApplication.java
│   │   │   │   ├── config/
│   │   │   │   │   ├── DataInitializer.java      # Inicialización de datos
│   │   │   │   │   └── RateLimitConfig.java      # Configuración rate limiting
│   │   │   │   ├── controller/
│   │   │   │   │   ├── AuthController.java       # Login, cambio de password
│   │   │   │   │   ├── ProfileController.java    # Perfil de usuario y preferencias
│   │   │   │   │   ├── UserController.java       # CRUD usuarios
│   │   │   │   │   ├── RoleController.java       # Gestión de roles
│   │   │   │   │   ├── InfoController.java       # Versión de la aplicación
│   │   │   │   │   └── SpaController.java        # Soporte SPA (React Router)
│   │   │   │   ├── service/
│   │   │   │   │   ├── AuthService.java          # Lógica de autenticación
│   │   │   │   │   ├── UserService.java          # Lógica CRUD de usuarios
│   │   │   │   │   └── ProfileService.java       # Perfil y preferencias
│   │   │   │   ├── util/
│   │   │   │   │   └── PasswordValidator.java    # Validación de contraseñas
│   │   │   │   ├── model/
│   │   │   │   │   ├── User.java                 # Entidad Usuario
│   │   │   │   │   ├── Role.java                 # Entidad Rol
│   │   │   │   │   └── UserLoginHistory.java     # Entidad historial de logins
│   │   │   │   ├── repository/
│   │   │   │   │   ├── UserRepository.java
│   │   │   │   │   ├── RoleRepository.java
│   │   │   │   │   └── UserLoginHistoryRepository.java
│   │   │   │   └── security/
│   │   │   │       ├── SecurityConfig.java       # Configuración seguridad
│   │   │   │       ├── JwtService.java           # Gestión JWT
│   │   │   │       ├── JwtAuthenticationFilter.java
│   │   │   │       └── RateLimitFilter.java      # Filtro de rate limiting
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── db/
│   │   │           └── migration/
│   │   │               ├── V1__create_tables.sql  # Esquema inicial
│   │   │               └── V2__add_theme_preference_to_users.sql  # Preferencia de tema
│   │   └── test/
│   └── pom.xml
│
└── frontend/                   # Aplicación React
    ├── index.html
    ├── src/
    │   ├── components/
    │   │   ├── auth/                  # Componentes de autenticación
    │   │   │   ├── Login.jsx
    │   │   │   ├── Login.css
    │   │   │   ├── ChangePassword.jsx
    │   │   │   └── ChangePassword.css
    │   │   ├── pages/                 # Páginas principales
    │   │   │   ├── Dashboard.jsx
    │   │   │   └── Dashboard.css
    │   │   ├── layout/                # Componentes de diseño
    │   │   │   ├── Sidebar.jsx
    │   │   │   └── Sidebar.css
    │   │   ├── features/              # Módulos CRUD
    │   │   │   ├── UserManagement.jsx
    │   │   │   └── ChangePasswordPanel.jsx
    │   │   └── common/                # Utilidades comunes
    │   │       └── PrivateRoute.jsx
    │   ├── services/
    │   │   ├── AuthService.js         # Autenticación
    │   │   ├── UserService.js         # API usuarios
    │   │   ├── RoleService.js         # API roles (usado por UserManagement para el dropdown)
    │   │   └── InfoService.js         # Versión de la aplicación
    │   ├── context/
    │   │   └── ThemeContext.jsx        # Contexto de tema oscuro/claro
    │   ├── config/
    │   └── menuConfig.js          # Ítems de menú, permisos por rol, etiquetas de vistas, APP_TITLE, APP_LOCALE
    │   ├── hooks/
    │   │   └── useInactivityLogout.js # Hook de inactividad
    │   ├── utils/
    │   │   └── passwordValidation.js  # Reglas y validador de política de contraseña
    │   ├── styles/
    │   │   ├── base/                  # Estilos base
    │   │   │   ├── variables.css
    │   │   │   ├── reset.css
    │   │   │   └── typography.css
    │   │   ├── components/            # Estilos de componentes
    │   │   │   ├── buttons.css
    │   │   │   ├── forms.css
    │   │   │   ├── cards.css
    │   │   │   └── badges.css
    │   │   ├── layout/
    │   │   │   └── container.css
    │   │   ├── utils/
    │   │   │   └── helpers.css
    │   │   └── main.css               # Importa todos los estilos
    │   ├── App.jsx
    │   ├── index.jsx
    │   └── main.jsx
    ├── package.json
    └── vite.config.js
```

## 🚀 Instalación y Ejecución

### Requisitos Previos
- **Java 21** o superior
- **Maven 3.6+**
- **Node.js 16+** y npm
- **Docker** y **Docker Compose** (para ejecución con contenedores)

### 🐳 Docker Compose (recomendado)

Levanta la aplicación completa (PostgreSQL + App) con un solo comando:

```bash
# Crear el archivo .env con el secreto JWT
echo "JWT_SECRET=<clave-base64-segura>" > .env

# Construir e iniciar todos los servicios
docker compose up --build
```

La aplicación estará disponible en: **`http://localhost:8081`**

| Servicio | Puerto host | Puerto contenedor |
|----------|-------------|-------------------|
| App (Spring Boot) | 8081 | 8080 |
| PostgreSQL | 5433 | 5432 |

> **Nota:** Para conectarte desde un cliente SQL externo (DBeaver, etc.) usa `localhost:5433`. La comunicación interna entre contenedores usa `postgres:5432`.

**Variables de entorno (`.env`):**
```env
# Obligatorio: clave de firma JWT (sin valor por defecto)
JWT_SECRET=<clave-base64-segura-minimo-32-bytes>

# Opcional: título de la aplicación (por defecto: Sistema de Gestión)
VITE_APP_TITLE=Sistema de Gestión

# Opcional: cookie segura — false para HTTP local, true en producción con HTTPS
COOKIE_SECURE=false
```

**Parar y eliminar los contenedores:**
```bash
docker compose down
# Para eliminar también el volumen de datos de PostgreSQL:
docker compose down -v
```

---

### Backend (Spring Boot)

1. Navega a la carpeta backend:
```bash
cd backend
```

2. Ejecuta la aplicación:
```bash
mvn spring-boot:run
```

O si prefieres compilar y ejecutar el JAR:
```bash
mvn clean package
java -jar target/springreact-backend-1.0.1.jar
```

El backend estará disponible en: **`http://localhost:8080`**

### Frontend (React + Vite)

1. Navega a la carpeta frontend:
```bash
cd frontend
```

2. Instala las dependencias:
```bash
npm install
```

3. Inicia el servidor de desarrollo:
```bash
npm run dev
```

El frontend estará disponible en: **`http://localhost:3000`**

> El build de producción (`npm run build`) genera los archivos directamente en `backend/src/main/resources/static/`, por lo que la aplicación completa se sirve desde el propio backend en el puerto 8080.

### 🔑 Credenciales por Defecto

Al iniciar la aplicación, se crean automáticamente:

**Usuario Administrador:**
- Email: `admin@example.com`
- Password: `admin123`
- Rol: ADMIN

**Usuario Estándar:**
- Email: `user@example.com`
- Password: `user123`
- Rol: USER

## 📡 API Endpoints

### Autenticación
| Método | Endpoint | Descripción | Acceso |
|--------|----------|-------------|--------|
| POST | `/api/auth/login` | Iniciar sesión | Público |
| POST | `/api/auth/refresh` | Renovar access token (via cookie) | Público |
| POST | `/api/auth/logout` | Cerrar sesión e invalidar cookie | Público |
| POST | `/api/auth/change-password` | Cambiar contraseña | Autenticado |

### Usuarios
| Método | Endpoint | Descripción | Acceso |
|--------|----------|-------------|--------|
| GET | `/api/users` | Obtener todos los usuarios | ADMIN |
| GET | `/api/users/{id}` | Obtener usuario por ID | ADMIN |
| POST | `/api/users` | Crear nuevo usuario | ADMIN |
| PUT | `/api/users/{id}` | Actualizar usuario | ADMIN |
| DELETE | `/api/users/{id}` | Eliminar usuario | ADMIN |
| GET | `/api/users/{id}/login-history` | Últimos 20 logins del usuario | ADMIN |
| GET | `/api/users/last-login` | Último acceso de todos los usuarios | ADMIN |

### Roles
| Método | Endpoint | Descripción | Acceso |
|--------|----------|-------------|--------|
| GET | `/api/roles` | Obtener todos los roles | Autenticado |

### Información
| Método | Endpoint | Descripción | Acceso |
|--------|----------|-------------|--------|
| GET | `/api/info/version` | Versión de la aplicación | Autenticado |

### Monitoreo (Actuator)
| Método | Endpoint | Descripción | Acceso |
|--------|----------|-------------|--------|
| GET | `/actuator/health` | Estado de la app y sus componentes | Público (detalles solo ADMIN) |
| GET | `/actuator/info` | Nombre y versión de la aplicación | ADMIN |
| GET | `/actuator/metrics` | Lista de métricas disponibles | ADMIN |
| GET | `/actuator/metrics/{nombre}` | Valor de una métrica específica | ADMIN |

### 📝 Ejemplos de Peticiones

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@example.com", "password": "admin123"}'
```

**Respuesta:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "lastLoginAt": "2026-04-30T10:23:45",
  "requiresPasswordChange": false,
  "vigencia": true,
  "id": 1,
  "name": "Administrador",
  "email": "admin@example.com",
  "role": {
    "id": 1,
    "name": "ADMIN",
    "descripcion": "Administrador del sistema con acceso completo"
  }
}
```

> El `refreshToken` **no aparece en el body**. Se envía como cookie `HttpOnly; Secure; SameSite=Strict` en el header `Set-Cookie`, inaccesible desde JavaScript.

**Refresh Token:**
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Cookie: refreshToken=<refresh-token>"
```
> El navegador envía la cookie automáticamente; en curl se pasa explícitamente con `-H "Cookie: ..."`.

**Logout:**
```bash
curl -X POST http://localhost:8080/api/auth/logout
```

**Cambiar Contraseña:**
```bash
curl -X POST http://localhost:8080/api/auth/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access-token>" \
  -d '{"currentPassword": "admin123", "newPassword": "NuevaPass1!"}'
```

**Crear Usuario (ADMIN):**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access-token-admin>" \
  -d '{"name": "Juan Pérez", "email": "juan@example.com", "password": "Pass1!word", "vigencia": true, "role": {"id": 2}}'
```

**Obtener Todos los Usuarios (ADMIN):**
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer <access-token-admin>"
```

**Obtener Usuario por ID (ADMIN):**
```bash
curl -X GET http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer <access-token-admin>"
```

**Actualizar Usuario (ADMIN):**
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access-token-admin>" \
  -d '{"name": "Admin Actualizado", "email": "admin@example.com", "vigencia": true}'
```

**Eliminar Usuario (ADMIN):**
```bash
curl -X DELETE http://localhost:8080/api/users/2 \
  -H "Authorization: Bearer <access-token-admin>"
```

**Historial de Login (ADMIN):**
```bash
curl -X GET http://localhost:8080/api/users/1/login-history \
  -H "Authorization: Bearer <access-token-admin>"
```

**Último Acceso de Todos (ADMIN):**
```bash
curl -X GET http://localhost:8080/api/users/last-login \
  -H "Authorization: Bearer <access-token-admin>"
```

**Obtener Perfil Propio:**
```bash
curl -X GET http://localhost:8080/api/profile \
  -H "Authorization: Bearer <access-token>"
```

**Actualizar Tema:**
```bash
curl -X PUT http://localhost:8080/api/profile/theme \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access-token>" \
  -d '{"theme": "dark"}'
```

**Obtener Roles:**
```bash
curl -X GET http://localhost:8080/api/roles \
  -H "Authorization: Bearer <access-token>"
```

**Versión de la Aplicación:**
```bash
curl -X GET http://localhost:8080/api/info/version \
  -H "Authorization: Bearer <access-token>"
```

**Health Check:**
```bash
curl http://localhost:8080/actuator/health
```

## 🗄️ Base de Datos

### PostgreSQL

Base de datos única soportada. Requiere una instancia de PostgreSQL en ejecución. Crear la base de datos y el usuario antes de iniciar:

```sql
CREATE USER tu_usuario WITH PASSWORD 'tu_contraseña';
CREATE DATABASE springreact OWNER tu_usuario;
GRANT ALL PRIVILEGES ON DATABASE springreact TO tu_usuario;
```

### Modelo de Datos

**Tabla: users**
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGINT | ID autoincremental |
| name | VARCHAR(100) | Nombre del usuario |
| email | VARCHAR(100) | Email único |
| password | VARCHAR(255) | Password encriptado (BCrypt) |
| requires_password_change | BOOLEAN | Requiere cambio de contraseña |
| vigencia | BOOLEAN | Usuario activo/inactivo |
| role_id | BIGINT | FK a tabla roles |

**Tabla: user_login_history**
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGINT | ID autoincremental |
| user_id | BIGINT | FK a tabla users |
| login_at | TIMESTAMP | Fecha y hora del login |
| ip_address | VARCHAR(45) | IP del cliente (soporta IPv6) |

**Tabla: roles**
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGINT | ID autoincremental |
| name | VARCHAR(50) | Nombre del rol (ADMIN, USER) |
| descripcion | VARCHAR(500) | Descripción del rol |

### Datos Iniciales

Al iniciar la aplicación, se crean automáticamente:

**Roles:**
- **ADMIN**: "Administrador del sistema"
- **USER**: "Usuario estándar"

**Usuarios:**
- **admin@example.com** (ADMIN, vigente, sin cambio de contraseña requerido)
- **user@example.com** (USER, vigente, sin cambio de contraseña requerido)

## 🎨 Sistema de Diseño

### Modo Oscuro / Claro

La aplicación implementa un sistema de temas completo:

- **`ThemeContext.jsx`**: Contexto React con estado `isDark` y función `toggleTheme`
- **Persistencia**: La preferencia se guarda en `localStorage` (clave `'theme'`)
- **Sin parpadeo**: El atributo `data-theme` se aplica en el inicializador lazy del `useState`, antes del primer render
- **Selector CSS**: Todos los colores del modo oscuro se definen bajo `[data-theme="dark"]` en `variables.css`
- **Toggle en el Sidebar**: Botón en la cabecera del sidebar con icono de sol (modo oscuro activo) o luna (modo claro activo)
- **`color-scheme`**: Declarado en `:root` y `[data-theme="dark"]`, lo que adapta los controles nativos del navegador (dropdowns, scrollbars) al tema activo
- **Fix autofill**: Mediante `-webkit-box-shadow` inset, los campos autocompletados respetan los colores del tema en lugar del fondo blanco forzado por el navegador
- **Sombras adaptadas**: Variables `--shadow-sidebar` y `--shadow-modal` con valores diferenciados (más intensas en oscuro) para mantener visibilidad en ambos temas
- **Modo claro mejorado**: Fondos diferenciados (página `#e8edf3` vs tarjetas `#ffffff`), bordes más visibles, sombras más pronunciadas y colores de texto con mayor contraste WCAG

```css
/* Modo claro (por defecto en :root) */
:root {
  color-scheme: light;
  --color-bg-primary: #ffffff;
  --color-bg-page: #e8edf3;       /* diferenciado del blanco de tarjetas */
  --color-text-primary: #1a2533;  /* mayor contraste WCAG */
  --shadow-sidebar: 2px 0 8px rgba(0, 0, 0, 0.12);
  --shadow-modal: 0 20px 25px -5px rgba(0, 0, 0, 0.14), ...;
}

/* Modo oscuro */
[data-theme="dark"] {
  color-scheme: dark;
  --color-bg-primary: #1e2433;
  --color-bg-page: #141824;
  --color-text-primary: #e2e8f0;
  --shadow-sidebar: 2px 0 16px rgba(0, 0, 0, 0.5);
  --shadow-modal: 0 20px 25px -5px rgba(0, 0, 0, 0.5), ...;
  /* ... resto de variables adaptadas ... */
}
```

### Tipografía

- **Fuente principal**: [Inter](https://fonts.google.com/specimen/Inter) cargada vía Google Fonts con `preconnect` para carga optimizada
- **Fallback**: `-apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica Neue', sans-serif`
- **Tamaño base**: `16px` definido en `html`; todos los componentes usan `em`/`rem` relativos
- **`line-height` base**: `1.6` en `body`; `1.5` en inputs y `1.3` en encabezados

### Variables CSS

El proyecto utiliza un sistema de diseño centralizado con variables CSS:

```css
/* Colores principales */
--color-primary: #2c3e50;
--color-secondary: #718096;
--color-danger: #e53e3e;
--color-info: #3182ce;
--color-warning: #f39c12;
--color-success: #38a169;

/* Espaciado */
--spacing-sm: 8px;
--spacing-md: 12px;
--spacing-lg: 16px;
--spacing-xl: 24px;

/* Sombras (modo claro) */
--shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.10);
--shadow-md: 0 4px 12px rgba(44, 62, 80, 0.18);
--shadow-lg: 0 10px 40px rgba(0, 0, 0, 0.14);
```

### Formularios e Inputs

Todos los controles de formulario están normalizados entre navegadores:

- **`::placeholder`**: Color explícito (`--color-text-muted`) con `opacity: 1` — Firefox sin esto aplica opacidad reducida
- **Select personalizado**: Flecha SVG vía `background-image` con `appearance: none`; la flecha cambia de color según el tema activo
- **Textarea**: `resize: vertical` + `min-height: 80px`
- **Estado disabled**: Fondo semántico (`--color-bg-subtle`) + texto `--color-text-muted`
- **Focus ring**: `box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.18)` — visible en ambos temas
- **`line-height: 1.5`** en todos los inputs para altura visual consistente

### Componentes Estilizados

- **Botones**: Primary, Secondary, Danger, Edit, Delete
- **Formularios**: Inputs, selects, textareas, checkboxes con validación visual y normalización cross-browser
- **Tarjetas**: Cards con header y body
- **Tarjetas de métricas**: Stat cards con color semántico por tipo (usuarios totales, activos, inactivos, pendientes)
- **Badges**: Estados activo/inactivo
- **Mensajes**: Error, success, warning, info

## 🔒 Seguridad

### Seguridad de Tokens (httpOnly Cookies)

- **`accessToken`**: Se almacena únicamente **en memoria** (variable de módulo en `AuthService.js`). No persiste en `localStorage` ni en `sessionStorage`; un XSS no puede leerlo. Se pierde al recargar la página y se restaura automáticamente usando el refresh token.
- **`refreshToken`**: El backend lo envía exclusivamente como **cookie `HttpOnly; Secure; SameSite=Strict`**. JavaScript nunca puede acceder a él. Al recargar, el browser lo envía automáticamente al endpoint `/api/auth/refresh`.
- **`user`** (datos de sesión): nombre, email, rol, etc. Se guardan en `localStorage` — no contienen datos críticos. El campo `password` está anotado con `@JsonProperty(WRITE_ONLY)` en el backend y nunca viaja al cliente.
- **Tema (oscuro/claro)**: `localStorage` key `theme` — dato no sensible.

| Dato | Almacenamiento | Accesible desde JS |
|------|---------------|--------------------|
| `accessToken` | Memoria (variable) | Sí, pero solo en la misma pestaña |
| `refreshToken` | Cookie `HttpOnly` | **No** |
| `user` (nombre, email, rol) | `localStorage` | Sí (no crítico) |
| `password` | Nunca llega al cliente | — |
| `theme` | `localStorage` | Sí (no sensible) |

**Configuración de la cookie** (`app.security.cookie.secure`):
```properties
# application-postgres.properties
# Configurable vía variable de entorno COOKIE_SECURE (false por defecto)
app.security.cookie.secure=${COOKIE_SECURE:false}
```
En desarrollo local (`http://localhost`) la variable vale `false`; en producción (HTTPS) establecer `COOKIE_SECURE=true`.

### Autenticación JWT

- **Access Token**: Válido por 10 minutos
- **Refresh Token**: Válido por 7 días (solo en cookie httpOnly)
- **Roles en Token**: El rol del usuario se incluye en el JWT
- **Encriptación**: BCrypt con fuerza 10 para contraseñas
- **Secreto externalizado**: La clave de firma JWT se lee de la variable de entorno `JWT_SECRET` (obligatoria; no tiene valor por defecto)

### Características de Seguridad

1. **Validación de Usuario Activo**: Solo usuarios con `vigencia=true` pueden iniciar sesión
2. **Cambio de Contraseña Obligatorio**: Usuarios marcados con `requiresPasswordChange=true` deben cambiar su contraseña
3. **Email Único**: Validación en backend y frontend
4. **Logout por Inactividad**: Sesión expira después de 10 minutos sin actividad y la última actividad se persiste para que el cierre también funcione si el navegador móvil suspende JavaScript
5. **Rutas Protegidas**: Componente `PrivateRoute` valida autenticación
6. **Autorización por Rol en backend**: `SecurityConfig` restringe `/api/users/**` a `ROLE_ADMIN`; `JwtAuthenticationFilter` inyecta el rol del JWT como `GrantedAuthority` de Spring Security
7. **Sin IDOR en cambio de contraseña**: El endpoint `/api/auth/change-password` obtiene el email del JWT autenticado, nunca del body de la petición
8. **Rate limit no falsificable**: `RateLimitFilter` usa `request.getRemoteAddr()` (resuelta por Tomcat con `server.forward-headers-strategy=native`) en lugar de leer `X-Forwarded-For` manualmente
9. **Política de Contraseña**: Las contraseñas nuevas deben cumplir: mínimo 8 caracteres, al menos una mayúscula, una minúscula, un número y un símbolo. Validación aplicada en frontend (tiempo real con checklist) y en backend (`PasswordValidator` usado por `AuthService` y `UserService`)

### Seguridad de URLs

#### Recursos estáticos excluidos del filtro de seguridad

Las siguientes rutas se excluyen completamente de la cadena de filtros de Spring Security mediante `WebSecurityCustomizer` (no pasan por JWT ni por rate limiting):

| Patrón | Descripción |
|--------|-------------|
| `/` | Raíz de la aplicación |
| `/index.html` | Entrada del SPA |
| `/login` | Ruta de login del SPA |
| `/favicon.ico` | Icono del sitio |
| `/assets/**` | Assets generados por Vite |
| `/static/**` | Recursos estáticos adicionales |
| `/*.css`, `/*.js` | Archivos de estilos y scripts en raíz |
| `/*.ico`, `/*.png`, `/*.jpg`, `/*.jpeg`, `/*.svg` | Imágenes en raíz |

#### Reglas de autorización de la API

Definidas en `SecurityFilterChain` con sesión **stateless** (sin sesión HTTP):

| Patrón | Acceso | Descripción |
|--------|--------|-------------|
| `/api/auth/login` | Público | Inicio de sesión |
| `/api/auth/refresh` | Público | Renovación de token (lee cookie httpOnly) |
| `/api/auth/logout` | Público | Invalidar cookie de refresh token |
| `/api/users/**` | Solo ADMIN | Gestión de usuarios (CRUD, historial) |
| `/api/**` | Autenticado (JWT) | Resto de endpoints de la API |
| `/**` (cualquier otra) | Permitido | Rutas SPA servidas por `SpaController` → `index.html` |

#### Orden de la cadena de filtros

```
Petición entrante
    │
    ▼
RateLimitFilter          ← limita intentos por IP (máx. 5 / minuto)
    │
    ▼
JwtAuthenticationFilter  ← valida y establece el contexto de seguridad
    │
    ▼
UsernamePasswordAuthenticationFilter (Spring Security)
    │
    ▼
Reglas de autorización (requestMatchers)
```

### Proxy Inverso y Headers Forwarded

La propiedad `server.forward-headers-strategy=native` indica a Spring Boot que confíe en los headers estándar de reenvío de proxy (`X-Forwarded-For`, `X-Forwarded-Proto`, `X-Forwarded-Host`, etc.) que envía el proxy inverso.

| Valor | Comportamiento |
|-------|----------------|
| `native` | Delega el procesamiento al servidor embebido (Tomcat `RemoteIpValve`). Recomendado con Railway/Nginx. |
| `framework` | Usa el `ForwardedHeaderFilter` de Spring. Más flexible, resultado equivalente. |
| `none` | Ignora los headers de proxy (por defecto si no se configura). |

Esta propiedad es necesaria para que:
- La IP real del cliente (`X-Forwarded-For`) llegue correctamente al `RateLimitFilter` y al registro de logins.
- El esquema HTTPS (`X-Forwarded-Proto`) se propague al backend, evitando que las cookies `Secure` sean rechazadas.

> **Seguridad**: Solo activar esta propiedad cuando el tráfico **siempre** pasa por el proxy (Railway, Nginx). Si la app fuera accesible directamente desde internet, un cliente malicioso podría falsificar la IP mediante estos headers.

### CORS

Configurado para desarrollo local:
- Orígenes permitidos: `http://localhost:3000`, `http://localhost:5173`, `http://localhost:8080`
- Métodos: GET, POST, PUT, DELETE, OPTIONS
- Headers permitidos: `Authorization`, `Content-Type`, `Accept`, `X-Requested-With`
- Headers expuestos: `Authorization`
- Scope: solo rutas `/api/**`
- Credentials: habilitadas

## 🎯 Funcionalidades por Rol

### ADMIN
- ✅ Gestión completa de usuarios (CRUD)
- ✅ Asignación de roles
- ✅ Activar/desactivar usuarios
- ✅ Ver historial de actividad (últimos 20 logins) de cada usuario
- ✅ Ver último acceso de todos los usuarios
- ✅ Acceso a todas las secciones

### USER
- ✅ Ver perfil propio
- ✅ Cambiar contraseña desde el menú (contraseña actual + nueva)
- ✅ Ver configuración
- ❌ No puede gestionar usuarios

## 🔧 Configuración

### Backend

La configuración del backend utiliza **Spring Profiles** para soportar múltiples motores de base de datos sin cambiar código.

**`application.properties`** (configuración común + perfil activo):
```properties
# Puerto del servidor
server.port=8080

# Perfil activo
spring.profiles.active=postgres

# JPA/Hibernate
spring.jpa.show-sql=false
spring.jackson.serialization.write-dates-as-timestamps=false

# Versión de la aplicación (inyectada desde pom.xml por Maven)
app.version=@project.version@

# JWT (requiere variable de entorno JWT_SECRET)
jwt.secret=${JWT_SECRET}

# Rate Limiting
rate-limit.max-attempts=5
rate-limit.refill-minutes=1
```

**`application-postgres.properties`** (PostgreSQL):
```properties
# DataSource
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/springreact}
spring.datasource.driverClassName=org.postgresql.Driver
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:userspringreact}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:userspringreact}

# JPA/Hibernate — Flyway gestiona el esquema; Hibernate solo valida
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# HikariCP - Pool de conexiones
spring.datasource.hikari.pool-name=HikariPool-Postgres
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.keepalive-time=60000

# Seguridad
app.security.cookie.secure=${COOKIE_SECURE:false}

# Proxy inverso (Railway/Nginx)
server.forward-headers-strategy=native
```

**Variables de entorno:**
```bash
# Obligatoria
export JWT_SECRET=<clave-base64-segura>

# Opcionales (se pueden poner en un archivo .env en la raíz del proyecto)
export VITE_APP_TITLE="Sistema de Gestión"                                    # título de la aplicación
export COOKIE_SECURE=false                                                    # true en producción con HTTPS
```

**Ejecutar:**
```bash
# Con Maven
mvn spring-boot:run

# Con el JAR
java -jar target/springreact-backend-1.0.1.jar
```

### Frontend

**Configuración de Vite (vite.config.js):**
```javascript
export default defineConfig({
  plugins: [react()],
  build: {
    outDir: '../backend/src/main/resources/static',
    emptyOutDir: true,
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

**Variables de Entorno (opcional):**
Crear `.env` en la carpeta frontend:
```env
# Título mostrado en el login y el sidebar
VITE_APP_TITLE=Sistema de Gestión
```

## 📦 Build para Producción

### Backend
```bash
cd backend
mvn clean package
```
El JAR ejecutable estará en `target/springreact-backend-1.0.1.jar`

### Frontend
```bash
cd frontend
npm run build
```
Los archivos optimizados estarán en `dist/`

## 🧪 Testing

### Backend

Tests unitarios con **JUnit 5 + Mockito** (sin contexto Spring). Los controllers delegan en una capa de servicios (`AuthService`, `UserService`, `ProfileService`) que contiene toda la lógica de negocio y está testeada por separado.

| Clase | Archivo | Tests |
|---|---|---|
| `JwtService` | `service/JwtServiceTest.java` | 16 |
| `JwtAuthenticationFilter` | `security/JwtAuthenticationFilterTest.java` | 25 |
| `RateLimitFilter` | `security/RateLimitFilterTest.java` | 5 |
| `AuthController` | `controller/AuthControllerTest.java` | 21 |
| `UserController` | `controller/UserControllerTest.java` | 19 |
| `RoleController` | `controller/RoleControllerTest.java` | 2 |
| `InfoController` | `controller/InfoControllerTest.java` | 1 |
| `SpaController` | `controller/SpaControllerTest.java` | 1 |
| `DataInitializer` | `config/DataInitializerTest.java` | 4 |
| `AuthService` | `service/AuthServiceTest.java` | 14 |
| `UserService` | `service/UserServiceTest.java` | 17 |
| `ProfileService` | `service/ProfileServiceTest.java` | 8 |
| `ArchitectureTests` | `ArchitectureTests.java` | 9 |
| **Total** | | **142** |

```bash
cd backend
mvn test -Dskip.frontend=true
```

**Cobertura con JaCoCo (mínimo 80%):**
```bash
cd backend
mvn verify -Dskip.frontend=true
```

> El goal `jacoco:check` falla el build automáticamente si la cobertura de instrucciones o ramas cae por debajo del 80%.

**Ver el informe HTML:**
```bash
# Abrir directamente en el navegador
xdg-open backend/target/site/jacoco/index.html

# O también con solo generar el informe sin ejecutar el check:
cd backend
mvn test jacoco:report -Dskip.frontend=true
```

El informe se encuentra en `backend/target/site/jacoco/index.html` y permite navegar por paquete → clase → código fuente con líneas marcadas en:
- 🟢 **Verde**: línea/rama cubierta por tests
- 🔴 **Rojo**: línea/rama no cubierta
- 🟡 **Amarillo**: rama parcialmente cubierta

Cobertura actual (clases excluidas: `SpringReactApplication`, modelos, `SecurityConfig`, `RateLimitConfig`):

| Métrica | Cobertura |
|---------|-----------|
| Instrucciones | 85% |
| Ramas | 86% |

### Validación de Arquitectura con ArchUnit

Se utiliza **ArchUnit** para validar automáticamente la arquitectura del proyecto y prevenir desvíos de diseño:

| Regla | Descripción | Estado |
|-------|-------------|--------|
| **JwtService Location** | `JwtService` debe estar en paquete `service`, no en `security` | ✅ |
| **@Service Annotation** | Clases con `@Service` solo en paquete `.service` | ✅ |
| **@Repository Annotation** | Clases con `@Repository` solo en paquete `.repository` | ✅ |
| **@Controller Annotation** | Clases con `@Controller/@RestController` solo en `.controller` | ✅ |
| **Controller Naming** | Clases en `.controller` terminan en "Controller" | ✅ |
| **Service Naming** | Clases en `.service` terminan en "Service" | ✅ |
| **Repository Naming** | Clases en `.repository` terminan en "Repository" | ✅ |
| **Layered Architecture** | Controllers → Services → Repositories (excepto casos documentados) | ✅ |

**Excepciones Documentadas:**
- `RoleController` accede directamente a `RoleRepository` (endpoint simple sin lógica compleja)
- `JwtAuthenticationFilter` accede a `UserRepository` (necesario para filtro de autenticación)

Ejecutar validación:
```bash
cd backend
mvn test -Dtest=ArchitectureTests
```

> Los tests de ArchUnit fallan automáticamente si se viola alguna regla, evitando que cambios futuros desvíen la arquitectura del proyecto.

### Frontend

Tests unitarios con **Vitest + @testing-library/react**. Cubren:

| Módulo | Archivo | Tests |
|--------|---------|-------|
| `utils/passwordValidation.js` | `test/passwordValidation.test.js` | 10 |
| `services/AuthService.js` | `test/AuthService.test.js` | 29 |
| **Total** | | **39** |

```bash
cd frontend
npm test                 # ejecutar tests (una sola vez)
npm run test:watch       # modo watch (re-ejecuta al guardar)
npm run test:coverage    # tests + informe de cobertura
```

**Ver el informe HTML:**
```bash
# Generar el informe
cd frontend
npm run test:coverage

# Abrir en el navegador
xdg-open frontend/coverage/index.html
```

El informe se encuentra en `frontend/coverage/index.html` (generado por **istanbul/v8**) y permite navegar por directorio → archivo → código fuente con el mismo código de colores que JaCoCo.

Cobertura actual de los módulos incluidos:

| Métrica | Cobertura |
|---------|-----------|
| Statements | 100% |
| Branches | 100% |
| Functions | 100% |
| Lines | 100% |

> **Nota**: El umbral mínimo del 80% aplica sobre los archivos incluidos en cobertura (`src/services/`, `src/utils/`). Los archivos excluidos explícitamente son: `main.jsx`, `index.jsx`, `src/styles/**`, `src/config/**`.

## 📚 Documentación Adicional

### Flujo de Autenticación

1. Usuario envía credenciales a `/api/auth/login`
2. Backend valida credenciales y estado `vigencia`
3. Si es válido, genera Access Token (15 min) y Refresh Token (7 días)
4. **Access Token** → devuelto en el body JSON → guardado en memoria (variable de módulo)
5. **Refresh Token** → enviado como cookie `HttpOnly; Secure; SameSite=Strict` → JS no puede leerlo
6. Cada petición incluye el Access Token en header `Authorization: Bearer {token}`
7. Al expirar el Access Token, el interceptor de Axios llama a `/api/auth/refresh`; el browser envía la cookie automáticamente
8. Backend valida la cookie y devuelve un nuevo Access Token en el body
9. Al recargar la página, `PrivateRoute` primero verifica si la sesión venció por inactividad; si aún es válida, entonces llama a `/refresh` para restaurar la sesión
10. Logout llama a `/api/auth/logout` → backend limpia la cookie (`Max-Age=0`) → frontend borra datos de `localStorage`

### Logging Estructurado

Los logs se emiten en **formato JSON** (compatible con Logstash/ELK) gracias a la configuración nativa de Spring Boot 3.4.

**Activado en** `application.properties`:
```properties
logging.structured.format.console=logstash
```

**Campos MDC incluidos por evento:**

| Evento | Campos |
|--------|--------|
| `LOGIN_SUCCESS` | `email`, `role`, `client_ip` |
| `LOGIN_FAILED` | `email`, `client_ip`, `reason` (`user_not_found` / `user_inactive` / `bad_credentials`) |
| `PASSWORD_CHANGED` | `email` |
| `USER_CREATED` | `user_id`, `email` |
| `USER_UPDATED` | `user_id`, `email` |
| `USER_DELETED` | `user_id`, `email` |

**Ejemplo de log de login exitoso:**
```json
{
  "@timestamp": "2026-05-10T10:23:45.123Z",
  "log.level": "INFO",
  "message": "Login exitoso",
  "event": "LOGIN_SUCCESS",
  "email": "admin@example.com",
  "role": "ADMIN",
  "client_ip": "192.168.1.1",
  "logger_name": "com.example.springreact.controller.AuthController"
}
```

**Archivos modificados:**
- `backend/src/main/resources/application.properties` — activa el formato JSON
- `backend/.../controller/AuthController.java` — MDC en login, login fallido y cambio de contraseña
- `backend/.../controller/UserController.java` — MDC en crear, actualizar y eliminar usuarios

### Configuración del Tiempo de Inactividad

El logout automático por inactividad está controlado en **un valor compartido y un marcador persistente**:

| Archivo | Variable | Valor actual |
|---------|----------|--------------|
| `backend/.../security/JwtService.java` | `ACCESS_TOKEN_VALIDITY` | 10 minutos |
| `frontend/src/config/sessionTimeout.js` | `INACTIVITY_TIMEOUT_MINUTES` | 10 minutos |
| `frontend/src/components/pages/Dashboard.jsx` | `useInactivityLogout(INACTIVITY_TIMEOUT_MINUTES)` | 10 minutos |
| `frontend/src/services/AuthService.js` | `lastActivityAt` en `localStorage` | última actividad |

Estos valores deben mantenerse alineados. Si el token dura más que el timer de inactividad (o viceversa), se produce una inconsistencia de seguridad; además, `PrivateRoute` bloquea la restauración por `refreshToken` cuando la inactividad ya venció.

Para cambiar el tiempo (ejemplo: 10 minutos), modificar **ambos archivos**:

```java
// JwtService.java — debe coincidir con Dashboard.jsx
private final long ACCESS_TOKEN_VALIDITY = 10 * 60 * 1000; // 10 min
```

```js
// sessionTimeout.js / Dashboard.jsx — debe coincidir con JwtService.java
export const INACTIVITY_TIMEOUT_MINUTES = 10;
useInactivityLogout(INACTIVITY_TIMEOUT_MINUTES);
```

### Flujo de Cambio de Contraseña

1. Usuario con `requiresPasswordChange=true` es redirigido a `/change-password`
2. Usuario ingresa contraseña actual y nueva contraseña
3. Frontend valida en tiempo real que la nueva contraseña cumpla la política (8+ caracteres, mayúscula, minúscula, número, símbolo) mostrando un checklist visual
4. Backend verifica usuario activo → contraseña actual correcta → política de contraseña → actualiza
5. Se marca `requiresPasswordChange=false`
6. Usuario es redirigido al dashboard

### Componentes Principales

**Frontend:**
- `Dashboard.jsx`: Contenedor principal con gestión de vistas y topbar (breadcrumb + usuario); activa el logout por inactividad con la constante compartida
- `Sidebar.jsx`: Navegación lateral con toggle integrado, filtrado por roles, usuario y versión en el pie; incluye botón de cambio de tema (sol/luna). Lee la configuración del menú desde `menuConfig.js`
- `menuConfig.js`: Fuente única de verdad para ítems de menú (`MENU_ITEMS`), título de la aplicación (`APP_TITLE`), tagline del login (`APP_TAGLINE`) y etiquetas de breadcrumb (`VIEW_LABELS`). Permisos definidos con `roles: []` (array); `[]` indica acceso universal
- `ThemeContext.jsx`: Contexto React para el tema oscuro/claro; provee `isDark` y `toggleTheme`
- `UserManagement.jsx`: CRUD de usuarios con validaciones, modal de historial de actividad y columna de último acceso
- `ChangePasswordPanel.jsx`: Panel de cambio de contraseña integrado en el dashboard (contraseña actual + nueva + confirmar), con checklist de política en tiempo real
- `Login.jsx`: Formulario de autenticación
- `ChangePassword.jsx`: Formulario de cambio de contraseña obligatorio (primer login), con checklist de política en tiempo real
- `PrivateRoute.jsx`: HOC para proteger rutas y evitar restaurar la sesión por refresh cuando la inactividad ya venció

**Backend:**
- `AuthService.java`: Lógica de autenticación (login, refresh, logout, cambio de contraseña)
- `UserService.java`: Lógica CRUD de usuarios, historial de actividad, último acceso
- `ProfileService.java`: Perfil de usuario y actualización de preferencia de tema
- `PasswordValidator.java`: Utilidad compartida de validación de política de contraseña
- `AuthController.java`: Endpoints de autenticación (delega en `AuthService`)
- `UserController.java`: CRUD de usuarios con validaciones (delega en `UserService`)
- `ProfileController.java`: Perfil y tema del usuario (delega en `ProfileService`)
- `UserLoginHistory.java`: Entidad y repositorio para el historial de logins
- `RoleController.java`: Endpoints de roles
- `InfoController.java`: Expone la versión del `pom.xml` vía `/api/info/version`
- `SecurityConfig.java`: Configuración de seguridad Spring
- `JwtService.java`: Generación y validación de tokens
- `DataInitializer.java`: Inicialización de datos por defecto

### Actuator y Health Checks

Spring Boot Actuator expone endpoints de monitoreo en `/actuator`. La configuración activa tres endpoints: `health`, `info` y `metrics`.

#### `/actuator/health` — Estado de la aplicación

Accesible **sin autenticación** (útil para health probes de Docker/Kubernetes/balanceadores):

```bash
curl http://localhost:8080/actuator/health
```

Respuesta sin autenticación (estado general):
```json
{
  "status": "UP"
}
```

Respuesta con JWT ADMIN (muestra componentes internos):
```bash
curl http://localhost:8080/actuator/health \
  -H "Authorization: Bearer <token-admin>"
```
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 321345536000,
        "threshold": 10485760
      }
    },
    "ping": { "status": "UP" }
  }
}
```

> **Estados posibles**: `UP` (todo OK), `DOWN` (algún componente falla), `OUT_OF_SERVICE` (fuera de servicio), `UNKNOWN`.

#### `/actuator/info` — Información de la aplicación

Requiere JWT de usuario ADMIN:

```bash
curl http://localhost:8080/actuator/info \
  -H "Authorization: Bearer <token-admin>"
```
```json
{
  "app": {
    "name": "SpringReact Backend",
    "version": "1.0.2"
  }
}
```

#### `/actuator/metrics` — Métricas disponibles

Requiere JWT de usuario ADMIN. Devuelve el catálogo de métricas disponibles:

```bash
curl http://localhost:8080/actuator/metrics \
  -H "Authorization: Bearer <token-admin>"
```
```json
{
  "names": [
    "hikaricp.connections",
    "hikaricp.connections.active",
    "hikaricp.connections.idle",
    "jvm.memory.used",
    "jvm.memory.max",
    "http.server.requests",
    "process.uptime",
    "system.cpu.usage",
    ...
  ]
}
```

Consultar una métrica específica:

```bash
# Conexiones activas del pool HikariCP
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active \
  -H "Authorization: Bearer <token-admin>"
```
```json
{
  "name": "hikaricp.connections.active",
  "measurements": [
    { "statistic": "VALUE", "value": 1.0 }
  ]
}
```

#### Configuración aplicada

```properties
# application.properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.endpoint.health.show-components=when-authorized
management.info.env.enabled=true
info.app.name=SpringReact Backend
info.app.version=@project.version@
```

#### Uso en Docker Compose (health probe)

El `Dockerfile` ya incluye `HEALTHCHECK` nativo y el servicio `app` en `docker-compose.yml` tiene `healthcheck` configurado. Ambos usan `wget` (disponible en `eclipse-temurin:21-jre-alpine`) para consultar el endpoint público `/actuator/health`:

```dockerfile
# Dockerfile — HEALTHCHECK nativo
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
```

```yaml
# docker-compose.yml — healthcheck del servicio app
healthcheck:
  test: ["CMD-SHELL", "wget -qO- http://localhost:8080/actuator/health || exit 1"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 60s
```

> `start_period: 60s` da margen para que Spring Boot arranque antes de que Docker empiece a contar los fallos.

## ⚠️ Consideraciones para Producción

### Base de Datos
- [x] Soporte para PostgreSQL
- [x] Configurar pool de conexiones (HikariCP)
- [x] Implementar migrations (Flyway)
- [ ] Backups automatizados

### Seguridad
- [x] Externalizar JWT secret a variables de entorno
- [x] Configurar HTTPS
- [x] Implementar rate limiting (Bucket4j)
- [x] Agregar logs de auditoría (historial de logins con IP)
- [x] Refresh token en cookie httpOnly (protegido de XSS)
- [x] Access token en memoria (no persiste en localStorage)
- [x] Headers de seguridad: CSP, XSS-Protection, Referrer-Policy
- [x] Configurar CORS para dominio de producción
- [x] RBAC en backend: `/api/users/**` restringido a ADMIN via Spring Security
- [x] Sin IDOR: email del cambio de contraseña tomado del JWT, no del body
- [x] Rate limit no falsificable: IP resuelta por Tomcat (no desde headers manuales)

### Monitoreo
- [x] Implementar logging estructurado (formato Logstash JSON con MDC)
- [x] Configurar métricas (Actuator)
- [x] Health checks
- [ ] Alertas y notificaciones

### Testing
- [x] Tests unitarios backend (JUnit 5 + Mockito, 133 tests)
- [x] Tests unitarios frontend (Vitest, 39 tests)
- [ ] Tests de integración
- [ ] Tests E2E (Cypress/Playwright)
- [x] Cobertura de código mínima 80%

### DevOps
- [x] Dockerizar aplicación (Dockerfile multi-stage + Docker Compose)
- [x] CI/CD pipeline (GitHub Actions / Jenkins)
- [ ] Kubernetes manifests
- [ ] Monitoreo con Prometheus/Grafana

## 🐛 Troubleshooting

### El backend no inicia
- Verifica que el puerto 8080 no esté en uso
- Asegúrate de tener Java 21 instalado: `java -version`
- Revisa los logs en la consola

### Error de conexión con PostgreSQL
- Verifica que PostgreSQL esté en ejecución: `pg_isready`
- Comprueba que la base de datos y el usuario existen
- Revisa las credenciales en `application-postgres.properties`
- Para usar H2 en su lugar: cambia `spring.profiles.active=h2` en `application.properties`

### El frontend no conecta con el backend
- Verifica que el backend esté ejecutándose en `http://localhost:8080`
- Revisa la configuración de CORS en `SecurityConfig.java`
- Verifica la URL en los servicios (`AuthService.js`, `UserService.js`)

### Error de autenticación
- Recarga la página — `PrivateRoute` intentará restaurar la sesión automáticamente via cookie
- Si persiste, verifica que la cookie `refreshToken` exista en DevTools → Application → Cookies
- Verifica que el usuario tenga `vigencia=true`
- En desarrollo local: asegúrate de que `app.security.cookie.secure=false` en `application-postgres.properties`

### Estilos no se aplican
- Verifica que `main.css` esté importado en `main.jsx`
- Ejecuta `npm run dev` nuevamente
- Limpia caché del navegador

### El tema oscuro/claro no se aplica correctamente
- Verifica que `ThemeProvider` envuelva la aplicación en `App.jsx`
- Limpia `localStorage` y recarga: `localStorage.removeItem('theme')`
- Comprueba que el atributo `data-theme` esté presente en `<html>` (DevTools → Inspector)

## 📄 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.