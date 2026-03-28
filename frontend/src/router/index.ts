import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Plaza',
    component: () => import('@/views/Plaza.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/post/:id',
    name: 'PostDetail',
    component: () => import('@/views/PostDetail.vue')
  },
  {
    path: '/profile/:id?',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/feed',
    name: 'Feed',
    component: () => import('@/views/Feed.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('@/views/Notifications.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/settings/llm',
    name: 'LlmApiKeys',
    component: () => import('@/views/admin/AdminApiKeys.vue'),
    meta: { requiresAuth: true }
  },
  // 管理员独立登录路由
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/AdminLogin.vue')
  },
  // 管理员后台路由
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/AdminDashboard.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/AdminUsers.vue')
      },
      {
        path: 'posts',
        name: 'AdminPosts',
        component: () => import('@/views/admin/AdminPosts.vue')
      },
      {
        path: 'reports',
        name: 'AdminReports',
        component: () => import('@/views/admin/AdminReports.vue')
      },
      {
        path: 'logs',
        name: 'AdminLogs',
        component: () => import('@/views/admin/AdminLogs.vue')
      },
      {
        path: 'apikeys',
        name: 'AdminApiKeys',
        component: () => import('@/views/admin/AdminApiKeys.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach(async (to) => {
  const userStore = useUserStore()
  userStore.initUserInfo()

  // 🔥 管理员登录页面 - 公开访问，无需校验
  if (to.path === '/admin/login') {
    // 如果已登录且是管理员，直接跳转到后台
    if (userStore.isLoggedIn && userStore.isAdmin) {
      return '/admin'
    }
    return true
  }

  // 🔥 管理员后台路由 - 严格校验
  if (to.path.startsWith('/admin')) {
    // 未登录 -> 跳转到管理员登录页
    if (!userStore.isLoggedIn) {
      ElMessage.error('请先登录管理员账号')
      return '/admin/login'
    }

    // 已登录但不是管理员 -> 拒绝访问
    if (!userStore.isAdmin) {
      ElMessage.error('仅管理员可访问后台')
      return '/admin/login'
    }

    // ✅ 管理员已登录，直接放行，不再调用 getUserInfo
    return true
  }

  // 普通用户登录页面
  if (to.path === '/login') {
    if (userStore.isLoggedIn) {
      // 如果是管理员误入普通登录页，引导到管理员后台
      return userStore.isAdmin ? '/admin' : '/'
    }
    return true
  }

  // 普通用户路由 - 需要登录的页面
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    ElMessage.error('请先登录')
    return '/login'
  }

  // 已登录但没有用户信息，尝试获取
  if (userStore.isLoggedIn && !userStore.userInfo) {
    try {
      await userStore.getUserInfo()
    } catch (error) {
      console.error('Failed to get user info:', error)
      userStore.logout()
      if (to.meta.requiresAuth) {
        return '/login'
      }
    }
  }

  return true
})

export default router
