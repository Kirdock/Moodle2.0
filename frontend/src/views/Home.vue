<template>
  <div class="home">
    <div class="form-horizontal col-md-6">
      <div class="form-group">
        <h1>Willkommen!</h1>
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
          appendToast: true,
          variant: 'success'
        })
    }
  }
}
</script>
