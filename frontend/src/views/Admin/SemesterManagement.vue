<template>
    <div class="semesterManagement">
        <h1>{{$t('semester.create')}}</h1>
        <ol class="breadcrumb">
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
                    <input id="typeSummer" type="radio" value="S" class="form-check-input"  v-model="semesterInfo.type">
                    <label for="typeSummer" class="form-check-label">
                        {{$t('semester.summer')}}
                    </label>
                </div>
                <div class="form-check">
                    <input id="typeWinter" type="radio" value="W" class="form-check-input"  v-model="semesterInfo.type">
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
                type: new Date().getMonth() > 1 && new Date().getMonth() < 9 ? 'S' : 'W'
            }
        }
    },
    created(){
        
    },
    methods:{
        async create(){
            this.loadingCreate = true;
            try{
                await this.$store.dispatch('createSemester', this.semesterInfo);
                this.$bvToast.toast(this.$t('semester.created'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }
            catch(error){
                const status = error.response.data.errorResponseCode;
                
                this.$bvToast.toast(status === 472 ? this.$t('semester.error.alreadyExists') : this.$t('semester.error.create'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
            finally{
                this.loadingCreate = false;
            }
        }
    }
}
</script>