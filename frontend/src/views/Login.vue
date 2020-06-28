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

          <button class="btn btn-primary" type="submit">{{$t('login')}}</button>
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
    async login(){
      try{
        await this.$store.dispatch('login', { user: this.user, password: this.password});
        this.$router.push('Courses');
      }
      catch{
        this.$bvToast.toast(this.$t('userPwdInvalid'), {
          title: this.$t('error'),
          appendToast: true,
          variant: 'danger'
        });
      }
    }
  }
}
</script>