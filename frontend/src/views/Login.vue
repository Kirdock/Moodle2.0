<template>
    <div class="col-md-6">
        <form @submit.prevent="login()" class="form-horizontal">
          <div class="form-group">
            <div class="col-md-2">
              <label for="user" class="control-label">{{$t('username')}}:</label>
            </div>
            <div class="col-md-10">
              <input id="user" type="text" class="form-control" v-model="user">
            </div>
          </div>

          <div class="form-group">
            <label for="password" class="control-label col-md-2">{{$t('password')}}:</label>
            <div class="col-md-10">
              <input id="password" class="form-control" type="password" v-model="password">
            </div>
          </div>

          <b-button type="submit" variant="primary">{{$t('login')}}</b-button>
        </form>
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
      this.$store.dispatch('login', { user: this.user, password: this.password})
        .then(() => {
          this.$router.push('Courses');
        })
        .catch(error => {
          this.$bvToast.toast(this.$t('userPwdInvalid'), {
            title: this.$t('error'),
            appendToast: true,
            variant: 'danger'
          });
        })
    }
  }
}
</script>