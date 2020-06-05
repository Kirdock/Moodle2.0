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
    fetchToken(state){
      state.token = getToken();
    },
    initialiseStore(state, isOwner) {
      state.settings = getSettings();
      setUserInfo(state, isOwner);
    },
    setToken(state, token){
      state.token = token;
      setToken(token);
    },
    logout(state){
      state.token = undefined;
      state.userInfo = {};
      state.loggedIn = false;
    },
    updateSettings(state, settings){
      Object.assign(state.settings, settings);
      saveSettings(state.settings);
    },
    fileTypes(state, fileTypes){
      state.fileTypes = fileTypes;
    },
    fileTypePromise(state, promise){
      state.fileTypesPromise = promise;
    }
  },
  actions: {
    initialiseStore({commit}, fetchToken){
      if(fetchToken){
        commit('fetchToken');
      }
      return new Promise((resolve, reject) =>{
        this.dispatch('getIsOwner').then(isOwner =>{
          commit('initialiseStore', isOwner);
          resolve(isOwner);
        }).catch((error)=>{
          commit('initialiseStore', false);
          reject(error);
        });
      })
      
    },
    logout({commit}, isTokenExpired){
      deleteToken();
      commit('logout');
      if(isTokenExpired){
        if(router.currentRoute.name !== 'Login'){
          router.push('Login');
        }
        this.$app.$bvToast.toast(this.$app.$t('sessionExpired'), {
            title: this.$app.$t('warning'),
            variant: 'warning',
            appendToast: true
        });
      }
      else if(router.currentRoute.path !== '/'){
        router.push('/');
      }
    },
    login({commit}, {user, password}) {
      return new Promise((resolve, reject) => {
          api.login(user, password)
              .then(response => {
                commit('setToken',response.data.accessToken);
                resolve(this.dispatch('initialiseStore'));
              })
              .catch(reject);
      });
    },
    checkToken({state}){
      if(state.userInfo && state.userInfo.exp < new Date().getTime()/1000){
        this.dispatch('logout', true);
      }
    },
    getIsOwner({commit, state}){
      return new Promise((resolve, reject) =>{
        if(state.token){
          api.getIsOwner().then(response => {
            resolve(response.data);
          }).catch(reject);
        }
        else{
          resolve(false);
        }
      })
    },
    createUser({commit}, userData){
      return api.createUser(userData);
    },
    createUsers({commit}, formData){
      return api.createUsers(formData);
    },
    updateUser({commit}, userData){
      return api.updateUser(userData);
    },
    deleteUser({commit}, matriculationNumber){
      return api.deleteUser(matriculationNumber);
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
    updateCourseDefaultTemplate({commit}, templateData){
      return api.updateCourseDefaultTemplate(templateData);
    },
    copyCourse({commit}, data){
      return api.copyCourse(data);
    },
    updateCourseUsers({commit}, data){
      return api.updateCourseUsers(data);
    },
    assignCourseUsers({commit}, formData){
      return api.assignCourseUsers(formData);
    },
    getSemesters({commit}){
      return api.getSemesters();
    },
    getSemestersAssigned({commit}){
      return api.getSemestersAssigned();
    },
    getCourses({commit}, semesterData){
      return api.getCourses(semesterData);
    },
    getCoursesAssigned({commit}, semesterData){
      return api.getCoursesAssigned(semesterData);
    },
    getCourseAssigned({commit}, data){
      return api.getCourseAssigned(data);
    },
    getCourse({commit}, data){
      return api.getCourse(data);
    },
    getUser({commit}){
      return api.getUser();
    },
    getUsers({commit}, data = {}){
      return new Promise((resolve, reject) =>{
        api.getUsers(data).then(response =>{
          if(data.courseId){
            response.data.forEach(user =>{
              user.oldRole = user.courseRole;
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
    getFileTypes({commit, state}){
      return new Promise((resolve, reject) =>{
        if(state.fileTypes){
          resolve({data: state.fileTypes})
        }
        else if(state.fileTypesPromise){
          resolve(state.fileTypesPromise);
        }
        else{
          const promise = api.getFileTypes().then(response =>{
            commit('fileTypes', response.data);
            return response;
          });
          commit('fileTypePromise', promise);
          resolve(promise);
        }
      })
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
    getExerciseSheetAssigned({commit}, sheetId){
      return api.getExerciseSheetAssigned(sheetId);
    },
    updateExerciseSheet({commit}, sheetData){
      return api.updateExerciseSheet(sheetData);
    },
    deleteExerciseSheet({commit}, sheetId){
      return api.deleteExerciseSheet(sheetId);
    },
    createExample({commit}, exampleData){
      return api.createExample(exampleData);
    },
    updateExample({commit}, exampleData){
      return api.updateExample(exampleData);
    },
    deleteExample({commit}, exampleId){
      return api.deleteExample(exampleId);
    },
    updateExampleOrder({commit}, changedOrders){
      return api.updateExampleOrder(changedOrders);
    },
    getAttendanceList({commit}, courseId){
      return api.getAttendanceList(courseId);
    },
    saveKreuzel({commit}, examples){
      return api.saveKreuzel(examples);
    },
    addExampleAttachement({commit}, formData){
      return api.addExampleAttachement(formData);
    },
    getExampleAttachement({commit}, id){
      return api.getExampleAttachement(id);
    },
    getUserKreuzel({commit}, kreuzelData){
      return api.getUserKreuzel(kreuzelData);
    },
    updatePresented({commit}, data){
      return api.updatePresented(data);
    }
  },
  modules: {
  },
  getters: {
    userInfo: state => state.userInfo,
    locale: state => state.settings.locale,
    isLoggedIn: state => state.loggedIn,
    decodedToken: state => state.userInfo,
    token: state=> state.token
  }
});

function setUserInfo(state, isOwner){
  const userInfo = decodeToken(state.token);
  userInfo.isOwner = userInfo.isAdmin ? true : isOwner;
  state.userInfo = userInfo;
  
  state.loggedIn = !!state.token;
}

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