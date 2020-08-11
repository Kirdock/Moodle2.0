<template>
    <div class="UserManagemgnt">
        <h1 id="users">{{$t('user.management')}}</h1>
        <ol class="breadcrumb">
            <li class="breadcrumb-item">
                <router-link :to="{name:'Admin'}" >{{ $t('admin') }}</router-link>
            </li>
            <li class="breadcrumb-item active">{{$t('user.management')}}</li>
        </ol>
        <label class="control-label requiredField" style="margin-left: 10px">{{ $t('requiredField') }}</label>
        <div class="form-horizontal col-md-12">
            <div class="form-inline">
                <button class="btn btn-primary" v-b-modal="'modal-new-user'" style="margin-right: 10px">
                    <span class="fa fa-sync fa-spin" v-if="loading.createUser"></span>
                    <span class="fa fa-plus" v-else></span>
                    {{$t('new')}}
                </button>
                <b-modal id="modal-new-user" :title="$t('user.title.new')" :ok-title="$t('confirm')" :cancel-title="$t('cancel')" @ok="createUser" @hidden="resetUserInfo">
                    <form ref="formCreate" @submit.prevent="createUser()">
                        <user-info v-model="userInfo" :isEdit="false"></user-info>
                    </form>
                </b-modal>
                <label class="btn btn-primary" style="margin-right: 10px">
                    <span class="fa fa-sync fa-spin" v-if="loading.fileUpload"></span>
                    <span class="fas fa-upload" v-else></span>
                    {{ $t('uploadCSV') }}
                    <input type="file" class="d-none" id="file" ref="file" accept=".csv" @change="submitUsers()"/>
                </label>
                <div class="form-check">
                    <input id="isAdmin" type="checkbox" class="form-check-input" v-model="isAdmin">
                    <label for="isAdmin"   class="form-check-label">
                        {{$t('admin')}}
                    </label>
                </div>
            </div>
            <div class="form-group">
                <label for="searchText" class="control-label">
                    <span class="fas fa-search"></span>
                    {{ $t('search') }}
                </label>
                <input id="searchText" type="text" class="form-control" v-model="searchText">
            </div>
            <table class="table table-hover" aria-describedby="users">
                <thead>
                    <th scope="col">{{$t('matriculationNumber')}}</th>
                    <th scope="col">{{$t('surname')}}</th>
                    <th scope="col">{{$t('forename')}}</th>
                    <th scope="col">{{$t('admin')}}</th>
                    <th scope="col">{{$t('actions')}}</th>
                </thead>
                <tbody>
                    <tr v-for="user in filteredUsers" :key="user.matriculationNumber">
                        <td>
                            {{user.matriculationNumber}}
                        </td>
                        <td>
                            {{user.surname}}
                        </td>
                        <td>
                            {{user.forename}}
                        </td>
                        <td>
                            {{user.isAdmin ? $t('yes') : $t('no')}}
                        </td>
                        <td>
                            <a href="#" :title="$t('edit')" @click.prevent="setEdit(user)" >
                                <span class="fa fa-edit fa-2x"></span>
                            </a>
                            <a href="#" :title="$t('delete')" @click.prevent="setDelete(user)">
                                <span class="fa fa-trash fa-2x"></span>
                            </a>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
        <b-modal id="modal-edit-user" :title="$t('user.title.edit')" :ok-title="$t('save')" :cancel-title="$t('cancel')" @ok="updateUser" @hidden="resetUserInfo">
            <form ref="formUpdate" @submit.prevent="updateUser()">
                <user-info :value="userInfo" :isEdit="true"></user-info>
            </form>
        </b-modal>
        <b-modal id="modal-delete-user" :title="$t('title.delete')" :ok-title="$t('yes')" :cancel-title="$t('no')" @ok="deleteUser()">
            {{$t('user.question.delete')}}
        </b-modal>
    </div>
</template>

<script>
import UserInfo from '@/components/UserInfo.vue';
import {userManagement} from '@/plugins/global';

