import axios from 'axios';

const API_URL = '/api/auth';

// accessToken en memoria: inaccesible desde XSS
let _accessToken = null;

class AuthService {
  async login(email, password) {
    const response = await axios.post(`${API_URL}/login`, { email, password });
    const { accessToken, ...userData } = response.data;

    // accessToken solo en memoria; refreshToken llega como cookie httpOnly (invisible a JS)
    _accessToken = accessToken;
    localStorage.setItem('user', JSON.stringify(userData));

    return response.data;
  }

  async refreshAccessToken() {
    try {
      // Sin body: el browser envía la cookie httpOnly automáticamente
      const response = await axios.post(`${API_URL}/refresh`, {});
      const { accessToken } = response.data;

      _accessToken = accessToken;
      return accessToken;
    } catch (error) {
      this.logout();
      return null;
    }
  }

  async logout() {
    try {
      // El backend invalida la cookie del refresh token
      await axios.post(`${API_URL}/logout`, {});
    } catch (_) {
      // Ignorar errores de red en logout
    }
    _accessToken = null;
    localStorage.removeItem('user');
    window.location.href = '/login';
  }

  async changePassword(currentPassword, newPassword) {
    // El email lo extrae el backend del JWT — no se envía en el body
    const response = await axios.post(`${API_URL}/change-password`, {
      currentPassword,
      newPassword
    });
    
    return response.data;
  }

  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      return JSON.parse(userStr);
    }
    return null;
  }

  getAccessToken() {
    return _accessToken;
  }

  isTokenExpired(token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      return payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }

  isAuthenticated() {
    const token = _accessToken;
    if (!token) return false;
    if (this.isTokenExpired(token)) {
      _accessToken = null;
      return false;
    }
    return true;
  }

  setupAxiosInterceptor() {
    // Interceptor para agregar token a todas las peticiones
    axios.interceptors.request.use(
      (config) => {
        const token = this.getAccessToken();
        if (token) {
          config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Interceptor para manejar errores 401 y renovar token
    axios.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config;

        // Si es error 401 y no es una petición de refresh
        if (error.response?.status === 401 && !originalRequest._retry && !originalRequest.url.includes('/refresh')) {
          originalRequest._retry = true;

          const newToken = await this.refreshAccessToken();
          
          if (newToken) {
            originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
            return axios(originalRequest);
          } else {
            // Si no se pudo renovar el token, redirigir a login
            this.logout();
            return Promise.reject(error);
          }
        }

        // Si es error 403 (Forbidden) y NO es una ruta de autenticación, redirigir
        if (error.response?.status === 403 && 
            !originalRequest.url.includes('/login') && 
            !originalRequest.url.includes('/auth')) {
          this.logout();
          return Promise.reject(error);
        }

        // Para errores de autenticación (login), dejar que el componente los maneje
        return Promise.reject(error);
      }
    );
  }
}

const authService = new AuthService();
authService.setupAxiosInterceptor();
export default authService;
