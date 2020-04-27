import Vue from 'vue';
import App from './App.vue';
import router from './router';
import 'bootstrap';
import 'bootstrap-vue';
import '@fortawesome/fontawesome-free/css/all.min.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-vue/dist/bootstrap-vue.css';
import './assets/style.css';
import {ToastPlugin, TabsPlugin} from 'bootstrap-vue';
import store from './store'
store.commit('initialiseStore');

Vue.config.productionTip = true;
Vue.use(ToastPlugin);
Vue.use(TabsPlugin);

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
