<template>
  <div id="app">
    <nav class="navbar navbar-expand-lg navbar-light bg-light">
      <div class="navbar-collapse order-1">
        <router-link class="navbar-brand" to="/">{{ $t('home') }}</router-link>
        <router-link class="nav-link" :to="{name:'Courses'}" v-if="$store.getters.isLoggedIn">{{ $t('courses.name') }}</router-link>
        <router-link :to="{name:'Account'}" class="nav-link" v-if="$store.getters.isLoggedIn">{{ $t('account') }}</router-link>
        <router-link :to="{name:'Admin'}" class="nav-link" v-if="$store.getters.userInfo.isOwner">{{ $t('admin') }}</router-link>
        <a href="#" class="nav-link" @click.prevent="logout" v-if="$store.getters.isLoggedIn">{{ $t('logout') }}</a>
        <router-link :to="{name:'Login'}" class="nav-link" v-else >{{ $t('login') }}</router-link>
      </div>
      <div class="navbar-collapse order-2">
        <ul class="navbar-nav ml-auto">
          <a href="#" class="nav-link" @click.prevent="changeLocale('en')" v-if="locale !== 'en'">English</a>
          <a href="#" class="nav-link" @click.prevent="changeLocale('de')" v-if="locale !== 'de'">Deutsch</a>
          <span v-if="$store.getters.isLoggedIn" style="margin-top: 8px">{{ $t('loggedInAs') }}
            <router-link :to="{name:'Account'}" > {{$store.getters.userInfo.forename}} {{$store.getters.userInfo.surname}} </router-link>
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
      this.$store.commit('updateSettings', {locale});
      i18n.locale = locale;
    },
    logout(){
      this.$store.dispatch('logout');
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
