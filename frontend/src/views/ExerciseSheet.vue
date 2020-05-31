<template>
    <div class="exerciseSheet">
        <div class="form-horizontal" v-if="sheetInfo.id">
            <div class="form-group" v-for="example in sheetInfo.examples" :key="example.id">

                <div v-if="example.subExamples.length === 0">
                    <input id="kreuzel" type="checkbox" class="form-check-input" v-model="example.state">
                    <label for="kreuzel"   class="form-check-label" style="margin-right:15px">
                        {{$t('submitFile')}}
                    </label>
                </div>

                <div v-for="subExample in example.subExamples" :key="subExample.id">

                </div>
            </div>
            <div class="form-group">
                <button type="button" class="btn btn-primary" @click="saveKreuzel()">
                    <span class="fa fa-sync fa-spin" v-if="loading"></span>
                    <span class="fa fa-save" v-else></span>
                    {{$t('save')}}
                </button>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    props: ['exerciseSheetId'],
    data(){
        return {
            sheetInfo: {},
            loading: false
        }
    },
    created(){
        this.getExerciseSheet(this.exerciseSheetId);
    },
    methods: {
        async getExerciseSheet(id){
            try{
                this.sheetInfo = await this.$store.dispatch('getExerciseSheet', id).data;
            }
            catch{
                this.$bvToast.toast(this.$t('exerciseSheet.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
        },
        async saveKreuzel(){
            if(this.sheetInfo.examples){
                this.loading = true;
                const examplesReduced = this.sheetInfo.examples.reduce((examples, example) => {
                    let result = examples || [];
                    if(example.subExamples.length === 0){
                        result = result.push({
                            exampleId: example.id,
                            state: example.state,
                            description: example.state === 'm' ? example.description : undefined
                        });
                    }
                    else{
                        result = result.concat(example.subExamples.map(subExample => {
                            return {
                                exampleId: subExample.id,
                                state: subExample.state,
                                description: subExample.state === 'm' ? subExample.description : undefined
                            }
                        }));
                    }
                    return result;
                });
                try{
                    await this.$store.dispatch('saveKreuzel', examplesReduced);
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
</script>