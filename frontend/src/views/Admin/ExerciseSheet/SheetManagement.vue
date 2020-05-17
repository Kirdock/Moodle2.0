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
            <li class="breadcrumb-item active">
                <router-link to="/Admin/CourseManagement" >{{$t('course.management')}}</router-link></li>
            <li class="breadcrumb-item active">{{name.replace('_',' ')}}</li>
            <li class="breadcrumb-item active">{{$t('exerciseSheet.name')}}</li>
            <li class="breadcrumb-item active">{{sheetInfo.name}}</li>
        </ol>
        <b-tabs class="mt-3">
            <b-tab :title="$t('information')">
                <div class="form-horizontal col-md-4">
                    <form ref="exerciseSheet" @submit.prevent="update()">
                        <es-info v-model="sheetInfo"></es-info>
                        <div class="form-inline">
                            <b-button variant="primary" type="submit">{{ $t('update') }}</b-button>
                            <div class="offset-md-1 form-inline" v-if="loading_updateInformation">
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
//remove parameter courseName. instead get course name and number with exerciseSheet information
//Examples are all <b-tab>, so there is a plus-icon in the tab
//information in example: name, description, valid fileTypes, subExamples, weighting, points
//Sub examples: how to infinite: Example has a list of SubExamples. if you click on a subExample the parent-example-view is replaced with the sub-example and a breadcrumb
import ExerciseSheetInfo from '@/components/ExerciseSheetInfo.vue';
export default {
    props: ['courseId', 'sheetId', 'name'],
    components: {
        'es-info': ExerciseSheetInfo
    },
    created(){
        this.getSheet(this.sheetId);
    },
    data(){
        return {
            sheetInfo: {},
            loading_updateInformation: false
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
            this.loading_updateInformation = true;
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
            }).finally(()=>{
                this.loading_updateInformation = false;
            });
        }
    }
}
</script>