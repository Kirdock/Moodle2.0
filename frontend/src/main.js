import Vue from 'vue';
import App from './App.vue';
import store from './store'
import router from './router';
import 'bootstrap';
import 'bootstrap-vue';
import '@fortawesome/fontawesome-free/css/all.min.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-vue/dist/bootstrap-vue.css';
import './assets/style.css';
import {BVConfigPlugin, ToastPlugin, TabsPlugin, ModalPlugin, ButtonPlugin} from 'bootstrap-vue';
import IntegerInput from './components/IntegerInput.vue';

Vue.config.productionTip = true;
Vue.use(BVConfigPlugin, {
  BModal:{
    cancelTitle: 'Abbrechen'
  },
  BToast:{
    autoHideDelay: store.getters.toastDelay
  }
});
Vue.use(ToastPlugin);
Vue.use(TabsPlugin);
Vue.use(ModalPlugin);
Vue.use(ButtonPlugin);

Vue.component('i-input', IntegerInput); //Global registration

store.commit('initialiseStore');

new Vue({
  // components:{ //not global
  //   NoCommaInput
  // },
  router,
  store,
  render: h => h(App)
}).$mount('#app')
