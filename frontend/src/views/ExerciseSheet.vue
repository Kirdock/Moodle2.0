<template>
    <div class="exerciseSheet">
        <h1 id="sheetName">{{sheetInfo.name}}</h1>
        <h2>{{$t('submissionDate')}}: {{new Date(sheetInfo.submissionDate).toLocaleString()}}</h2>
        <table class="table" aria-describedby="sheetName" v-if="sheetInfo.id">
            <thead>
                <th scope="col">{{$t('example.name')}}</th>
                <th scope="col" v-if="hasSubExamples">{{$t('subExample.name')}}</th>
                <th scope="col">{{$t('mandatory')}}</th>
                <th scope="col">{{$t('weighting')}}</th>
                <th scope="col">{{$t('points')}}</th>
                <th scope="col">{{$t('kreuzel.name')}}</th>
                <th scope="col">{{$t('actions')}}</th>
            </thead>
            <tbody>
                <template v-for="example in sheetInfo.examples">
                    <tr :key="example.id">
                        <td>
                            {{example.name}}
                        </td>
                        <td v-if="hasSubExamples">
                        </td>
                        <template v-if="example.subExamples.length === 0">
                            <td>
                                {{example.mandatory ? $t('yes') : $t('no')}}
                            </td>
                            <td>
                                {{example.weighting}}
                            </td>
                            <td>
                                {{example.points}}
                            </td>
                        </template>
                        <template v-else>
                            <td></td>
                            <td></td>
                            <td></td>
                        </template>
                        <kreuzel-info :value="example" :supportedFileTypes="supportedFileTypes" :includeThird="sheetInfo.includeThird" :deadlineReached="deadlineReached" :isDeadlineReached="isDeadlineReached"> </kreuzel-info>
                        <td>
                        </td>
                    </tr>
                    <tr v-for="subExample in example.subExamples" :key="subExample.id">
                        <td>
                        </td>
                        <td>
                            {{subExample.name}}
                        </td>
                        <td>
                            {{subExample.mandatory ? $t('yes') : $t('no')}}
                        </td>
                        <td>
                            {{subExample.weighting}}
                        </td>
                        <td>
                            {{subExample.points}}
                        </td>
                        <kreuzel-info :value="subExample" :supportedFileTypes="supportedFileTypes" :includeThird="sheetInfo.includeThird" :deadlineReached="deadlineReached" :isDeadlineReached="isDeadlineReached"> </kreuzel-info>
                        <td>
                        </td>
                    </tr>
                </template>
            </tbody>
        </table>
        <div class="form-group">
            <button type="button" class="btn btn-primary" @click="saveKreuzel()" :disabled="deadlineReached">
                <span class="fa fa-sync fa-spin" v-if="loading"></span>
                <span class="fa fa-save" v-else></span>
                {{$t('save')}}
            </button>
        </div>
    </div>
</template>

<script>
import {exampleManagement} from '@/plugins/global';
import kreuzelInfo from '@/components/KreuzelInfo.vue';

export default {
    components:{
        'kreuzel-info': kreuzelInfo
    },
    props: ['exerciseSheetId'],
    data(){
        return {
            sheetInfo: {},
            loading: false,
            supportedFileTypes: undefined,
            hasSubExamples: false,
            deadlineReached: false
        }
    },
    created(){
        this.getExerciseSheet(this.exerciseSheetId);
    },
    methods: {
        isDeadlineReached(){
            this.deadlineReached = new Date() >= new Date(this.sheetInfo.submissionDate);
            return this.deadlineReached;
        },
        async getExerciseSheet(id){
            try{
                await this.getFileTypes();
                const response = await this.$store.dispatch('getExerciseSheet', id);
                this.sheetInfo = response.data;
                this.hasSubExamples = this.sheetInfo.examples.some(example => example.subExamples.length > 0);
                this.isDeadlineReached();
            }
            catch{
                this.$bvToast.toast(this.$t('exerciseSheet.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
        },
        async getFileTypes(){
            try{
                const response = await this.$store.dispatch('getFileTypes');
                this.supportedFileTypes = response.data;
            }
            catch{
                this.$bvToast.toast(this.$t('fileTypes.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
        },
        async saveKreuzel(){
            if(this.sheetInfo.examples){
                if(this.isDeadlineReached()){
                    this.$bvToast.toast(this.$t('deadlineReached'), {
                        title: this.$t('error'),
                        variant: 'danger',
                        appendToast: true
                    });
                }
                else{
                    this.loading = true;
                    try{
                        await this.$store.dispatch('saveKreuzel', exampleManagement.selectMany(this.sheetInfo.examples));
                        this.$bvToast.toast(this.$t('kreuzel.save'), {
                            title: this.$t('success'),
                            variant: 'success',
                            appendToast: true
                        });
                    }
                    catch{
                        this.$bvToast.toast(this.$t('kreuzel.error.save'), {
                            title: this.$t('error'),
                            variant: 'danger',
                            appendToast: true
                        });
                    }
                    finally{
                        this.loading = false;
                    }
                }
            }
        }
    }
}
</script>