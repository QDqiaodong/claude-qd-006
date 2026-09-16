import { createRouter, createWebHistory } from 'vue-router'
import Kitchens from '../views/Kitchens.vue'
import Batches from '../views/Batches.vue'
import Ingredients from '../views/Ingredients.vue'
import Slots from '../views/Slots.vue'
import Deliveries from '../views/Deliveries.vue'

const routes = [
  { path: '/', redirect: '/kitchens' },
  { path: '/kitchens', component: Kitchens, meta: { title: '操作间与设备' } },
  { path: '/batches', component: Batches, meta: { title: '菜品与备餐批次' } },
  { path: '/ingredients', component: Ingredients, meta: { title: '原料台账' } },
  { path: '/slots', component: Slots, meta: { title: '操作间排期' } },
  { path: '/deliveries', component: Deliveries, meta: { title: '出餐配送' } }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
