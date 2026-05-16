# 🚀 Spring Boot + React Enterprise Application

Aplicación web empresarial completa con autenticación JWT, sistema de roles y gestión de usuarios. Backend desarrollado con Spring Boot y frontend moderno con React 18.

## ✨ Características Principales

### 🔐 Autenticación y Seguridad
- **JWT Authentication**: Tokens de acceso y refresh
- **Sistema de Roles**: ADMIN y USER con permisos diferenciados
- **Cambio de Contraseña Obligatorio**: En el primer inicio de sesión
- **Logout Automático**: Por inactividad (15 minutos)
- **Rutas Protegidas**: Componentes protegidos con PrivateRoute
- **Validación de Usuario Activo**: Campo "vigencia" para activar/desactivar usuarios
- **Registro de Actividad**: Cada login registra fecha/hora e IP del cliente
- **Último Acceso en Login**: La respuesta del login incluye la fecha del acceso anterior (`lastLoginAt`)
- **Logging Estructurado**: Logs en formato JSON (Logstash) con campos MDC: evento, email, IP, rol

### 👥 Gestión de Usuarios
- **CRUD Completo**: Crear, leer, actualizar y eliminar usuarios
- **Asignación de Roles**: Admin puede asignar roles a usuarios
- **Email Único**: Validación frontend y backend
- **Estados de Usuario**: Activo/Inactivo con badges visuales
- **Restricción de Acceso**: Solo ADMIN puede gestionar usuarios
- **Historial de Actividad**: Modal para ver los últimos 20 logins de cada usuario (fecha + IP)
- **Último Acceso Global**: Columna de último acceso visible en la tabla de usuarios

### 🎨 Interfaz de Usuario
- **Diseño Empresarial Profesional**: UI moderna y limpia
- **Sistema de Diseño Modular**: Variables CSS centralizadas
- **Sidebar Colapsable**: Navegación responsive con toggle integrado en la cabecera
- **Layout de Gestión**: Sidebar (logo+nav+pie usuario) + Topbar (breadcrumb + usuario/rol)
- **Versión en Pie del Sidebar**: Número de versión del `pom.xml` mostrado en tiempo de ejecución
- **Badges de Estado**: Visualización clara del estado del usuario
- **Toast Notifications**: Notificaciones animadas de éxito/error con auto-dismiss (4s)
- **Diálogo de Confirmación Personalizado**: Reemplaza `window.confirm()` con modal estilizado
- **Validación en Español**: Mensajes de validación de formularios en español
- **Toggle Contraseña**: Botón para mostrar/ocultar la contraseña
- **Generador de Contraseña Segura**: Genera contraseña aleatoria de 12 caracteres con botón de copiar al portapapeles
- **Modo Oscuro / Claro**: Toggle en el sidebar para cambiar entre temas; preferencia persistida en `localStorage` y aplicada instantáneamente sin parpadeo
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
- **Logging estructurado** (formato Logstash JSON, nativo Spring Boot 3.4)

### Frontend
- **React 18.2.0**
- **React Router DOM 7.11.0** para navegación
- **Vite 7.3.2** como build tool
- **Axios 1.6.2** para peticiones HTTP
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
│   │   │   │   │   ├── UserController.java       # CRUD usuarios
│   │   │   │   │   ├── RoleController.java       # Gestión de roles
│   │   │   │   │   ├── InfoController.java       # Versión de la aplicación
│   │   │   │   │   └── SpaController.java        # Soporte SPA (React Router)
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
│   │   │       └── application.properties
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
    │   │   │   └── RoleManagement.jsx
    │   │   └── common/                # Utilidades comunes
    │   │       └── PrivateRoute.jsx
    │   ├── services/
    │   │   ├── AuthService.js         # Autenticación
    │   │   ├── UserService.js         # API usuarios
    │   │   ├── RoleService.js         # API roles
    │   │   └── InfoService.js         # Versión de la aplicación
    │   ├── context/
    │   │   └── ThemeContext.jsx        # Contexto de tema oscuro/claro
    │   ├── hooks/
    │   │   └── useInactivityLogout.js # Hook de inactividad
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

