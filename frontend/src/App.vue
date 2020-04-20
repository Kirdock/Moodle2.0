<template>
  <div id="app">
    <nav class="navbar navbar-expand-lg navbar-light bg-light">
      <router-link class="navbar-brand" to="/">Home</router-link>
      <router-link to="/Courses">Kurse</router-link>
      <a href="javascript:void(0)" class="nav-link" @click="logout" v-if="$store.getters.isLoggedIn">Abmelden</a>
      <router-link to="/Login" class="nav-link" v-else >Anmelden</router-link>
    </nav>
    <router-view/>
  </div>
</template>
<script>
export default {
  methods:{
    logout(){
      this.$store.dispatch('logout');
      this.$router.push('/');
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
