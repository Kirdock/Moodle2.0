<template>
  <div class="home">
    <div class="form-horizontal col-md-6">
      <div class="form-group">
        <button type="button" class="btn btn-primary" v-on:click="testToast()">Toast Test</button>
      </div>
    </div>
    <div class="col-md-6">
        <form @submit.prevent="login()" class="form-horizontal">
          <div class="form-group">
            <div class="col-md-2">
              <label for="user" class="control-label">Benutzername:</label>
            </div>
            <div class="col-md-10">
              <input id="user" type="text" class="form-control" v-model="user">
            </div>
          </div>

          <div class="form-group">
            <label for="password" class="control-label col-md-2">Password:</label>
            <div class="col-md-10">
              <input id="password" class="form-control" type="password" v-model="password">
            </div>
          </div>

          <button type="submit" class="btn btn-primary">Login</button>
        </form>
    </div>
  </div>
</template>

<script>
// @ is an alias to /src

export default {
  data() {
    return {
      user: undefined,
      password: undefined
    }
  },
  methods:{
    login(){
      this.$store.dispatch("login", { user: this.user, password: this.password})
        .then(() => {
          this.$router.push('/Courses');
        })
        .catch(error => {
          this.$bvToast.toast(`Benutzername oder Password stimmt nicht überein`, {
            title: 'Fehler',
            autoHideDelay: this.$store.toastDelay,
            appendToast: true,
            variant: 'danger'
          });
        })
    },
    testToast(){
      this.$bvToast.toast(`This is toast number 1`, {
          title: 'BootstrapVue Toast',
          autoHideDelay: this.$store.toastDelay,
          appendToast: true,
          variant: 'success'
        })
    }
  }
}
</script>
