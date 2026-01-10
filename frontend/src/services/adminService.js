import api from './api'

const adminService = {
  async fetchUsers({ role, page = 0, size = 10 }) {
    const params = { page, size }
    if (role && role !== 'ALL') {
      params.role = role
    }
    const response = await api.get('/admin/users', { params })
    return response.data
  },

  async freezeUser(id, freeze = true) {
    const response = await api.put(`/admin/users/${id}/freeze`, null, {
      params: { freeze }
    })
    return response.data
  },

  async fetchOccupancy({ landlordId, page = 0, size = 10 }) {
    const params = { page, size }
    if (landlordId) {
      params.landlordId = landlordId
    }
    const response = await api.get('/admin/properties/occupancy', { params })
    return response.data
  },

  async freezeProperty(id, freeze = true) {
    const response = await api.put(`/admin/properties/${id}/freeze`, null, {
      params: { freeze }
    })
    return response.data
  }
}

export default adminService
