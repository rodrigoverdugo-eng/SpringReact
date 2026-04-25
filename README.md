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

### 👥 Gestión de Usuarios
- **CRUD Completo**: Crear, leer, actualizar y eliminar usuarios
- **Asignación de Roles**: Admin puede asignar roles a usuarios
- **Email Único**: Validación frontend y backend
- **Estados de Usuario**: Activo/Inactivo con badges visuales
- **Restricción de Acceso**: Solo ADMIN puede gestionar usuarios

### 🎨 Interfaz de Usuario
- **Diseño Empresarial Profesional**: UI moderna y limpia
- **Sistema de Diseño Modular**: Variables CSS centralizadas
- **Sidebar Colapsable**: Navegación responsive
- **Badges de Estado**: Visualización clara del estado del usuario
- **Toast Notifications**: Notificaciones animadas de éxito/error con auto-dismiss (4s)
- **Diálogo de Confirmación Personalizado**: Reemplaza `window.confirm()` con modal estilizado
- **Validación en Español**: Mensajes de validación de formularios en español
- **Toggle Contraseña**: Botón para mostrar/ocultar la contraseña
- **Generador de Contraseña Segura**: Genera contraseña aleatoria de 12 caracteres con botón de copiar al portapapeles
- **Arquitectura Componetizada**: Organización por responsabilidades

## 🛠️ Stack Tecnológico

### Backend
- **Java 21**
- **Spring Boot 3.4.5**
- **Spring Security** con JWT
- **Spring Data JPA**
- **H2 Database** (en memoria)
- **BCrypt** para encriptación de contraseñas
- **Maven** para gestión de dependencias

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
│   │   │   │   │   └── SpaController.java        # Soporte SPA (React Router)
│   │   │   │   ├── model/
│   │   │   │   │   ├── User.java                 # Entidad Usuario
│   │   │   │   │   └── Role.java                 # Entidad Rol
│   │   │   │   ├── repository/
│   │   │   │   │   ├── UserRepository.java
│   │   │   │   │   └── RoleRepository.java
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
    │   │   └── RoleService.js         # API roles
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
java -jar target/springreact-backend-0.0.1-SNAPSHOT.jar
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
| POST | `/api/auth/change-password` | Cambiar contraseña | Autenticado |
| POST | `/api/auth/refresh` | Renovar token | Autenticado |

### Usuarios
| Método | Endpoint | Descripción | Acceso |
|--------|----------|-------------|--------|
| GET | `/api/users` | Obtener todos los usuarios | ADMIN |
| GET | `/api/users/{id}` | Obtener usuario por ID | ADMIN |
| POST | `/api/users` | Crear nuevo usuario | ADMIN |
| PUT | `/api/users/{id}` | Actualizar usuario | ADMIN |
| DELETE | `/api/users/{id}` | Eliminar usuario | ADMIN |

### Roles
| Método | Endpoint | Descripción | Acceso |
|--------|----------|-------------|--------|
| GET | `/api/roles` | Obtener todos los roles | Autenticado |

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
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "name": "Administrador",
    "email": "admin@example.com",
    "requiresPasswordChange": false,
    "vigencia": true,
    "role": {
      "id": 1,
      "name": "ADMIN",
      "descripcion": "Administrador del sistema con acceso completo"
    }
  }
}
```

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

### H2 Database (Desarrollo)

Consola H2 disponible en: **`http://localhost:8080/h2-console`**

**Configuración:**
- JDBC URL: `jdbc:h2:mem:testdb`
- Usuario: `sa`
- Contraseña: (vacía)

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

**Tabla: roles**
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGINT | ID autoincremental |
| name | VARCHAR(50) | Nombre del rol (ADMIN, USER) |
| descripcion | VARCHAR(500) | Descripción del rol |

### Datos Iniciales

Al iniciar la aplicación, se crean automáticamente:

**Roles:**
- **ADMIN**: "Administrador del sistema con acceso completo"
- **USER**: "Usuario estándar con permisos básicos"

**Usuarios:**
- **admin@example.com** (ADMIN, vigente, sin cambio de contraseña requerido)
- **user@example.com** (USER, vigente, sin cambio de contraseña requerido)

## 🎨 Sistema de Diseño

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

### Autenticación JWT

- **Access Token**: Válido por 1 hora
- **Refresh Token**: Válido por 7 días
- **Roles en Token**: El rol del usuario se incluye en el JWT
- **Encriptación**: BCrypt con fuerza 10 para contraseñas

### Características de Seguridad

