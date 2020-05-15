<template>
    <div class="SheetManagement">
        <h1>{{$t('course.management')}}</h1>
        <ol class="breadcrumb">
            <li class="breadcrumb-item">
                <router-link to="/">{{ $t('home') }}</router-link>
            </li>
            <li class="breadcrumb-item">
                <router-link to="/Admin" >{{ $t('admin') }}</router-link>
            </li>
            <li class="breadcrumb-item active">{{$t('course')}}</li>
            <li class="breadcrumb-item active">{{courseId}}</li>
            <li class="breadcrumb-item active">{{$t('exerciseSheet.name')}}</li>
            <li class="breadcrumb-item active">{{sheetId}}</li>
            <li class="breadcrumb-item active">{{$t('exerciseSheet.management')}}</li>
        </ol>
        <div class="form-horizontal">
            <form ref="exerciseSheet" @submit.prevent="update()">
                <es-info v-model="sheetInfo"></es-info>
            </form>
        </div>
    </div>
</template>



<script>
import ExerciseSheetInfo from '@/components/ExerciseSheetInfo.vue';
export default {
    props: ['courseId', 'sheetId'],
    components: {
        'es-info': ExerciseSheetInfo
    },
    created(){
        this.getSheet(this.sheetId);
    },
    data(){
        return {
            sheetInfo: {}
        }
    },
    methods: {
        getSheet(sheedId){
            this.$store.dispatch('getExerciseSheet', sheedId).then(response =>{
                this.sheetInfo = response.data;
            }).catch(()=>{
                this.$bvToast.toast(this.$t('exerciseSheet.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            })
        },
        update(){
            this.$store.dispatch('updateExerciseSheet', this.sheetInfo).then(()=>{
                this.$bvToast.toast(this.$t('exerciseSheet.updated'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }).catch(()=>{
                this.$bvToast.toast(this.$t('exerciseSheet.error.update'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            });
        }
    }
}
</script>