### 📝 Ejemplos de Peticiones

**Login:**
```json
POST /api/auth/login
{
  "email": "admin@example.com",
  "password": "admin123"
}
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

**Crear Usuario:**
```json
POST /api/users
Authorization: Bearer {token}
{
  "name": "Juan Pérez",
  "email": "juan@example.com",
  "password": "password123",
  "vigencia": true,
  "role": {
    "id": 2
  }
}
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

```css
/* Modo claro (por defecto en :root) */
:root {
  color-scheme: light;
  --color-bg-primary: #ffffff;
  --color-bg-page: #f5f7fa;
  --color-text-primary: #2c3e50;
  --shadow-sidebar: 2px 0 8px rgba(0, 0, 0, 0.05);
  --shadow-modal: 0 20px 25px -5px rgba(0, 0, 0, 0.1), ...;
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

/* Sombras */
--shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.06);
--shadow-md: 0 4px 12px rgba(44, 62, 80, 0.15);
--shadow-lg: 0 10px 40px rgba(0, 0, 0, 0.1);
```

### Componentes Estilizados

- **Botones**: Primary, Secondary, Danger, Edit, Delete
- **Formularios**: Inputs, selects, checkboxes con validación visual
- **Tarjetas**: Cards con header y body
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
app.security.cookie.secure=true   # Solo HTTPS (producción)
```
En desarrollo local (`http://localhost`) poner `false` para que la cookie funcione sin TLS.

### Autenticación JWT

- **Access Token**: Válido por 15 minutos
- **Refresh Token**: Válido por 7 días (solo en cookie httpOnly)
- **Roles en Token**: El rol del usuario se incluye en el JWT
- **Encriptación**: BCrypt con fuerza 10 para contraseñas
- **Secreto externalizado**: La clave de firma JWT se lee de la variable de entorno `JWT_SECRET` (obligatoria; no tiene valor por defecto)

### Características de Seguridad

1. **Validación de Usuario Activo**: Solo usuarios con `vigencia=true` pueden iniciar sesión
2. **Cambio de Contraseña Obligatorio**: Usuarios marcados con `requiresPasswordChange=true` deben cambiar su contraseña
3. **Email Único**: Validación en backend y frontend
4. **Logout por Inactividad**: Sesión expira después de 15 minutos sin actividad
5. **Rutas Protegidas**: Componente `PrivateRoute` valida autenticación
6. **Autorización por Rol**: Endpoints protegidos según rol del usuario

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
- ✅ Ver lista de roles
- ✅ Ver historial de actividad (últimos 20 logins) de cada usuario
- ✅ Ver último acceso de todos los usuarios
- ✅ Acceso a todas las secciones

### USER
- ✅ Ver perfil propio
- ✅ Cambiar contraseña
- ✅ Ver configuración
- ✅ Ver roles del sistema
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
spring.jpa.show-sql=true
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
spring.datasource.url=jdbc:postgresql://localhost:5432/springreact
spring.datasource.driverClassName=org.postgresql.Driver
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update

# HikariCP - Pool de conexiones
spring.datasource.hikari.pool-name=HikariPool-Postgres
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.keepalive-time=60000

# Cookie segura (solo HTTPS) en producción
app.security.cookie.secure=true
```

**Variable de entorno requerida:**
```bash
export JWT_SECRET=<clave-base64-segura>
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
VITE_API_URL=http://localhost:8080/api
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
```bash
cd backend
mvn test
```

### Frontend
```bash
cd frontend
npm test
```

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
9. Al recargar la página, `PrivateRoute` detecta que no hay token en memoria y llama a `/refresh` para restaurar la sesión
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

El logout automático por inactividad está controlado en **dos archivos que deben mantenerse sincronizados**:

