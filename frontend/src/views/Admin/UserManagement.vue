<template>
    <div class="UserManagemgnt">
        <h1>{{$t('user.management')}}</h1>
        <ol class="breadcrumb">
            <li class="breadcrumb-item">
                <router-link to="/">{{ $t('home') }}</router-link>
            </li>
            <li class="breadcrumb-item">
                <router-link to="/Admin" >{{ $t('admin') }}</router-link>
            </li>
            <li class="breadcrumb-item active">{{$t('user.management')}}</li>
        </ol>
        <label class="control-label requiredField" style="margin-left: 10px">{{ $t('requiredField') }}</label>
        <b-tabs content-class="mt-3">
            <b-tab :title="$t('create')" active>
                <div class="row" style="margin-left: 0px">
                    <div class="form-horizontal col-md-4">
                        <form @submit.prevent @submit="createUser()">
                            <div class="form-group">
                                <label class="control-label" for="userRole">{{$t('role')}}</label>
                                <select class="form-control" id="userRole" v-model="userInfo.role">
                                    <option v-for="role in roles" :value="role.key" :key="role.key">
                                        {{role.value}}
                                    </option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="username" class="control-label required">{{ $t('username') }}</label> 
                                <input id="username" type="text" class="form-control" v-model="userInfo.username" required>
                            </div>
                            <div class="form-group">
                                <label for="matrikelnummer" class="control-label required">{{ $t('matrikelnummer') }}</label>
                                <input id="matrikelnummer" type="text" class="form-control"  pattern="[0-9]{8}" v-model="userInfo.matrikelnummer" :title="$t('eightDigitNumber')" required>
                            </div>
                            <div class="form-group">
                                <label for="surname" class="control-label required">{{ $t('surname') }}</label>
                                <input id="surname" type="text" class="form-control" v-model="userInfo.surname" required>
                            </div>
                            <div class="form-group">
                                <label for="forename" class="control-label required">{{ $t('forename') }}</label>
                                <input id="forename" type="text" class="form-control" v-model="userInfo.forename" required>
                            </div>
                            <div class="form-inline">
                                <button class="btn btn-primary" type="submit">
                                    <span class="fa fa-plus"></span>
                                    {{ $t('create') }}
                                </button>
                                <div class="offset-md-1 form-inline" v-if="loadingCreateUser">
                                    <span class="fa fa-sync fa-spin"></span>
                                    <label class="control-label">{{ $t('loading') }}...</label>
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="form-horizontal col-md-4 offset-md-1">
                        <div class="form-inline">
                            <label class="btn btn-primary finger">
                                <span class="fas fa-upload" style="margin-right: 2px"></span>
                                {{ $t('uploadCSV') }}
                                <input type="file" class="d-none" id="file" ref="file" accept=".csv" @change="submitUsers()"/>
                            </label>
                            <div class="offset-md-1 form-inline" v-if="loadingFileUpload">
                                <span class="fa fa-sync fa-spin"></span>
                                <label class="control-label">{{ $t('loading') }}...</label>
                            </div>
                        </div>
                    </div>
                </div>
            </b-tab>
            <b-tab :title="$t('edit')" @click="getUsers()">
                <div class="row col-md-12">
                    <div class="form-group col-md-6">
                        <label for="selectUser" class="control-label required">{{$t('user.user')}}</label>
                        <select class="form-control" v-model="selectedUser" id="selectUser" required>
                            <option v-for="user in users" :value="user.matrikelNummer" :key="user.matrikelNummer">
                                {{user.matrikelNummer}} {{user.surname}} {{user.forename}}
                            </option>
                        </select>
                    </div>
                    <div class="col-md-6" style="margin-top: 30px">
                        <button class="btn btn-danger" v-b-modal="'modal-delete-user'">
                            <span class="fa fa-trash"></span>
                            {{ $t('delete') }}
                        </button>
                        <b-modal id="modal-delete-user" :title="$t('title.delete')" :ok-title="$t('yes')" :cancel-title="$t('no')" @ok="deleteUser(selectedUser)">
                            {{$t('user.question.delete')}}
                        </b-modal>
                    </div>
                </div>
            </b-tab>
        </b-tabs>
    </div>
</template>

<script>
export default {
    data(){
        return {
            userInfo: {},
            selectedUser: undefined,
            users: [],
            loadingCreateUser: false,
            loadingFileUpload: false
        }
    },
    created(){
        this.resetUserInfo();
    },
    computed: {
        roles(){
            return this.$store.getters.userRoles;
        }
    },
    methods:{
        resetUserInfo(){
            this.userInfo = {
                role: 's'
            };
        },
        submitUsers(){
            this.loadingFileUpload = true;
            const formData = new FormData();
            formData.append('file',this.$refs.file.files[0]);
            this.$refs.file.value = '';
            this.$store.dispatch('createUsers', formData).then(response =>{
                this.$bvToast.toast(this.$t('user.created'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }).catch(()=>{
                this.$bvToast.toast(this.$t('user.create.error'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }).finally(()=>{
                this.loadingFileUpload = false;
            });
        },
        createUser(){
            this.loadingCreateUser = true;
            this.$store.dispatch('createUser', this.userInfo).then(response=>{
                this.$bvToast.toast(this.$t('user.created'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
                this.resetUserInfo();
                if(this.users.length !== 0){
                    this.getUsers(true);
                }
            }).catch(()=>{
                this.$bvToast.toast(this.$t('user.error.create'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }).finally(()=>{
                this.loadingCreateUser = false;
            });
        },
        deleteUser(matrikelNummer){
            this.$store.dispatch('deleteUser', matrikelNummer).then(response =>{
                this.getUsers(true);
                this.$bvToast.toast(this.$t('user.deleted'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }).catch(()=>{
                this.$bvToast.toast(this.$t('user.error.delete'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            })
        },
        getUsers(forceUpdate){
            if(forceUpdate || this.users.length === 0){
                this.$store.dispatch('getUsers',{}).then(response=>{
                    this.users = response.data;
                }).catch(()=>{
                    this.$bvToast.toast(this.$t('user.error.get'), {
                        title: this.$t('error'),
                        variant: 'danger',
                        appendToast: true
                    });
                });
            }
        }
    }
}
</script>