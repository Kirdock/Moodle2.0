<template>
    <div class="row col-md-12">
        <div class="form-horizontal col-md-5">
            <div class="form-group">
                <label for="name" class="control-label required">{{ $t('name') }}</label>
                <input id="name" type="text" class="form-control"  v-model="value.name" required>
            </div>
            <div class="form-group">
                <label for="description" class="control-label" :class="value.subExamples.length === 0 ? 'required' : ''">{{ $t('description') }}</label>
                <textarea id="description" type="text" class="form-control"  v-model="value.description" :required="value.subExamples.length === 0">
                </textarea>
            </div>
            <div v-if="value.subExamples.length === 0">
                <div class="form-group">
                    <label for="weighting" class="control-label required">{{ $t('weighting') }}</label>
                    <div class="col-md-4" style="padding-left: 0px">
                        <i-input id="weighting" type="number" min="1" class="form-control"  v-model="value.weighting" required> </i-input>
                    </div>
                </div>
                <div class="form-group">
                    <label for="points" class="control-label required">{{ $t('points') }}</label>
                    <div class="col-md-4" style="padding-left: 0px">
                        <i-input id="points" type="number" class="form-control"  v-model="value.points" required> </i-input>
                    </div>
                </div>
                <div class="form-check" style="margin-top: 20px">
                    <input id="submitFile" type="checkbox" class="form-check-input" v-model="value.submitFile" @click="submitFile_changed()">
                    <label for="submitFile"   class="form-check-label" style="margin-right:15px">
                        {{$t('submitFile')}}
                    </label>
                </div>
                <div class="form-group" v-if="value.submitFile">
                    <label for="validator" class="control-label">{{ $t('validator') }}</label>
                    <input id="validator" type="text" class="form-control"  v-model="value.validator">
                </div>
                <div class="form-group" v-show="value.submitFile && !value.validator">
                    <label for="supportedFileTypes" class="control-label required">{{ $t('supportedFileTypes') }}</label>
                    <multiselect v-model="value.supportedFileTypes" id="supportedFileTypes" label="name" open-direction="bottom"
                                :placeholder="$t('typeToSearch')"
                                track-by="value"
                                selectLabel=""
                                :options="supportedFileTypes"
                                :multiple="true"
                                :searchable="true"
                                :clear-on-select="false"
                                :close-on-select="false"
                                :options-limit="300"
                                
                                :max-height="600"
                                :show-no-results="false"
                                :hide-selected="true"
                                required
                                >
                    </multiselect>
                </div>
                <div class="form-check" style="margin-top: 20px">
                    <input id="mandatory" type="checkbox" class="form-check-input" v-model="value.mandatory">
                    <label for="mandatory"   class="form-check-label" style="margin-right:15px">
                        {{$t('mandatory')}}
                    </label>
                </div>
            </div>
        </div>
        <div class="form-horizontal offset-md-2 col-md-5" v-if="hasSubExamples">
            <button class="btn btn-primary" type="button" style="margin-top: 10px" @click="newSubExample()">
                <span class="fa fa-plus"></span>
                {{$t('subExample.new')}}
            </button>
            <table class="table">
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
                            <a href="javascript:void(0)" :title="$t('delete')" v-b-modal="'modal-delete-subExample'">
                                <span class="fa fa-trash fa-2x"></span>
                            </a>
                            <b-modal id="modal-delete-subExample" :title="$t('title.delete')" :ok-title="$t('yes')" :cancel-title="$t('no')" @ok="removeSubExample(exampleIndex)">
                                {{$t('example.question.delete')}}
                            </b-modal>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
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
    data(){
        return {
            supportedFileTypes: [
                {
                    name: 'Word',
                    value: 'application/msword, application/vnd.openxmlformats-officedocument.wordprocessingml.document'
                },
                {
                    name: 'Excel',
                    value: 'application/msexcel, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
                }, 
                {
                    name: 'Archiv-Dateien',
                    value: '*.zip, *.rar'
                }
            ]
        }
    },
    methods:{
        removeSubExample(index){
            this.value.subExamples.splice(index,1);
            this.adjustOrder(this.value.subExamples, index);
        },
        submitFile_changed(){
            if(!this.value.submitFile){
                this.value.validator = this.value.supportedFiles = undefined;
            }
        },
        newSubExample(){
            this.value.subExamples.push(this.buildExample(this.$t('subExample.name'), this.value.subExamples.length));
        }
    }
}
</script>