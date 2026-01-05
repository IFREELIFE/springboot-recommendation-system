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
    // 同时将用户信息保存到 localStorage 以便刷新后恢复
    if (userData) {
      localStorage.setItem('user', JSON.stringify(userData))
    } else {
      localStorage.removeItem('user')
    }
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
    const savedUser = localStorage.getItem('user')
    
    if (savedToken) {
      token.value = savedToken
      
      // 优先使用保存的用户信息
      if (savedUser) {
        try {
          user.value = JSON.parse(savedUser)
        } catch (error) {
          console.error('Failed to parse saved user data:', error)
          // 如果解析失败，尝试从 token 中获取
          const userData = authService.getCurrentUser()
          if (userData) {
            user.value = userData
          }
        }
      } else {
        // 如果没有保存的用户信息，从 token 中获取
        const userData = authService.getCurrentUser()
        if (userData) {
          user.value = userData
        }
      }
    }
  }

  function logout() {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
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
