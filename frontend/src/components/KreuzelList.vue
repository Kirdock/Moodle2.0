<template>
    <div class="kreuzelList">
        <div class="form-group">
            <label :for="`exerciseSheets_${_uid}`" class="control-label">
                {{$t('exerciseSheets.name')}}
            </label>
            <select class="form-control" v-model="selectedExerciseSheet" @change="getKreuzelList()">
                <option v-for="sheet in exerciseSheets" :value="sheet.id" :key="sheet.id">
                    {{sheet.name}}
                </option>
            </select>
        </div>
        <div class="form-group">
            <b-form-checkbox v-model="editMode" name="check-button" switch>
                {{$t('edit')}}
            </b-form-checkbox>
        </div>
        <table class="table" aria-describedby="modal-presented">
            <thead>
                <th scope="col">{{$t('matriculationNumber')}}</th>
                <th scope="col">{{$t('surname')}}</th>
                <th scope="col">{{$t('forename')}}</th>
                <th scope="col" v-for="example in examples" :key="example.id">{{example.name}}</th>
            </thead>
            <tbody>
                <tr v-for="kreuzel in kreuzelList" :key="`${kreuzel.matriculationNumber} ${kreuzel.exampleId}`">
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
                        <select v-if="editMode" class="form-control" v-model="kreuzel.states[index]" style="width: 60px">
                            <option v-for="stateType in stateTypes" :value="stateType.key" :key="stateType.key">
                                {{stateType.value}}
                            </option>
                        </select>
                        <label class="control-label" v-else>
                            {{state === 'n' ? '' : state === 'y' ? 'X' : 'O'}}
                        </label>
                    </td>
                </tr>
            </tbody>
        </table>
        <button class="btn btn-primary" @click="saveKreuzel()">
            <span class="fa fa-save"></span>
            {{$t('save')}}
        </button>
    </div>
</template>


<script>
export default {
    name: 'kreuzel-list',
    props: ['exerciseSheets'],
    data(){
        return {
            includeThird: false,
            selectedExerciseSheet: {},
            editMode: false,
            examples: [],
            kreuzelList: []
        }
    },
    computed:{
        stateTypes(){
            return this.includeThird ? [
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
        async getKreuzelList(){
            try{
                const response = await this.$store.dispatch('getKreuzelList', this.selectedExerciseSheet.id);
                this.includeThird = response.data.includeThird;
                this.examples = response.data.examples;
                this.kreuzelList = response.data.kreuzel;
            }
            catch{
                this.$bvToast.toast(this.$t('kreuzel.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
        },
        async saveKreuzel(){
            try{
                let data = [];
                for(let kreuzel of this.kreuzelList){
                    kreuzel.states.forEach((state, index) =>{
                        data.push({
                            matriculationNumber: kreuzel.matriculationNumber,
                            exampleId: this.examples[index].id,
                            state
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
        }
    }
}
</script>