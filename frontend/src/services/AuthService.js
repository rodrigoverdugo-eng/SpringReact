import axios from 'axios';

const API_URL = '/api/auth';

class AuthService {
  async login(email, password) {
    const response = await axios.post(`${API_URL}/login`, { email, password });
    const { accessToken, refreshToken, ...userData } = response.data;
    
    // Guardar tokens y datos del usuario
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('user', JSON.stringify(userData));
    
    return response.data;
  }

  async register(userData) {
    const response = await axios.post(`${API_URL}/register`, userData);
    const { accessToken, refreshToken, ...user } = response.data;
    
    // Guardar tokens y datos del usuario
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('user', JSON.stringify(user));
    
    return response.data;
  }

  async refreshAccessToken() {
    const refreshToken = localStorage.getItem('refreshToken');
    
    if (!refreshToken) {
      this.logout();
      return null;
    }
    
    try {
      const response = await axios.post(`${API_URL}/refresh`, { refreshToken });
      const { accessToken } = response.data;
      
      localStorage.setItem('accessToken', accessToken);
      return accessToken;
    } catch (error) {
      this.logout();
      return null;
    }
  }

  logout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    window.location.href = '/login';
  }

  async changePassword(currentPassword, newPassword) {
    const user = this.getCurrentUser();
    if (!user) {
      throw new Error('Usuario no autenticado');
    }
    
    const response = await axios.post(`${API_URL}/change-password`, {
      email: user.email,
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
    return localStorage.getItem('accessToken');
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
    const token = this.getAccessToken();
    if (!token) return false;
    if (this.isTokenExpired(token)) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
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
