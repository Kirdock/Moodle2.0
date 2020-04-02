import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import Courses from '../views/Courses.vue';

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/courses',
    name: 'Kurse',
    component: Courses,
    meta:{
      authentication: true
    }
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.authentication && !router.app.$auth.isLoggedIn()) {
    next('/');
  }
  else{
    next();
  }
});

export default router
