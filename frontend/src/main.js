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
import i18n from '@/plugins/i18n';
import {BVConfigPlugin, ToastPlugin, TabsPlugin, ModalPlugin, ButtonPlugin} from 'bootstrap-vue';
import IntegerInput from './components/IntegerInput.vue';

Vue.config.productionTip = true;
Vue.use(BVConfigPlugin, {
  BModal:{
    cancelTitle: 'Abbrechen'
  },
  BToast:{
    autoHideDelay: 8000
  }
});
Vue.use(ToastPlugin);
Vue.use(TabsPlugin);
Vue.use(ModalPlugin);
Vue.use(ButtonPlugin);

Vue.component('i-input', IntegerInput); //Global registration

store.commit('initialiseStore');
i18n.locale = store.getters.locale;

const app = new Vue({
  // components:{ //not global
  //   NoCommaInput
  // },
  i18n,
  router,
  store,
  render: h => h(App)
}).$mount('#app');

store.$app = app; //need it for logout-toast
store.dispatch('checkToken');
