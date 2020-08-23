<template>
    <div class="kreuzelList">
        <div class="form-group">
            <label :for="`exerciseSheets_${_uid}`" class="control-label">
                {{$t('exerciseSheet.name')}} <span class="fa fa-sync fa-spin" v-if="loading.exerciseSheet"></span>
            </label>
            <select class="form-control" v-model="selectedExerciseSheet" @change="getKreuzel()">
                <option v-for="sheet in exerciseSheets" :value="sheet.id" :key="sheet.id">
                    {{sheet.name}}
                </option>
            </select>
        </div>
        <template v-if="selectedExerciseSheet !== undefined">
            <div class="form-group">
                <button class="btn btn-primary" @click="getKreuzelList()">
                    <span class="fa fa-sync fa-spin" v-if="loading.pdf"></span>
                    <span class="fa fa-download" v-else></span>
                    {{$t('pdf')}}
                </button>
            </div>
            <div class="form-group">
                <b-form-checkbox v-model="editMode" name="check-button" switch>
                    {{$t('edit')}}
                </b-form-checkbox>
            </div>
            <div class="table_scroll">
                <table class="table nowrap table-hover" aria-describedby="modal-presented">
                    <thead>
                        <th scope="col">{{$t('matriculationNumber')}}</th>
                        <th scope="col">{{$t('surname')}}</th>
                        <th scope="col">{{$t('forename')}}</th>
                        <th scope="col" v-for="example in kreuzelInfo.examples" :key="example.id">{{example.name}}</th>
                    </thead>
                    <tbody>
                        <tr v-for="kreuzel in kreuzelInfo.kreuzel" :key="`${kreuzel.matriculationNumber} ${kreuzel.exampleId}`">
                            <td>
                                {{kreuzel.matriculationNumber}}
                            </td>
                            <td>
                                {{kreuzel.surname}}
                            </td>
                            <td>
                                {{kreuzel.forename}}
                            </td>
                            <td v-for="(state, index) in kreuzel.states" :key="index">
                                <select v-if="editMode" class="form-control" v-model="state.type" style="width: 60px">
                                    <option v-for="stateType in stateTypes" :value="stateType.key" :key="stateType.key">
                                        {{stateType.value}}
                                    </option>
                                </select>
                                <template v-else>
                                    {{state.type === 'n' ? '' : state.type === 'y' ? 'X' : 'O'}}
                                </template>
                                <textarea v-if="state.description" class="form-control" readonly="true" :value="state.description"></textarea>
                                <a href="#" @click.prevent="setSelectedKreuzelResult(state.result)" :title="$t('result')" v-if="state.result && state.result.length !== 0">
                                    <span class="fa fa-list fa-2x"></span>
                                </a>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <button class="btn btn-primary" @click="saveKreuzel()">
                <span class="fa fa-sync fa-spin"  v-if="loading.save"></span>
                <span class="fa fa-save" v-else></span>
                {{$t('save')}}
            </button>
        </template>
        <b-modal id="modal-kreuzelResult" :title="$t('kreuzel.name')" size="xl" hide-footer>
            <kreuzel-result v-model="selectedKreuzelResult"></kreuzel-result>
        </b-modal>
    </div>
</template>


<script>
import {fileManagement} from '@/plugins/global';
import kreuzelResult from '@/components/KreuzelResult.vue';
export default {
    name: 'kreuzel-list',
    components:{
        'kreuzel-result': kreuzelResult
    },
    props: ['exerciseSheets'],
    data(){
        return {
            kreuzelInfo: {},
            selectedExerciseSheet: undefined,
            editMode: false,
            selectedKreuzelResult: {},
            loading: {
                save: false,
                exerciseSheet: false,
                pdf: false
            }
        }
    },
    computed:{
        stateTypes(){
            return this.kreuzelInfo.includeThird ? [
                {
                    value: 'X',
                    key: 'y'
                },
                {
                    value: 'O',
                    key: 'm'
                },
                {
                    value: '',
                    key: 'n'
                }
            ] : [
                {
                    value: 'X',
                    key: 'y'
                },
                {
                    value: '',
                    key: 'n'
                }
            ]
        }
    },
    methods:{
        async getKreuzel(){
            this.loading.exerciseSheet = true;
            try{
                const response = await this.$store.dispatch('getKreuzel', this.selectedExerciseSheet);
                this.kreuzelInfo = response.data;
            }
            catch{
                this.$bvToast.toast(this.$t('kreuzel.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
            finally{
                this.loading.exerciseSheet = false;
            }
        },
        async getKreuzelList(){
            this.loading.pdf = true;
            try{
                const response = await this.$store.dispatch('getKreuzelList', this.selectedExerciseSheet);
                fileManagement.download(response);
            }
            catch{
                this.$bvToast.toast(this.$t('kreuzel.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
            finally{
                this.loading.pdf = false;
            }
        },
        async saveKreuzel(){
            try{
                this.loading.save = true;
                let data = [];
                for(let kreuzel of this.kreuzelInfo.kreuzel){
                    kreuzel.states.forEach((state, index) =>{
                        data.push({
                            matriculationNumber: kreuzel.matriculationNumber,
                            exampleId: this.kreuzelInfo.examples[index].id,
                            state: state.type
                        })
                    });
                }
                await this.$store.dispatch('saveKreuzelMulti', data);
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
                this.loading.save = false;
            }
        },
        setSelectedKreuzelResult(result){
            this.selectedKreuzelResult = result;
            this.$bvModal.show('modal-kreuzelResult');
        }
    }
}
</script>