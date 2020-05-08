<template>
  <div id="app">
    <nav class="navbar navbar-expand-lg navbar-light bg-light">
      <div class="navbar-collapse order-1">
        <router-link class="navbar-brand" to="/">Home</router-link>
        <router-link class="nav-link" to="/Courses" v-if="$store.getters.isLoggedIn">Kurse</router-link>
        <router-link to="/Account" class="nav-link" v-if="$store.getters.isLoggedIn">Konto</router-link>
        <router-link to="/Admin" class="nav-link" v-if="$store.getters.userInfo.isAdmin">Admin</router-link>
        <b-button variant="link" class="nav-link" @click="logout" v-if="$store.getters.isLoggedIn">Abmelden</b-button>
        <router-link to="/Login" class="nav-link" v-else >Anmelden</router-link>
      </div>
      <div class="navbar-collapse order-2">
        <ul class="navbar-nav ml-auto">
          <b-button variant="link" class="nav-link" @click="changeLocale('en')" v-if="locale !== 'en'">Englisch</b-button>
          <b-button variant="link" class="nav-link" @click="changeLocale('de')" v-if="locale !== 'de'">German</b-button>
          <span v-if="$store.getters.isLoggedIn" style="margin-top: 8px">Angemeldet als
            <router-link to="/Account" > {{$store.getters.userInfo.forename}} {{$store.getters.userInfo.surname}} </router-link>
          </span>
        </ul>
      </div>
    </nav>
    <router-view/>
  </div>
</template>
<script>
import i18n from '@/plugins/i18n';
export default {
  computed:{
    locale(){
      return i18n.locale;
    }
  },
  methods:{
    changeLocale(locale){
      this.$store.dispatch('updateSettings', {locale}).then(()=>{
        i18n.locale = locale;
      }).catch(()=>{
        this.$bvToast.toast(this.$t('changeLanguageError'), {
          title: this.$t('toastErrorTitle'),
          appendToast: true,
          variant: 'error'
        })
      });
    },
    logout(){
      this.$store.dispatch('logout').then(()=>{
        if(this.$route.path !== '/'){
          this.$router.push('/');
        }
      });
    }
  }
}
</script>
<style>

#nav {
  padding: 30px;
}

#nav a {
  font-weight: bold;
  color: #2c3e50;
}

#nav a.router-link-exact-active {
  color: #42b983;
}
</style>
