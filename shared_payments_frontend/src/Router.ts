import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    alias: '/groups',
    name: 'group-list',
    component: () => import('./components/GroupList.vue')
  },
  {
    path: '/groups/:id',
    name: 'group-details',
    component: () => import('./components/GroupDetail.vue')
  },
  {
    path: '/groups/:id/addfriend',
    name: 'add-friend',
    component: () => import('./components/AddFriend.vue')
  },
  {
    path: '/groups/:id/addpayment',
    name: 'add-payment',
    component: () => import('./components/AddPayment.vue')
  },
  {
    path: '/add-group',
    name: 'add-group',
    component: () => import('./components/AddGroup.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
