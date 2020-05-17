/*
The store has two major tasks:
1. centralized store for all components
2. it serves as a middleware between function-call in component and api-call. The data is modified here if response needs to be modified

api should not be called directly from components, it should always be called through this store
*/

/*
ATTENTION!!
new properties for state have to be in state initialization or a explizit call to Vue, else update will not be recognized
 */

import Vue from 'vue';
import Vuex from 'vuex';
import api from '@/components/backend-api';
import router from '@/router';
import i18n from '@/plugins/i18n';

const tokenName = 'Token';
const settingName = 'Settings';
const storage = window.localStorage;
Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    userInfo: {},
    token: undefined,
    loggedIn: false
  },
  mutations: {
    initialiseStore(state) {
      state.token = getToken();
      state.userInfo = decodeToken(state.token);
      state.loggedIn = !!state.token;
      state.settings = getSettings();
    },
    loginSuccess(state, payload){
      state.token = payload.accessToken;
      state.userInfo = decodeToken(payload.accessToken);
      state.loggedIn = true;
      setToken(payload.accessToken);
    },
    logout(state){
      state.token = undefined;
      state.userInfo = {};
      state.loggedIn = false;
    },
    updateSettings(state, settings){
      Object.assign(state.settings, settings);
      saveSettings(state.settings);
    }
  },
  actions: {
    logout({commit}, isTokenExpired){
      deleteToken();
      commit('logout');
      if(isTokenExpired){
        router.push('Login');
        this.$app.$bvToast.toast(this.$app.$t('sessionExpired'), {
            title: this.$app.$t('warning'),
            variant: 'warning',
            appendToast: true
        });
      }
      else if(router.path !== '/'){
        router.push('/');
      }
    },
    login({commit}, {user, password}) {
      return new Promise((resolve, reject) => {
          api.login(user, password)
              .then(response => {
                commit('loginSuccess',response.data);
                resolve(response);
              })
              .catch(error => {
                reject(error);
              });
      });
    },
    checkToken({state}){
      if(state.userInfo && state.userInfo.exp < new Date().getTime()/1000){
        this.dispatch('logout', true);
      }
    },
    updateSettings({commit}, data){
      return commit('updateSettings', data);
    },
    createUser({commit}, userData){
      return api.createUser(userData);
    },
    createUsers({commit}, formData){
      return api.createUsers(formData);
    },
    deleteUser({commit}, matrikelNummer){
      return api.deleteUser(matrikelNummer);
    },
    createSemester({commit}, semesterData){
      return api.createSemester(semesterData);
    },
    createCourse({commit}, courseData){
      return api.createCourse(courseData);
    },
    updateCourse({commit}, courseData){
      return api.updateCourse(courseData)
    },
    copyCourse({commit}, data){
      return api.copyCourse(data);
    },
    updateCourseUsers({commit}, data){
      return api.updateCourseUsers(data);
    },
    getSemesters({commit}){
      return api.getSemesters();
    },
    getCourses({commit}, semesterData){
      return api.getCourses(semesterData);
    },
    getCourse({commit}, data){
      return api.getCourse(data);
    },
    getUsers({commit}, data){
      return new Promise((resolve, reject) =>{
        api.getUsers(data).then(response =>{
          if(data.courseId){
            response.data.forEach(user =>{
              user.oldRole = user.role;
            });
          }
          resolve({data: response.data});
        }).catch(reject);
        
      })
    },
    udpatePassword({commit}, data){
      return api.udpatePassword(data);
    },
    deleteCourse({commit}, data){
      return api.deleteCourse(data);
    },
    createExerciseSheet({commit}, data){
      return api.createExerciseSheet(data);
    },
    getExerciseSheets({commit}, courseData){
      return api.getExerciseSheets(courseData);
    },
    getExerciseSheet({commit}, sheedId){
      return api.getExerciseSheet(sheedId);
    },
    updateExerciseSheet({commit}, sheetData){
      return api.updateExerciseSheet(sheetData);
    },
    deleteExerciseSheet({commit}, sheetId){
      return api.deleteExerciseSheet(sheetId);
    }
  },
  modules: {
  },
  getters: {
    userInfo: state => state.userInfo,
    locale: state => state.settings.locale,
    isLoggedIn: state => state.loggedIn,
    decodedToken: state => state.userInfo,
    token: state=> state.token,
    currentDateTime: ()=>{
      const date = new Date();
      return new Date(date.getTime() - (date.getTimezoneOffset() * 60000)).toISOString().split(/ *:..\..../)[0];
    },
    courseRoles: () => {
      return [
        {
            key: 'l',
            value: i18n.t('lecturer'),
        },
        {
            key: 't',
            value: i18n.t('tutor'),
        },
        {
            key: 's',
            value: i18n.t('student'),
        },
      ];
    },
    userRoles: () => {
      return [
        {
            key: 'l',
            value: i18n.t('lecturer'),
        },
        {
            key: 's',
            value: i18n.t('student'),
        },
      ];
    }
  }
});


function getToken(){
  return storage.getItem(tokenName);
}

function getSettings(){
  let settings = storage.getItem(settingName);
  return settings ? JSON.parse(settings) : {};
}

function saveSettings(settings){
  storage.setItem(settingName, JSON.stringify(settings));
}

function setToken(token){
  storage.setItem(tokenName, token);
}

function deleteToken(){
  storage.removeItem(tokenName);
}

function decodeToken(token){
  let decoded_payload = {};
  let payload;

  if (token) {
      payload = token.split(".")[1];
      switch (payload.length % 4) {
          case 0:
              break;
          case 1:
              payload += "===";
              break;
          case 2:
              payload += "==";
              break;
          case 3:
              payload += "=";
              break;
      }
      decoded_payload = JSON.parse(atob(payload));
  }
  return decoded_payload;
}