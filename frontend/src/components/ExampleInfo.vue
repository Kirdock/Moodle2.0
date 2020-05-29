<template>
    <div>
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
                    <div class="form-group" v-show="value.submitFile">
                        <label :for="`eInfoSupportedFileTypes${_uid}`" class="control-label required">{{ $t('supportedFileTypes') }}</label>
                        <multiselect v-model="selectedfileTypes" :id="`eInfoSupportedFileTypes${_uid}`" label="name" open-direction="bottom" @input="fileTypesChanged"
                                    :placeholder="$t('searchOrAddFileType')"
                                    track-by="value"
                                    selectLabel=""
                                    :tag-placeholder="$t('addTag')"
                                    :taggable="true"
                                    @tag="addCustomFileType"
                                    :selectedLabel="$t('selected')"
                                    :options="fileTypes"
                                    :multiple="true"
                                    :searchable="true"
                                    :clear-on-select="false"
                                    :close-on-select="false"
                                    :options-limit="300"
                                    :minHeight="800"
                                    :max-height="800"
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
            <div class="form-horizontal offset-md-2 col-md-5" v-if="!isSubExample">
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
                    <tbody :id="`dragtable${_uid}`">
                        <tr v-for="(subExample, exampleIndex) in value.subExamples" :key="subExample.id">
                            <td>
                                <span class="fas fa-bars handle"></span>
                                {{subExample.name}}
                            </td>
                            <td>
                                {{subExample.points}}
                            </td>
                            <td>
                                <a href="#" :title="$t('edit')" @click.prevent="setSelectedExample(subExample); setFileTypes()">
                                    <span class="fa fa-edit fa-2x"></span>
                                </a>
                                <a href="#" :title="$t('delete')" v-b-modal="'modal-delete-example'" type="button" @click.prevent="setDeleteExample(subExample.id, exampleIndex, value.subExamples)">
                                    <span class="fa fa-trash fa-2x"></span>
                                </a>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <div class="form-inline" style="margin-left: 10px; margin-top: 10px" v-if="isSubExample">
            <button class="btn btn-primary" type="submit">
                <span class="fa fa-sync fa-spin"  v-if="value.loading"></span>
                <span class="fa fa-save" v-else></span>
                {{ $t('save') }}
            </button>
        </div>
    </div>
</template>

<script>
import Multiselect from 'vue-multiselect';
import 'vue-multiselect/dist/vue-multiselect.min.css';
import {orderManagement} from '@/plugins/global';
import Sortable from "sortablejs";

export default {
    name: 'example-info',
    props: ['value', 'setSelectedExample', 'isSubExample', 'deleteExample', 'setDeleteExample', 'buildExample'],
    components: {
        Multiselect
    },
    mounted(){
        if(!this.isSubExample){
            const el = document.querySelector(`#dragtable${this._uid}`);
            const sortable = Sortable.create(el, {
                handle: '.handle',
                animation: 200,
                onEnd: evt =>{
                    if(evt.oldIndex !== evt.newIndex){
                        const changedOrders = orderManagement.moveTo(this.value.subExamples, evt.oldIndex, evt.newIndex);
                        this.$store.dispatch('updateExampleOrder', changedOrders).then(()=>{
                            orderManagement.sort(this.value.subExamples);
                        }).catch(()=>{
                            orderManagement.revertSort(this.value.subExamples);
                            const array = sortable.toArray();
                            orderManagement.move(array, evt.newIndex, evt.oldIndex);
                            sortable.sort(array);
                            
                            this.$bvToast.toast(this.$t('sortError'), {
                                title: this.$t('error'),
                                variant: 'danger',
                                appendToast: true
                            });
                        })
                    }
                }
            });
        }
    },
    created(){ //created is not triggered, when a subExample is being selected; because it is already created
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
        getFileTypes(){
            this.$store.dispatch('getFileTypes').then(response =>{
                this.fileTypes = response.data;
                this.setFileTypes();
            }).catch(()=>{
                this.$bvToast.toast(this.$t('fileTypes.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            })
        },
        setFileTypes(){
            let types = this.fileTypes.filter(fileType => this.value.supportedFileTypes.some(sfileType => sfileType.id === fileType.id));
            this.selectedfileTypes = types.concat(this.value.customFileTypes.map(type => {return {value: type, name: type}}));
        },
        submitFile_changed(){
            if(!this.value.submitFile){
                this.value.supportedFileTypes = [];
            }
        },
        newSubExample(){
            if(this.value.subExamples.length === 0){
                this.value.mandatory = this.value.submitFile = false;
                this.value.customFileTypes = [];
                this.value.supportedFileTypes = [];
                this.setFileTypes();
            }
            if(this.value.id){
                this.value.subExamples.push(this.buildExample(this.$t('subExample.name'), this.value.subExamples.length));
            }
            else{
                this.$bvToast.toast(this.$t('example.warning.createSubExample'), {
                    title: this.$t('warning'),
                    variant: 'warning',
                    appendToast: true
                });
            }
            
        },
        addCustomFileType(test){
            let type = test.split(' ')[0];
            if(type.startsWith('.')){
                type = `*${type}`;
            }
            else if(!type.startsWith('*.')){
                type = `*.${type}`;
            }
            this.value.customFileTypes.push(type);
            this.selectedfileTypes.push({name: type, value: type});
        },
        fileTypesChanged(fileTypes){
            this.value.supportedFileTypes = fileTypes.filter(type => type.id !== undefined).map(type => type.id);
            this.value.customFileTypes = fileTypes.filter(type => type.id === undefined).map(type => type.value);
        }
    }
}
</script>