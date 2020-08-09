import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '@/views/Home.vue'
import Courses from '@/views/Courses.vue';
import Course from '@/views/Course.vue';
import ExerciseSheet from '@/views/ExerciseSheet.vue';
import Login from '@/views/Login.vue';
import Account from '@/views/Account.vue';
import Admin from '@/views/Admin.vue';
import UserManagement from '@/views/Admin/UserManagement.vue';
import SemesterManagement from '@/views/Admin/SemesterManagement.vue';
import CourseManagement from '@/views/Admin/CourseManagement.vue';
import SheetManagement from '@/views/Admin/ExerciseSheet/SheetManagement.vue';
import store from '@/store/index';

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
    path: '/Course/:id',
    name: 'Course',
    component: Course,
    meta:{
      authentication: true
    },
    props: true
  },
  {
    path: '/Course/:courseId/ExerciseSheet/:exerciseSheetId',
    name: 'ExerciseSheet',
    component: ExerciseSheet,
    meta:{
      authentication: true
    },
    props: true
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
      isOwner: true
    }
  },
  {
    path: '/Admin/UserManagement',
    name: 'UserManagement',
    component: UserManagement,
    meta: {
      authentication: true,
      isAdmin: true
    }
  },
  {
    path: '/Admin/SemesterManagement',
    name: 'SemesterManagement',
    component: SemesterManagement,
    meta: {
      authentication: true,
      isAdmin: true
    }
  },
  {
    path: '/Admin/CourseManagement',
    name: 'CourseManagement',
    component: CourseManagement,
    meta: {
      authentication: true,
      isOwner: true
    }
  },
  {
    path: '/Admin/Course/:courseId/SheetManagement/:sheetId',
    name: 'SheetManagement',
    component: SheetManagement,
    meta: {
      authentication: true,
      isOwner: true
    },
    props: true
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
  store.dispatch('initialiseStore').finally(()=>{
    if (to.matched.some(record => record.meta.authentication)) {
      if(store.getters.isLoggedIn){
        if(to.matched.some(record => record.meta.isOwner)){
          if(store.getters.userInfo.isOwner){
            next();
          }
          else{
            next('/');
          }
        }
        else if(to.matched.some(record => record.meta.isAdmin)){
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
      next('/Courses');
    }
    else{
      next();
    }
  })
});

export default router
