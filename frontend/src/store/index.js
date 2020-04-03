import Vue from 'vue';
import Vuex from 'vuex';
import api from '../components/backend-api';
const tokenName = 'Token';
const storage = window.localStorage;

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    toast_delay: 8000
  },
  mutations: {
    initialiseStore(state) {
      // if you want to store/receive the whole store:
      // this.replaceState(
      // 	Object.assign(state, JSON.parse(localStorage.getItem('store')))
      // );
    },
    login_success(state, payload){
      state.loggedIn = true;
      state.userInfo = state.getters.decodedToken;
      this.setToken(tokenName, payload.token);
    },
  },
  actions: {
    setToken(token){
      storage.setItem(tokenName, token);
    },
    deleteToken(){
      storage.removeItem(tokenName);
    },
    login({commit}, {user, password}) {
      return new Promise((resolve, reject) => {
        resolve(true);
          // api.login(user, password)
          //     .then(response => {
          //       commit('login_success',{
          //         token: response.data
          //       });
          //       resolve(response);
          //     })
          //     .catch(error => {
          //         reject(error);
          //     });
      });
    }
  },
  modules: {
  },
  getters: {
    isLoggedIn: state => state.loggedIn,
    isTeacher: state => state.userInfo.teacher,
    getUserName: state => state.userInfo.username,
    token: state => {
      return storage.getItem(tokenName);
    },
    decodedToken: state =>{
        let decoded_payload;
        let payload;
        const token = state.getters.token;
    
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
})
