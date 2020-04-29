<template>
  <div id="app">
    <nav class="navbar navbar-expand-lg navbar-light bg-light">
      <div class="navbar-collapse order-1">
        <router-link class="navbar-brand" to="/">Home</router-link>
        <router-link class="nav-link" to="/Courses" v-if="$store.getters.isLoggedIn">Kurse</router-link>
        <router-link to="/Account" class="nav-link" v-if="$store.getters.isLoggedIn">Konto</router-link>
        <router-link to="/Admin" class="nav-link" v-if="$store.getters.userInfo.isAdmin">Admin</router-link>
        <a href="javascript:void(0)" class="nav-link" @click="logout" v-if="$store.getters.isLoggedIn">Abmelden</a>
        <router-link to="/Login" class="nav-link" v-else >Anmelden</router-link>
      </div>
      <div class="navbar-collapse order-2">
        <ul class="navbar-nav ml-auto">
          <span v-if="$store.getters.isLoggedIn">Angemeldet als
            <router-link to="/Account" > {{$store.getters.userInfo.forename}} {{$store.getters.userInfo.surname}} </router-link>
          </span>
        </ul>
      </div>
    </nav>
    <router-view/>
  </div>
</template>
<script>
export default {
  methods:{
    logout(){
      this.$store.dispatch('logout');
      if(this.$route.path !== '/'){
        this.$router.push('/');
      }
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
