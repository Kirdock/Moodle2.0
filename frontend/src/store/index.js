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
import api from '../components/backend-api';
const tokenName = 'Token';
const storage = window.localStorage;
Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    toastDelay: 8000,
    userInfo: {},
    token: undefined,
    loggedIn: false
  },
  mutations: {
    initialiseStore(state) {
      // if you want to store/receive the whole store:
      // this.replaceState(
      // 	Object.assign(state, JSON.parse(localStorage.getItem('store')))
      // );
      state.token = getToken();
      state.userInfo = decodeToken(state.token);
      state.loggedIn = !!state.token;
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
      deleteToken();
    }
  },
  actions: {
    logout({commit}){
      deleteToken();
      commit('logout');
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
    createUser({commit}, userData){
      return api.createUser(userData);
    },
    createUsers({commit}, formData){
      return api.createUsers(formData);
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
    getSemesters({commit}){
      return api.getSemesters();
    },
    getCourses({commit}, semesterData){
      return api.getCourses(semesterData);
    },
    getCourse({commit}, data){
      return api.getCourse(data);
    },
    getUsers({commit}){
      return api.getUsers();
    },
    deleteCourse({commit}, data){
      return api.deleteCourse(data);
    }
  },
  modules: {
  },
  getters: {
    userInfo: state => state.userInfo,
    toastDelay: state => state.toastDelay,
    isLoggedIn: state => state.loggedIn,
    decodedToken: state => state.userInfo,
    token: state=> state.token
  }
});


function getToken(){
  return storage.getItem(tokenName);
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