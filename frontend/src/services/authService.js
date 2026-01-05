import api from './api'

const authService = {
  async register(userData) {
    const response = await api.post('/auth/register', userData)
    return response.data
  },

  async login(credentials) {
    const response = await api.post('/auth/login', credentials)
    if (response.data.success) {
      const { token, id, username, email, role } = response.data.data
      localStorage.setItem('token', token)
      return { id, username, email, role, token }
    }
    throw new Error(response.data.message)
  },

  logout() {
    localStorage.removeItem('token')
  },

  getCurrentUser() {
    const token = localStorage.getItem('token')
    if (token) {
      try {
        // Decode JWT token manually (basic implementation)
        const base64Url = token.split('.')[1]
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
        }).join(''))
        
        return JSON.parse(jsonPayload)
      } catch (error) {
        return null
      }
    }
    return null
  }
}

export default authService