| Archivo | Variable | Valor actual |
|---------|----------|--------------|
| `backend/.../security/JwtService.java` | `ACCESS_TOKEN_VALIDITY` | 15 minutos |
| `frontend/src/components/pages/Dashboard.jsx` | `useInactivityLogout(15)` | 15 minutos |

Ambos valores deben ser iguales. Si el token dura más que el timer de inactividad (o viceversa), se produce una inconsistencia de seguridad.

Para cambiar el tiempo (ejemplo: 10 minutos), modificar **ambos archivos**:

```java
// JwtService.java — debe coincidir con Dashboard.jsx
private final long ACCESS_TOKEN_VALIDITY = 10 * 60 * 1000; // 10 min
```

```js
// Dashboard.jsx — debe coincidir con JwtService.java
useInactivityLogout(10);
```

### Flujo de Cambio de Contraseña

1. Usuario con `requiresPasswordChange=true` es redirigido a `/change-password`
2. Usuario ingresa contraseña actual y nueva contraseña
3. Backend valida contraseña actual y actualiza
4. Se marca `requiresPasswordChange=false`
5. Usuario es redirigido al dashboard

### Componentes Principales

**Frontend:**
- `Dashboard.jsx`: Contenedor principal con gestión de vistas y topbar (breadcrumb + usuario)
- `Sidebar.jsx`: Navegación lateral con toggle integrado, filtrado por roles, usuario y versión en el pie; incluye botón de cambio de tema (sol/luna)
- `ThemeContext.jsx`: Contexto React para el tema oscuro/claro; provee `isDark` y `toggleTheme`
- `UserManagement.jsx`: CRUD de usuarios con validaciones, modal de historial de actividad y columna de último acceso
- `RoleManagement.jsx`: Vista de roles del sistema
- `Login.jsx`: Formulario de autenticación
- `ChangePassword.jsx`: Formulario de cambio de contraseña
- `PrivateRoute.jsx`: HOC para proteger rutas

**Backend:**
- `AuthController.java`: Endpoints de autenticación (registra IP y fecha en cada login)
- `UserController.java`: CRUD de usuarios con validaciones y endpoints de historial de actividad
- `UserLoginHistory.java`: Entidad y repositorio para el historial de logins
- `RoleController.java`: Endpoints de roles
- `InfoController.java`: Expone la versión del `pom.xml` vía `/api/info/version`
- `SecurityConfig.java`: Configuración de seguridad Spring
- `JwtService.java`: Generación y validación de tokens
- `DataInitializer.java`: Inicialización de datos por defecto

## ⚠️ Consideraciones para Producción

### Base de Datos
- [x] Soporte para PostgreSQL
- [x] Configurar pool de conexiones (HikariCP)
- [ ] Implementar migrations (Flyway/Liquibase)
- [ ] Backups automatizados

### Seguridad
- [ ] Externalizar JWT secret a variables de entorno
- [ ] Configurar HTTPS
- [x] Implementar rate limiting (Bucket4j)
- [x] Agregar logs de auditoría (historial de logins con IP)
- [x] Refresh token en cookie httpOnly (protegido de XSS)
- [x] Access token en memoria (no persiste en localStorage)
- [x] Headers de seguridad: CSP, XSS-Protection, Referrer-Policy
- [ ] Configurar CORS para dominio de producción

### Monitoreo
- [x] Implementar logging estructurado (formato Logstash JSON con MDC)
- [ ] Configurar métricas (Actuator)
- [ ] Health checks
- [ ] Alertas y notificaciones

### Testing
- [ ] Tests unitarios (JUnit + Jest)
- [ ] Tests de integración
- [ ] Tests E2E (Cypress/Playwright)
- [ ] Cobertura de código mínima 80%

### DevOps
- [ ] Dockerizar aplicación
- [ ] CI/CD pipeline (GitHub Actions / Jenkins)
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