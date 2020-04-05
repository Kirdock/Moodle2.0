import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import Courses from '../views/Courses.vue';
import Login from '../views/Login.vue';
import store from '../store/index';

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/Login',
    name: 'Login',
    component: Login
  },
  {
    path: '/Courses',
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
  if (to.matched.some(record => record.meta.authentication)) {
    if(store.getters.isLoggedIn){
      next();
    }
    else{
      next('/Login');
    }
  }
  else if(to.path === '/Login' && store.getters.isLoggedIn){
    next('/');
  }
  else{
    next();
  }
});

export default router
