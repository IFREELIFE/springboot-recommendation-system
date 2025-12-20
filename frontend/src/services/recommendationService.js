import api from './api'

const recommendationService = {
  async getRecommendations(limit = 10) {
    const response = await api.get('/recommendations', { params: { limit } })
    return response.data
  },

  async getCollaborativeRecommendations(limit = 10) {
    const response = await api.get('/recommendations/collaborative', { params: { limit } })
    return response.data
  },

  async getContentBasedRecommendations(limit = 10) {
    const response = await api.get('/recommendations/content-based', { params: { limit } })
    return response.data
  }
}

export default recommendationService
