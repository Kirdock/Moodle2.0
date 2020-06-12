<template>
    <div class="exerciseSheet">
        <h1 id="sheetName">{{sheetInfo.name}}</h1>
        <h2>{{$t('submissionDate')}}: {{new Date(sheetInfo.submissionDate).toLocaleString()}}</h2>
        <h2>{{$t('requirements')}}</h2>
        <div class="form-group">
            <label class="control-label">{{$t('minKreuzel')}}: <strong>{{sheetInfo.minKreuzel || 0}}%</strong></label>
        </div>
        <div class="form-group">
            <label class="control-label">{{$t('minPoints')}}: <strong>{{sheetInfo.minPoints || 0}}%</strong></label>
        </div>
        <form @submit.prevent="saveKreuzel()">
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
                        <kreuzel-info :hasSubExamples="hasSubExamples" :key="example.id" :isParent="true" :value="example" :supportedFileTypes="supportedFileTypes" :includeThird="sheetInfo.includeThird" :deadlineReached="deadlineReached" :isDeadlineReached="isDeadlineReached"> </kreuzel-info>
                        <kreuzel-info :hasSubExamples="hasSubExamples" v-for="subExample in example.subExamples" :key="subExample.id" :isParent="false" :value="subExample" :supportedFileTypes="supportedFileTypes" :includeThird="sheetInfo.includeThird" :deadlineReached="deadlineReached" :isDeadlineReached="isDeadlineReached"> </kreuzel-info>
                    </template>
                    <tr style="font-weight: bold">
                        <td>
                            {{$t('total')}}
                        </td>
                        <td v-if="hasSubExamples"></td>
                        <td :style="minimumRequired(mandatoryTotal, mandatory, mandatoryTotal)">
                            {{mandatory}}/{{mandatoryTotal}}
                        </td>
                        <td></td>
                        <td :style="minimumRequired(sheetInfo.minPoints, points, pointsTotal)">
                            {{points}}/{{pointsTotal}}
                        </td>
                        <td :style="minimumRequired(sheetInfo.minKreuzel, kreuzel, kreuzelTotal)">
                            {{kreuzel}}/{{kreuzelTotal}}
                        </td>
                        <td></td>
                    </tr>
                </tbody>
            </table>
            <div class="form-group">
                <button type="submit" class="btn btn-primary" :disabled="deadlineReached">
                    <span class="fa fa-sync fa-spin" v-if="loading"></span>
                    <span class="fa fa-save" v-else></span>
                    {{$t('save')}}
                </button>
            </div>
        </form>
    </div>
</template>

<script>
import {exampleManagement, calcManagement} from '@/plugins/global';
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
    computed:{
        mandatory(){
            let total = 0;
            for(let example of this.sheetInfo.examples){
                if(example.subExamples.length !== 0){
                    for(let subExample of example.subExamples){
                        if(subExample.state !== 'n' && subExample.mandatory){
                            total++;
                        }
                    }
                }
                else if(example.state !== 'n' && example.mandatory){
                    total++;
                }
            }
            return total;
        },
        mandatoryTotal(){
            let total = 0;
            for(let example of this.sheetInfo.examples){
                if(example.subExamples.length !== 0){
                    for(let subExample of example.subExamples){
                        if(subExample.mandatory){
                            total++;
                        }
                    }
                }
                else if(example.mandatory){
                    total++;
                }
            }
            return total;
        },
        kreuzel(){
            let total = 0;
            for(let example of this.sheetInfo.examples){
                if(example.subExamples.length !== 0){
                    for(let subExample of example.subExamples){
                        if(subExample.state !== 'n'){
                            total++;
                        }
                    }
                }
                else if(example.state !== 'n'){
                    total++;
                }
            }
            return total;
        },
        kreuzelTotal(){
            let total = 0;
            for(let example of this.sheetInfo.examples){
                if(example.subExamples.length !== 0){
                    for(let subExample of example.subExamples){
                        total++;
                    }
                }
                else{
                    total++;
                }
            }
            return total;
        },
        points(){
            let total = 0;
            for(let example of this.sheetInfo.examples){
                if(example.subExamples.length !== 0){
                    for(let subExample of example.subExamples){
                        if(subExample.state !== 'n'){
                            total += subExample.weighting * subExample.points;
                        }
                    }
                }
                else if(example.state !== 'n'){
                    total += example.weighting * example.points;
                }
            }
            return total;
        },
        pointsTotal(){
            let total = 0;
            for(let example of this.sheetInfo.examples){
                if(example.subExamples.length !== 0){
                    for(let subExample of example.subExamples){
                        total += subExample.weighting * subExample.points;
                    }
                }
                else{
                    total += example.weighting * example.points;
                }
            }
            return total;
        }
    },
    created(){
        this.getExerciseSheet(this.exerciseSheetId);
    },
    methods: {
        minimumRequired: calcManagement.minimumRequired,
        isDeadlineReached(){
            this.deadlineReached = new Date() >= new Date(this.sheetInfo.submissionDate);
            return this.deadlineReached;
        },
        async getExerciseSheet(id){
            try{
                await this.getFileTypes();
                const response = await this.$store.dispatch('getExerciseSheetAssigned', id);
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
                        this.$bvToast.toast(this.$t('kreuzel.saved'), {
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