1. **Validación de Usuario Activo**: Solo usuarios con `vigencia=true` pueden iniciar sesión
2. **Cambio de Contraseña Obligatorio**: Usuarios marcados con `requiresPasswordChange=true` deben cambiar su contraseña
3. **Email Único**: Validación en backend y frontend
4. **Logout por Inactividad**: Sesión expira después de 15 minutos sin actividad
5. **Rutas Protegidas**: Componente `PrivateRoute` valida autenticación
6. **Autorización por Rol**: Endpoints protegidos según rol del usuario

### CORS

Configurado para desarrollo local:
- Origen permitido: `http://localhost:5173`
- Métodos: GET, POST, PUT, DELETE
- Headers: Authorization, Content-Type

## 🎯 Funcionalidades por Rol

### ADMIN
- ✅ Gestión completa de usuarios (CRUD)
- ✅ Asignación de roles
- ✅ Activar/desactivar usuarios
- ✅ Ver lista de roles
- ✅ Acceso a todas las secciones

### USER
- ✅ Ver perfil propio
- ✅ Cambiar contraseña
- ✅ Ver configuración
- ✅ Ver roles del sistema
- ❌ No puede gestionar usuarios

## 🔧 Configuración

### Backend

**application.properties:**
```properties
# Puerto del servidor
server.port=8080

# Base de datos H2
spring.datasource.url=jdbc:h2:mem:testdb
spring.h2.console.enabled=true

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Frontend

**Configuración de Vite (vite.config.js):**
```javascript
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
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
El JAR ejecutable estará en `target/springreact-backend-0.0.1-SNAPSHOT.jar`

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
3. Si es válido, genera Access Token (1h) y Refresh Token (7d)
4. Frontend almacena tokens en localStorage
5. Cada petición incluye Access Token en header `Authorization: Bearer {token}`
6. Si Access Token expira, frontend usa Refresh Token para renovar
7. Logout elimina tokens del localStorage

### Flujo de Cambio de Contraseña

1. Usuario con `requiresPasswordChange=true` es redirigido a `/change-password`
2. Usuario ingresa contraseña actual y nueva contraseña
3. Backend valida contraseña actual y actualiza
4. Se marca `requiresPasswordChange=false`
5. Usuario es redirigido al dashboard

### Componentes Principales

**Frontend:**
- `Dashboard.jsx`: Contenedor principal con gestión de vistas
- `Sidebar.jsx`: Navegación lateral con filtrado por roles
- `UserManagement.jsx`: CRUD de usuarios con validaciones
- `RoleManagement.jsx`: Vista de roles del sistema
- `Login.jsx`: Formulario de autenticación
- `ChangePassword.jsx`: Formulario de cambio de contraseña
- `PrivateRoute.jsx`: HOC para proteger rutas

**Backend:**
- `AuthController.java`: Endpoints de autenticación
- `UserController.java`: CRUD de usuarios con validaciones
- `RoleController.java`: Endpoints de roles
- `SecurityConfig.java`: Configuración de seguridad Spring
- `JwtService.java`: Generación y validación de tokens
- `DataInitializer.java`: Inicialización de datos por defecto

## ⚠️ Consideraciones para Producción

### Base de Datos
- [ ] Migrar de H2 a PostgreSQL/MySQL
- [ ] Configurar pool de conexiones
- [ ] Implementar migrations (Flyway/Liquibase)
- [ ] Backups automatizados

### Seguridad
- [ ] Externalizar JWT secret a variables de entorno
- [ ] Configurar HTTPS
- [x] Implementar rate limiting (Bucket4j)
- [ ] Agregar logs de auditoría
- [ ] Configurar CORS para dominio de producción

### Monitoreo
- [ ] Implementar logging estructurado
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

### El frontend no conecta con el backend
- Verifica que el backend esté ejecutándose en `http://localhost:8080`
- Revisa la configuración de CORS en `SecurityConfig.java`
- Verifica la URL en los servicios (`AuthService.js`, `UserService.js`)

### Error de autenticación
- Limpia localStorage: `localStorage.clear()`
- Verifica que las credenciales sean correctas
- Revisa que el usuario tenga `vigencia=true`

### Estilos no se aplican
- Verifica que `main.css` esté importado en `main.jsx`
- Ejecuta `npm run dev` nuevamente
- Limpia caché del navegador

## 📞 Soporte

Para reportar bugs o solicitar nuevas características, por favor abre un issue en el repositorio.

## 📄 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

---

**Desarrollado con ❤️ usando Spring Boot y React**
