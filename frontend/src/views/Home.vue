<template>
  <div class="home">
    <div class="form-horizontal col-md-6">
      <div class="form-group">
        <button type="button" class="btn btn-primary" v-on:click="testToast()">Toast Test</button>
      </div>
    </div>
  </div>
</template>

<script>
// @ is an alias to /src

export default {
  data() {
    return {
    }
  },
  methods:{
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
