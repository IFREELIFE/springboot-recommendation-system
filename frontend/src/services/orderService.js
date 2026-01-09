import api from './api'

const orderService = {
  async createOrder(orderData) {
    const response = await api.post('/orders', orderData)
    return response.data
  },

  async getMyOrders(params) {
    const response = await api.get('/orders/my-orders', { params })
    return response.data
  },

  async getOrderById(id) {
    const response = await api.get(`/orders/${id}`)
    return response.data
  },

  async updateOrderStatus(id, status) {
    const response = await api.put(`/orders/${id}/status`, null, { params: { status } })
    return response.data
  },

  async cancelOrder(id) {
    const response = await api.delete(`/orders/${id}`)
    return response.data
  },

  async getLandlordOrders(params) {
    const response = await api.get('/orders/landlord', { params })
    return response.data
  },

  async reviewCancellation(id, approve) {
    const response = await api.post(`/orders/${id}/review`, null, { params: { approve } })
    return response.data
  }
}

export default orderService
