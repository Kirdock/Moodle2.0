/*
The store has two major tasks:
1. centralized store for all components
2. it serves as a middleware between function-call in component and api-call. The data is modified here if response needs to be modified

api should not be called directly from components, it should always be called through this store
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
  },
  mutations: {
    initialiseStore(state) {
      // if you want to store/receive the whole store:
      // this.replaceState(
      // 	Object.assign(state, JSON.parse(localStorage.getItem('store')))
      // );
      const token = getToken();
      state.userInfo = decodeToken(token);
      api.setToken(token);
    },
    loginSuccess(state, payload){
      state.loggedIn = true;
      state.userInfo = decodeToken(payload.token);
      setToken(payload.token);
    },
    logout(state){
      state.userInfo = undefined;
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
                commit('loginSuccess',{
                  token: response.data
                });
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
    fetchSemesters({commit}){
      return api.fetchSemesters();
    },
    fetchCourses({commit}){
      return api.fetchCourses();
    }
  },
  modules: {
  },
  getters: {
    userInfo: state => state.userInfo,
    toastDelay: state => state.toastDelay,
    isLoggedIn: state => !!state.userInfo,
    decodedToken: state => state.userInfo
  }
});


function getToken(){
  return storage.getItem(tokenName);
}

function setToken(token){
  api.setToken(token);
  storage.setItem(tokenName, token);
}

function deleteToken(){
  storage.removeItem(tokenName);
  api.setToken();
}

function decodeToken(token){
  let decoded_payload;
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