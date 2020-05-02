import Vue from 'vue';
import App from './App.vue';
import store from './store'
store.commit('initialiseStore');
import router from './router';
import 'bootstrap';
import 'bootstrap-vue';
import '@fortawesome/fontawesome-free/css/all.min.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-vue/dist/bootstrap-vue.css';
import './assets/style.css';
import {ToastPlugin, TabsPlugin} from 'bootstrap-vue';
import IntegerInput from './components/IntegerInput.vue';

Vue.config.productionTip = true;
Vue.use(ToastPlugin);
Vue.use(TabsPlugin);

Vue.component('i-input', IntegerInput); //Global registration

new Vue({
  // components:{ //not global
  //   NoCommaInput
  // },
  router,
  store,
  render: h => h(App)
}).$mount('#app')