export default {
    components:{
        'user-info': UserInfo
    },
    data(){
        return {
            userInfo: {},
            selectedUser: undefined,
            users: [],
            loading: {
                createUser: false,
                fileUpload: false,
            },
            searchText: undefined,
            isAdmin: false
        }
    },
    created(){
        this.getUsers();
    },
    computed: {
        roles(){
            return userManagement.roles();
        },
        rolesWithAll(){
            return userManagement.rolesWithAll();
        },
        filteredUsers(){
            return userManagement.filteredUsers({users: this.users, searchText: this.searchText});
        }
    },
    methods:{
        resetUserInfo(){
            this.userInfo = {};
        },
        setEdit(user){
            this.userInfo = {...user};
            this.selectedUser = user;
            this.$bvModal.show('modal-edit-user');
        },
        setDelete(user){
            this.selectedUser = user;
            this.$bvModal.show('modal-delete-user');
        },
        async submitUsers(){
            this.loading.fileUpload = true;
            const formData = new FormData();
            formData.append('file',this.$refs.file.files[0]);
            formData.append('isAdmin', this.isAdmin)
            this.$refs.file.value = '';
            try{
                const response = await this.$store.dispatch('createUsers', formData);
                this.$bvToast.toast(this.$t('user.created'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
                this.users.push(...response.data);
                this.users.sort((a,b) => a.matriculationNumber.localeCompare(b.matriculationNumber));
            }
            catch{
                this.$bvToast.toast(this.$t('user.create.error'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
            finally{
                this.loading.fileUpload = false;
            }
        },
        async createUser(modal){
            if(modal){
                modal.preventDefault();
            }
            if(!this.$refs.formCreate.checkValidity()){
                this.$refs.formCreate.reportValidity();
            }
            else{
                this.loading.createUser = true;
                try{
                    await this.$store.dispatch('createUser', this.userInfo);
                    this.$bvModal.hide('modal-new-user');
                    this.$bvToast.toast(this.$t('user.created'), {
                        title: this.$t('success'),
                        variant: 'success',
                        appendToast: true
                    });
                    this.users.push(this.userInfo);
                    this.users.sort((a,b) => a.matriculationNumber.localeCompare(b.matriculationNumber));
                }
                catch(error){
                    const status = error.response.data.errorResponseCode;
                    let message;
                    if(status === 470){
                        message = this.$t('user.error.usernameExists');
                    }
                    else if(status === 471){
                        message = this.$t('user.error.matriculationNumberExists');
                    }
                    else{
                        message = this.$t('user.error.create');
                    }
                    this.$bvToast.toast(message, {
                        title: this.$t('error'),
                        variant: 'danger',
                        appendToast: true
                    });
                }
                finally{
                    this.loading.createUser = false;
                }
            }
        },
        async updateUser(modal){
            if(modal){
                modal.preventDefault();
            }
            if(!this.$refs.formUpdate.checkValidity()){
                this.$refs.formUpdate.reportValidity();
            }
            else{
                this.loadingUpdateUser = true;
                try{
                    await this.$store.dispatch('updateUser', this.userInfo);
                    Object.assign(this.selectedUser,this.userInfo); //update local
                    this.$bvModal.hide('modal-edit-user');
                    this.$bvToast.toast(this.$t('user.saved'), {
                        title: this.$t('success'),
                        variant: 'success',
                        appendToast: true
                    });
                    this.resetUserInfo();
                }
                catch{
                    this.$bvToast.toast(this.$t('user.error.save'), {
                        title: this.$t('error'),
                        variant: 'danger',
                        appendToast: true
                    });
                }
                finally{
                    this.loadingUpdateUser = false;
                }
            }
        },
        async deleteUser(){
            try{
                const matriculationNumber = this.selectedUser.matriculationNumber;
                await this.$store.dispatch('deleteUser', matriculationNumber);
                const index = this.users.findIndex(user => user.matriculationNumber === matriculationNumber);
                this.users.splice(index, 1);
                this.$bvToast.toast(this.$t('user.deleted'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }
            catch{
                this.$bvToast.toast(this.$t('user.error.delete'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
        },
        async getUsers(){
            try{
                const response = await this.$store.dispatch('getUsers');
                this.users = response.data;
            }
            catch{
                this.$bvToast.toast(this.$t('users.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
        }
    }
}
</script>