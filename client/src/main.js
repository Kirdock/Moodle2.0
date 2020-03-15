import Vue from 'vue';
import App from './App.vue';
import router from './router';
import axios from 'axios';
import 'bootstrap';
import '@fortawesome/fontawesome-free/css/all.min.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-vue';
import auth from './services/authentication';
import config from './services/config';
import {ToastPlugin} from 'bootstrap-vue';

Vue.config.productionTip = true;
Vue.prototype.$config = config;
Vue.prototype.$auth = auth;
Vue.use(ToastPlugin);

axios.interceptors.request.use(function (config) {
  const token = auth.getToken();
  if(token){
      config.headers.Authorization = 'Bearer ' + token;
  }
  return config;
});

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
