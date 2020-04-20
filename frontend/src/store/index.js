import Vue from 'vue';
import Vuex from 'vuex';
import api from '../components/backend-api';
const tokenName = 'Token';
const storage = window.localStorage;

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    toast_delay: 8000,
    token: undefined
  },
  mutations: {
    initialiseStore(state) {
      // if you want to store/receive the whole store:
      // this.replaceState(
      // 	Object.assign(state, JSON.parse(localStorage.getItem('store')))
      // );
      state.token = getToken();
    },
    loginSuccess(state, payload){
      state.loggedIn = true;
      state.token = payload.token;
      state.userInfo = state.decodedToken;
      setToken(payload.token);
    },
    logout(state){
      state.token = false;
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
    }
  },
  modules: {
  },
  getters: {
    isLoggedIn: state => !!state.token,
    getToken: state => getToken(),
    isTeacher: state => state.userInfo.teacher,
    getUserName: state => state.userInfo.username,
    decodedToken: state =>{
        let decoded_payload;
        let payload;
        const token = state.token;
    
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