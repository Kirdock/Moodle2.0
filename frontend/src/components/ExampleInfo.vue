<template>
    <div class="row col-md-12">
        <div class="form-horizontal col-md-5">
            <div class="form-group">
                <label :for="`eInfoName${_uid}`" class="control-label required">{{ $t('name') }}</label>
                <input :id="`eInfoName${_uid}`" type="text" class="form-control"  v-model="value.name" required>
            </div>
            <div class="form-group">
                <label :for="`eInfoDescription${_uid}`" class="control-label" :class="value.subExamples.length === 0 ? 'required' : ''">{{ $t('description') }}</label>
                <textarea :id="`eInfoDescription${_uid}`" class="form-control"  v-model="value.description" :required="value.subExamples.length === 0">
                </textarea>
            </div>
            <div v-if="value.subExamples.length === 0">
                <div class="form-group">
                    <label :for="`eInfoWeighting${_uid}`" class="control-label required">{{ $t('weighting') }}</label>
                    <div class="col-md-4" style="padding-left: 0px">
                        <i-input :id="`eInfoWeighting${_uid}`" type="number" min="1" class="form-control"  v-model="value.weighting" required> </i-input>
                    </div>
                </div>
                <div class="form-group">
                    <label :for="`eInfoPoints${_uid}`" class="control-label required">{{ $t('points') }}</label>
                    <div class="col-md-4" style="padding-left: 0px">
                        <i-input :id="`eInfoPoints${_uid}`" type="number" class="form-control"  v-model="value.points" required> </i-input>
                    </div>
                </div>
                <div class="form-check" style="margin-top: 20px">
                    <input :id="`eInfoSubmitFile${_uid}`" type="checkbox" class="form-check-input" v-model="value.submitFile" @click="submitFile_changed()">
                    <label :for="`eInfoSubmitFile${_uid}`"   class="form-check-label" style="margin-right:15px">
                        {{$t('submitFile')}}
                    </label>
                </div>
                <div class="form-group" v-if="value.submitFile && selectedfileTypes.length === 0">
                    <label :for="`eInfoValidator${_uid}`" class="control-label">{{ $t('validator') }}</label>
                    <input :id="`eInfoValidator${_uid}`" type="text" class="form-control"  v-model="value.validator" @input="resetFileTypes" >
                </div>
                <div class="form-group" v-show="value.submitFile && !value.validator">
                    <label :for="`eInfoSupportedFileTypes${_uid}`" class="control-label required">{{ $t('supportedFileTypes') }}</label>
                    <multiselect v-model="selectedfileTypes" :id="`eInfoSupportedFileTypes${_uid}`" label="name" open-direction="bottom" @input="fileTypesChanged"
                                :placeholder="$t('typeToSearch')"
                                track-by="value"
                                selectLabel=""
                                :selectedLabel="$t('selected')"
                                :options="fileTypes"
                                :multiple="true"
                                :searchable="true"
                                :clear-on-select="false"
                                :close-on-select="false"
                                :options-limit="300"
                                :minHeight="800"
                                :deselectLabel="$t('remove')"
                                :show-no-results="false"
                                :hide-selected="false"
                                required
                                >
                    </multiselect>
                </div>
                <div class="form-check" style="margin-top: 20px">
                    <input :id="`eInfoMandatory${_uid}`" type="checkbox" class="form-check-input" v-model="value.mandatory">
                    <label :for="`eInfoMandatory${_uid}`"   class="form-check-label" style="margin-right:15px">
                        {{$t('mandatory')}}
                    </label>
                </div>
            </div>
        </div>
        <div class="form-horizontal offset-md-2 col-md-5" v-if="hasSubExamples">
            <h1 :id="`eInfoHeader${_uid}`">{{$t('subExamples.name')}}</h1>
            <button class="btn btn-primary" type="button" style="margin-top: 10px" @click="newSubExample()">
                <span class="fa fa-plus"></span>
                {{$t('new')}}
            </button>
            <table class="table" :aria-describedby="`eInfoHeader${_uid}`">
                <thead>
                    <th scope="col">{{$t('name')}}</th>
                    <th scope="col">{{$t('points')}}</th>
                    <th scope="col">{{$t('actions')}}</th>
                </thead>
                <tbody>
                    <tr v-for="(subExample, exampleIndex) in value.subExamples" :key="subExample.id">
                        <td>
                            {{subExample.name}}
                        </td>
                        <td>
                            {{subExample.points}}
                        </td>
                        <td>
                            <a href="javascript:void(0)" :title="$t('edit')" @click="setSelectedExample(subExample)">
                                <span class="fa fa-edit fa-2x"></span>
                            </a>
                            <a href="javascript:void(0)" :title="$t('delete')" v-b-modal="`modal-delete-subExample${_uid}`" @click="selectedExample_delete = exampleIndex">
                                <span class="fa fa-trash fa-2x"></span>
                            </a>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
        <b-modal :id="`modal-delete-subExample${_uid}`" :title="$t('title.delete')" :ok-title="$t('yes')" :cancel-title="$t('no')" @ok="removeSubExample(selectedExample_delete)">
            {{$t('example.question.delete')}}
        </b-modal>
    </div>
</template>

<script>
import Multiselect from 'vue-multiselect';
import 'vue-multiselect/dist/vue-multiselect.min.css';

export default {
    name: 'example-info',
    props: ['value', 'hasSubExamples', 'adjustOrder', 'buildExample', 'setSelectedExample'],
    components: {
        Multiselect
    },
    created(){
        this.getFileTypes();
    },
    data(){
        return {
            selectedExample_delete: undefined,
            selectedfileTypes: [],
            fileTypes: []
        }
    },
    methods:{
        resetFileTypes(){
            this.selectedfileTypes = [];
            this.value.supportedFileTypes = [];
        },
        getFileTypes(){
            this.$store.dispatch('getFileTypes').then(response =>{
                this.fileTypes = response.data;
                this.selectedfileTypes = this.fileTypes.filter(fileType => (this.value.supportedFileTypes).some(sfileType => sfileType.id === fileType.id));
            }).catch(()=>{
                this.$bvToast.toast(this.$t('fileTypes.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            })
        },
        removeSubExample(index){
            this.value.subExamples.splice(index,1);
            this.adjustOrder(this.value.subExamples, index);
        },
        submitFile_changed(){
            if(!this.value.submitFile){
                this.value.validator = this.value.supportedFileTypes = undefined;
            }
        },
        newSubExample(){
            this.value.subExamples.push(this.buildExample(this.$t('subExample.name'), this.value.subExamples.length));
        },
        fileTypesChanged(fileTypes){
            this.value.supportedFileTypes = fileTypes.map(value => value.id);
        }
    }
}
</script>