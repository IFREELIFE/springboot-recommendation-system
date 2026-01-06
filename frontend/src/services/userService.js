import api from './api'

const userService = {
  async getProfile() {
    const response = await api.get('/users/me')
    return response.data
  },

  async updateProfile(payload) {
    const response = await api.put('/users/me', payload)
    return response.data
  }
}

export default userService
