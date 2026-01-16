import api from './api'

const userService = {
  async getProfile() {
    const response = await api.get('/users/me')
    return response.data
  },

  async updateProfile(payload) {
    const response = await api.put('/users/me', payload)
    return response.data
  },

  async uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    const response = await api.post('/users/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    return response.data
  }
}

export default userService
