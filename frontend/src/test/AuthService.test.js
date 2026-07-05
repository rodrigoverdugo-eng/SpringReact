// @vitest-environment happy-dom
import { describe, it, expect, vi, beforeEach } from 'vitest'
import axios from 'axios'
import authService from '../services/AuthService'

// Mock de axios (Vitest lo hoista automáticamente)
vi.mock('axios', () => {
  // axios es una función callable que también tiene métodos
  const mockAxios = vi.fn().mockResolvedValue({ data: {} })
  mockAxios.post = vi.fn()
  mockAxios.get = vi.fn()
  mockAxios.interceptors = {
    request: { use: vi.fn() },
    response: { use: vi.fn() },
  }
  mockAxios.defaults = { headers: { common: {} } }
  return { default: mockAxios }
})

// Mock de localStorage compatible con Node.js v26
const localStorageMock = (() => {
  let store = {}
  return {
    getItem: (key) => store[key] ?? null,
    setItem: (key, value) => { store[key] = String(value) },
    removeItem: (key) => { delete store[key] },
    clear: () => { store = {} },
  }
})()
vi.stubGlobal('localStorage', localStorageMock)

describe('AuthService', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  describe('getCurrentUser', () => {
    it('retorna null cuando no hay usuario en localStorage', () => {
      expect(authService.getCurrentUser()).toBeNull()
    })

    it('retorna el usuario guardado en localStorage', () => {
      const user = { id: 1, name: 'Admin', email: 'admin@example.com', role: { name: 'ADMIN' } }
      localStorage.setItem('user', JSON.stringify(user))
      expect(authService.getCurrentUser()).toEqual(user)
    })
  })

  describe('getAccessToken', () => {
    it('el getter existe y retorna un valor (null inicialmente)', () => {
      expect(authService.getAccessToken).toBeDefined()
      // Sin login previo el token en memoria es null
      expect(authService.getAccessToken()).toBeNull()
    })
  })

  describe('isTokenExpired', () => {
    it('retorna true para un token con formato inválido', () => {
      expect(authService.isTokenExpired('invalid.token')).toBe(true)
    })

    it('retorna true para un token vacío', () => {
      expect(authService.isTokenExpired('')).toBe(true)
    })

    it('retorna true para un token expirado', () => {
      const expiredPayload = btoa(JSON.stringify({ exp: 1 }))
      const expiredToken = `header.${expiredPayload}.signature`
      expect(authService.isTokenExpired(expiredToken)).toBe(true)
    })

    it('retorna false para un token vigente', () => {
      const futureExp = Math.floor(Date.now() / 1000) + 3600
      const validPayload = btoa(JSON.stringify({ exp: futureExp }))
      const validToken = `header.${validPayload}.signature`
      expect(authService.isTokenExpired(validToken)).toBe(false)
    })
  })

  describe('login', () => {
    it('llama a la API y guarda los datos (sin accessToken) en localStorage', async () => {
      const mockResponse = {
        data: {
          accessToken: 'mock-access-token',
          id: 1,
          name: 'Admin',
          email: 'admin@example.com',
          role: { id: 1, name: 'ADMIN' },
        },
      }
      axios.post.mockResolvedValueOnce(mockResponse)

      const result = await authService.login('admin@example.com', 'admin123')

      expect(axios.post).toHaveBeenCalledWith('/api/auth/login', {
        email: 'admin@example.com',
        password: 'admin123',
      })
      expect(result).toEqual(mockResponse.data)

      const stored = JSON.parse(localStorage.getItem('user'))
      expect(stored).not.toHaveProperty('accessToken')
      expect(stored.email).toBe('admin@example.com')
    })

    it('propaga el error si la API falla', async () => {
      axios.post.mockRejectedValueOnce(new Error('Network Error'))
      await expect(authService.login('bad@example.com', 'wrong')).rejects.toThrow('Network Error')
    })
  })

  describe('logout', () => {
    it('limpia localStorage al hacer logout', async () => {
      localStorage.setItem('user', JSON.stringify({ name: 'Admin' }))
      axios.post.mockResolvedValueOnce({})

      await authService.logout()

      expect(localStorage.getItem('user')).toBeNull()
    })

    it('limpia localStorage aunque la petición de logout falle', async () => {
      localStorage.setItem('user', JSON.stringify({ name: 'Admin' }))
      axios.post.mockRejectedValueOnce(new Error('Network Error'))

      await authService.logout()

      expect(localStorage.getItem('user')).toBeNull()
    })
  })

  describe('refreshAccessToken', () => {
    it('renueva el token correctamente y lo guarda en memoria', async () => {
      const futureExp = Math.floor(Date.now() / 1000) + 3600
      const payload = btoa(JSON.stringify({ exp: futureExp }))
      const newToken = `header.${payload}.sig`

      axios.post.mockResolvedValueOnce({ data: { accessToken: newToken } })

      const result = await authService.refreshAccessToken()

      expect(axios.post).toHaveBeenCalledWith('/api/auth/refresh', {})
      expect(result).toBe(newToken)
      expect(authService.getAccessToken()).toBe(newToken)
    })

    it('llama a logout y retorna null si la renovación falla', async () => {
      axios.post
        .mockRejectedValueOnce(new Error('Unauthorized')) // refresh falla
        .mockResolvedValueOnce({})                         // logout (best-effort)

      const result = await authService.refreshAccessToken()

      expect(result).toBeNull()
      expect(localStorage.getItem('user')).toBeNull()
    })
  })

  describe('changePassword', () => {
    it('llama al endpoint correcto con las contraseñas', async () => {
      axios.post.mockResolvedValueOnce({ data: { message: 'ok' } })

      const result = await authService.changePassword('oldPass1!', 'NewPass1!')

      expect(axios.post).toHaveBeenCalledWith('/api/auth/change-password', {
        currentPassword: 'oldPass1!',
        newPassword: 'NewPass1!',
      })
      expect(result).toEqual({ message: 'ok' })
    })

    it('propaga el error si el endpoint falla', async () => {
      axios.post.mockRejectedValueOnce(new Error('Wrong password'))
      await expect(authService.changePassword('bad', 'NewPass1!')).rejects.toThrow('Wrong password')
    })
  })

  describe('isAuthenticated', () => {
    it('retorna false cuando no hay token en memoria', async () => {
      // Asegurar que no haya token: hacer logout
      axios.post.mockResolvedValueOnce({})
      await authService.logout()
      expect(authService.isAuthenticated()).toBe(false)
    })

    it('retorna true cuando hay un token vigente', async () => {
      const futureExp = Math.floor(Date.now() / 1000) + 3600
      const payload = btoa(JSON.stringify({ exp: futureExp }))
      const validToken = `header.${payload}.sig`

      // Establecer token mediante login
      axios.post.mockResolvedValueOnce({
        data: { accessToken: validToken, id: 1, name: 'Admin', email: 'a@b.com', role: { name: 'ADMIN' } },
      })
      await authService.login('a@b.com', 'pass')

      expect(authService.isAuthenticated()).toBe(true)
    })

    it('retorna false y limpia el token cuando está expirado', async () => {
      // Login con token expiado
      const expiredPayload = btoa(JSON.stringify({ exp: 1 }))
      const expiredToken = `header.${expiredPayload}.sig`

      axios.post.mockResolvedValueOnce({
        data: { accessToken: expiredToken, id: 1, name: 'Admin', email: 'a@b.com', role: { name: 'ADMIN' } },
      })
      await authService.login('a@b.com', 'pass')

      expect(authService.isAuthenticated()).toBe(false)
      expect(authService.getAccessToken()).toBeNull()
    })
  })

  describe('setupAxiosInterceptor (interceptores capturados)', () => {
    let reqSuccess, reqError, resSuccess, resError

    beforeEach(() => {
      // Re-registrar para capturar callbacks tras vi.clearAllMocks()
      authService.setupAxiosInterceptor()
      ;[reqSuccess, reqError] = axios.interceptors.request.use.mock.calls[0]
      ;[resSuccess, resError] = axios.interceptors.response.use.mock.calls[0]
    })

    it('el interceptor de request añade Authorization cuando hay token', () => {
      const futureExp = Math.floor(Date.now() / 1000) + 3600
      const fakeToken = `h.${btoa(JSON.stringify({ exp: futureExp }))}.s`
      vi.spyOn(authService, 'getAccessToken').mockReturnValueOnce(fakeToken)
      const result = reqSuccess({ headers: {} })
      expect(result.headers['Authorization']).toBe(`Bearer ${fakeToken}`)
    })

    it('el interceptor de request no añade Authorization cuando no hay token', () => {
      vi.spyOn(authService, 'getAccessToken').mockReturnValueOnce(null)
      const config = { headers: {} }
      const result = reqSuccess(config)
      expect(result.headers['Authorization']).toBeUndefined()
    })

    it('el error callback de request rechaza la promesa', async () => {
      const err = new Error('request error')
      await expect(reqError(err)).rejects.toThrow('request error')
    })

    it('el callback de response éxito pasa la respuesta directamente', () => {
      const response = { data: { ok: true } }
      expect(resSuccess(response)).toBe(response)
    })

    it('error 401 sin retry: renueva token y reintenta la petición', async () => {
      const futureExp = Math.floor(Date.now() / 1000) + 3600
      const newToken = `h.${btoa(JSON.stringify({ exp: futureExp }))}.s`

      // refreshAccessToken llama a axios.post('/api/auth/refresh')
      axios.post.mockResolvedValueOnce({ data: { accessToken: newToken } })
      // Llamada de retry: axios(originalRequest)
      axios.mockResolvedValueOnce({ data: { retried: true } })

      const error = {
        config: { url: '/api/users', _retry: false, headers: {} },
        response: { status: 401 },
      }
      const result = await resError(error)
      expect(result.data.retried).toBe(true)
    })

    it('error 401 sin retry: si no hay nuevo token hace logout', async () => {
      // refreshAccessToken falla
      axios.post
        .mockRejectedValueOnce(new Error('cookie expired'))  // refresh
        .mockResolvedValueOnce({})                            // logout

      const error = {
        config: { url: '/api/users', _retry: false, headers: {} },
        response: { status: 401 },
      }
      await expect(resError(error)).rejects.toBeDefined()
      expect(localStorage.getItem('user')).toBeNull()
    })

    it('error 403 en ruta no-auth hace logout', async () => {
      axios.post.mockResolvedValueOnce({}) // logout
      const error = {
        config: { url: '/api/users', headers: {} },
        response: { status: 403 },
      }
      await expect(resError(error)).rejects.toBeDefined()
    })

    it('otros errores se rechazan sin logout', async () => {
      const error = {
        config: { url: '/api/data', headers: {} },
        response: { status: 500 },
      }
      await expect(resError(error)).rejects.toBeDefined()
    })
  })
})

