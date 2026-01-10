import api from './api'

const propertyService = {
  async getProperties(params) {
    const response = await api.get('/properties', { params })
    return response.data
  },

  async getPropertyById(id) {
    const response = await api.get(`/properties/${id}`)
    return response.data
  },

  async searchProperties(params) {
    const response = await api.get('/properties/search', { params })
    return response.data
  },

  async getPopularProperties() {
    const response = await api.get('/properties/popular')
    return response.data
  },

  async getTopRatedProperties() {
    const response = await api.get('/properties/top-rated')
    return response.data
  },

  async createProperty(propertyData) {
    const response = await api.post('/properties', propertyData)
    return response.data
  },

  async updateProperty(id, propertyData) {
    const response = await api.put(`/properties/${id}`, propertyData)
    return response.data
  },

  async uploadImages(files) {
    const formData = new FormData()
    files.forEach((file) => {
      formData.append('files', file)
    })
    const response = await api.post('/properties/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    return response.data
  },

  async deleteProperty(id) {
    const response = await api.delete(`/properties/${id}`)
    return response.data
  },

  async getMyProperties(params) {
    const response = await api.get('/properties/landlord/my-properties', { params })
    return response.data
  },

  async getPropertyOccupancy(params) {
    const response = await api.get('/properties/landlord/occupancy', { params })
    return response.data
  }
}

export default propertyService
