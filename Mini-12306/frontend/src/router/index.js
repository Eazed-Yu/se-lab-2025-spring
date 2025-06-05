import { createRouter, createWebHistory } from 'vue-router';
import TicketSearch from '../views/TicketSearch.vue';
import UserTickets from '../views/UserTickets.vue';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';
import PassengerManagement from '../views/PassengerManagement.vue';
import ChatView from '../views/ChatView.vue';

const routes = [
  {
    path: '/',
    redirect: '/tickets/search'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { requiresAuth: false }
  },
  {
    path: '/tickets/search',
    name: 'TicketSearch',
    component: TicketSearch,
    meta: { requiresAuth: true }
  },
  {
    path: '/tickets/user',
    name: 'UserTickets',
    component: UserTickets,
    meta: { requiresAuth: true }
  },
  {
    path: '/passengers',
    name: 'PassengerManagement',
    component: PassengerManagement,
    meta: { requiresAuth: true }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: ChatView,
    meta: { requiresAuth: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫，检查用户是否已登录
router.beforeEach((to, from, next) => {
  // 如果路由需要认证
  if (to.meta.requiresAuth) {
    // 检查本地存储中是否有用户信息
    const userStr = localStorage.getItem('user');
    if (!userStr) {
      // 如果没有用户信息，重定向到登录页面
      next({ name: 'Login' });
    } else {
      // 如果有用户信息，继续导航
      next();
    }
  } else {
    // 如果路由不需要认证，直接导航
    next();
  }
});

export default router;
