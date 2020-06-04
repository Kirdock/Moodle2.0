<template>
    <div class="semesterManagement">
        <h1>{{$t('semester.create')}}</h1>
        <ol class="breadcrumb">
            <li class="breadcrumb-item">
                <router-link to="/">{{ $t('home') }}</router-link>
            </li>
            <li class="breadcrumb-item">
                <router-link :to="{name:'Admin'}" >{{ $t('admin') }}</router-link>
            </li>
            <li class="breadcrumb-item active">{{$t('semester.management')}}</li>
        </ol>
        <div class="form-horizontal col-md-4">
            <form @submit.prevent @submit="create()">
                <div class="form-group">
                    <label for="year" class="control-label required">{{$t('year')}}</label>
                    <i-input id="year" class="form-control"  :max="maxYear" :min="maxYear-100" v-model="semesterInfo.year" required></i-input>
                </div>
                <div class="form-check">
                    <input id="typeSummer" type="radio" value="s" class="form-check-input"  v-model="semesterInfo.type">
                    <label for="typeSummer" class="form-check-label">
                        {{$t('semester.summer')}}
                    </label>
                </div>
                <div class="form-check">
                    <input id="typeWinter" type="radio" value="w" class="form-check-input"  v-model="semesterInfo.type">
                    <label for="typeWinter"   class="form-check-label" style="margin-right:15px">
                        {{$t('semester.winter')}}
                    </label>
                </div>
                <div class="form-inline">
                    <button class="btn btn-primary" type="submit">
                        <span class="fa fa-sync fa-spin" v-if="loadingCreate"></span>
                        <span class="fa fa-plus" v-else></span>
                        {{$t('create')}}
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>

<script>
export default {
    data(){
        return {
            loadingCreate: false,
            maxYear: new Date().getFullYear(),
            semesterInfo: {
                year: new Date().getFullYear(),
                type: new Date().getMonth() > 1 && new Date().getMonth() < 9 ? 's' : 'w'
            }
        }
    },
    created(){
        
    },
    methods:{
        create(){
            this.loadingCreate = true;
            this.$store.dispatch('createSemester', this.semesterInfo).then(response=>{
                this.$bvToast.toast(this.$t('semester.created'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }).catch(error=>{
                const status = error.response.data.errorResponseCode;
                let message;
                if(status === 472){
                    message = this.$t('semester.error.alreadyExists');
                }
                else{
                    message = this.$t('semester.error.create');
                }
                this.$bvToast.toast(message, {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }).finally(()=>{
                this.loadingCreate = false;
            });
        }
    }
}
</script>