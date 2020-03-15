<template>
  <div class="home">
    <p>
      <button type="button" class="btn btn-primary" v-on:click="testToast()">Toast Test</button>
    </p>
    <img alt="Vue logo" src="../assets/logo.png">
    <HelloWorld msg="Welcome to Your Vue.js App"/>
  </div>
</template>

<script>
// @ is an alias to /src
import HelloWorld from '@/components/HelloWorld.vue'

export default {
  name: 'Home',
  components: {
    HelloWorld
  },
  methods:{
    testToast(){
      this.$bvToast.toast(`This is toast number 1`, {
          title: 'BootstrapVue Toast',
          autoHideDelay: this.$config.toastDelay,
          appendToast: true
        })
    }
  }
}
</script>
