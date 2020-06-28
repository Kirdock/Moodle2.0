<template>
  <div class="account">
    <b-tabs content-class="mt-3">
      <b-tab :title="$t('information')" active>
        <div class="form-horizontal col-md-6">
          <form @submit.prevent="updateUser()">
            <user-info :value="userInfo" :isEdit="true"></user-info>
            <button class="btn btn-primary" type="submit">
              <span class="fa fa-sync fa-spin" v-if="loading_updateInformation"></span>
              <span class="fa fa-save" v-else></span>
              {{$t('save')}}
            </button>
          </form>
        </div>
      </b-tab>
      <b-tab :title="$t('security')">
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
              <button class="btn btn-primary" type="submit">
                <span class="fa fa-sync fa-spin" v-if="loadingPasswordChange"></span>
                <span class="fa fa-save" v-else></span>
                {{$t('save')}}
              </button>
            </div>
          </form>
        </div>
      </b-tab>
    </b-tabs>
  </div>
</template>

<script>
import UserInfo from '@/components/UserInfo.vue';
export default {
    components:{
        'user-info': UserInfo
    },
  data() {
    return {
      userInfo: {},
      passwordData: {},
      loadingPasswordChange: false,
      loading_updateInformation: false
    }
  },
  created(){
    this.getUser();
  },
  methods:{
    async getUser(){
      try{
        const response = await this.$store.dispatch('getUser');
        this.userInfo = response.data;
      }
      catch{
        this.$bvToast.toast(this.$t('user.error.get'), {
          title: this.$t('error'),
          variant: 'danger',
          appendToast: true
        });
      };
    },
    async updateUser(){
      this.loading_updateInformation = true;
      try{
        await this.$store.dispatch('updateUser', this.userInfo);
        this.$bvToast.toast(this.$t('user.saved'), {
          title: this.$t('success'),
          variant: 'success',
          appendToast: true
        });
      }
      catch{
        this.$bvToast.toast(this.$t('user.error.save'), {
          title: this.$t('error'),
          variant: 'danger',
          appendToast: true
        });
      }
      finally{
          this.loading_updateInformation = false;
      }
    },
    resetValidationMessage(){
      this.$refs.newPasswordConfirm.setCustomValidity('');
    },
    async updatePassword(){
      if(this.passwordData.newPassword !== this.passwordData.newPasswordConfirm){
        this.$refs.newPasswordConfirm.setCustomValidity(this.$t('passwordDontMatch'));
        this.$refs.form.reportValidity();
      }
      else{
        this.loadingPasswordChange = true;
        const {newPasswordConfirm, ...data} = this.passwordData;
        try{
          await this.$store.dispatch('udpatePassword', data);
          this.$bvToast.toast(this.$t('passwordUpdated'), {
            title: this.$t('success'),
            variant: 'success',
            appendToast: true
          });
        }
        catch{
          this.$bvToast.toast(this.$t('passwordUpdatedError'), {
            title: this.$t('error'),
            variant: 'danger',
            appendToast: true
          });
        }
        finally{
          this.loadingPasswordChange = false;
        }
      }
    }
  }
}
</script>
