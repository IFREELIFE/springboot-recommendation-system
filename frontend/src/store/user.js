import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authService from '../services/authService'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token'))

  const isAuthenticated = computed(() => !!token.value)
  const isLandlord = computed(() => user.value?.role === 'ROLE_LANDLORD' || user.value?.role === 'ROLE_ADMIN')

  function setUser(userData) {
    user.value = userData
  }

  function setToken(newToken) {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  function checkAuth() {
    const savedToken = localStorage.getItem('token')
    if (savedToken) {
      token.value = savedToken
      // In a real app, you'd validate the token with the server
      const userData = authService.getCurrentUser()
      if (userData) {
        user.value = userData
      }
    }
  }

  function logout() {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
  }

  return {
    user,
    token,
    isAuthenticated,
    isLandlord,
    setUser,
    setToken,
    checkAuth,
    logout
  }
})
