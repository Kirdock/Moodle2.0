import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import Courses from '../views/Courses.vue';
import Login from '../views/Login.vue';
import Account from '../views/Account.vue';
import Admin from '../views/Admin.vue';
import store from '../store/index';

Vue.use(VueRouter);

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
    name: 'Courses',
    component: Courses,
    meta:{
      authentication: true
    }
  },
  {
    path: '/Account',
    name: 'Account',
    component: Account,
    meta: {
      authentication: true
    }
  },
  {
    path: '/Admin',
    name: 'Admin',
    component: Admin,
    meta: {
      authentication: true,
      isAdmin: true
    }
  },
  //otherwise
  { path: '*', redirect: '/' }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.authentication)) {
    if(store.getters.isLoggedIn){
      if(to.matched.some(record => record.meta.isAdmin)){
        if(store.getters.userInfo.isAdmin){
          next();
        }
        else{
          next('/');
        }
      }
      else{
        next();
      }
    }
    else{
      next('/Login');
    }
  }
  else if(to.matched.some(record => record.name === 'Login') && store.getters.isLoggedIn){
    next('/');
  }
  else{
    next();
  }
});

export default router
