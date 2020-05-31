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
import {BVConfigPlugin, ToastPlugin, TabsPlugin, ModalPlugin, BNavItem} from 'bootstrap-vue';
import IntegerInput from '@/components/IntegerInput.vue';
import CKEditor from '@ckeditor/ckeditor5-vue';

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
Vue.use(ModalPlugin);
Vue.use(TabsPlugin);
Vue.use( CKEditor );

Vue.component('b-nav-item', BNavItem);
Vue.component('i-input', IntegerInput); //Global registration

store.dispatch('initialiseStore', true).finally(()=>{
  i18n.locale = store.getters.locale;

  const app = new Vue({
    i18n,
    router,
    store,
    render: h => h(App)
  }).$mount('#app');

  store.$app = app; //need it for logout-toast
  store.dispatch('checkToken');
});