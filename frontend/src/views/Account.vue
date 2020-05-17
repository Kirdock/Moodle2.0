<template>
  <div class="account">
    <b-tabs content-class="mt-3">
      <b-tab :title="$t('settings')" active>
        <div class="form-horizontal col-md-4">
          <form ref="form" @submit.prevent="updatePassword()">
            <div class="form-group">
              <label for="oldPassword" class="control-label required">{{ $t('passwordOld') }}</label> 
              <input id="oldPassword" type="password" class="form-control" v-model="passwordData.oldPassword" required>
            </div>
            <div class="form-group">
              <label for="newPassword" class="control-label required">{{ $t('passwordNew') }}</label> 
              <input id="newPassword" type="password" class="form-control" v-model="passwordData.newPassword" @change="resetValidationMessage()" required>
            </div>
            <div class="form-group">
              <label for="newPasswordConfirm" class="control-label required">{{ $t('passwordNewConfirm') }}</label> 
              <input id="newPasswordConfirm" ref="newPasswordConfirm" type="password" class="form-control" @change="resetValidationMessage()" v-model="passwordData.newPasswordConfirm" required>
            </div>
            <div class="form-inline">
              <b-button variant="primary" type="submit">
                {{$t('update')}}
              </b-button>
              <div class="offset-md-1 form-inline" v-if="loadingPasswordChange">
                <span class="fa fa-sync fa-spin"></span>
                <label class="control-label">{{ $t('loading') }}...</label>
              </div>
            </div>
          </form>
        </div>
      </b-tab>
    </b-tabs>
  </div>
</template>

<script>

export default {
  data() {
    return {
      passwordData: {},
      loadingPasswordChange: false
    }
  },
  created(){
  },
  methods:{
    resetValidationMessage(){
      this.$refs.newPasswordConfirm.setCustomValidity('');
    },
    updatePassword(){
      if(this.passwordData.newPassword !== this.passwordData.newPasswordConfirm){
        this.$refs.newPasswordConfirm.setCustomValidity(this.$t('passwordDontMatch'));
        this.$refs.form.reportValidity();
      }
      else{
        this.loadingPasswordChange = true;
        const {newPasswordConfirm, ...data} = this.passwordData;
        this.$store.dispatch('udpatePassword', data).then(()=>{
          this.$bvToast.toast(this.$t('passwordUpdated'), {
            title: this.$t('success'),
            variant: 'success',
            appendToast: true
          });
        }).catch(()=>{
          this.$bvToast.toast(this.$t('passwordUpdatedError'), {
            title: this.$t('error'),
            variant: 'danger',
            appendToast: true
          });
        }).finally(()=>{
          this.loadingPasswordChange = false;
        });
      }
    }
  }
}
</script>
