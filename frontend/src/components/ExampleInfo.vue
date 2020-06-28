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
                    <editor :id="`eInfoDescription${_uid}`" v-model="value.description" :required="value.subExamples.length === 0" ></editor>
                </div>
                <div v-if="value.subExamples.length === 0">
                    <div class="form-group">
                        <label :for="`eInfoWeighting${_uid}`" class="control-label required">{{ $t('weighting') }}</label>
                        <div class="col-md-4" style="padding-left: 0px">
                            <i-input :id="`eInfoWeighting${_uid}`" min="1" class="form-control"  v-model="value.weighting" required> </i-input>
                        </div>
                    </div>
                    <div class="form-group">
                        <label :for="`eInfoPoints${_uid}`" class="control-label required">{{ $t('points') }}</label>
                        <div class="col-md-4" style="padding-left: 0px">
                            <i-input :id="`eInfoPoints${_uid}`" class="form-control" min="0"  v-model="value.points" required> </i-input>
                        </div>
                    </div>
                    <div class="form-check" style="margin-top: 20px">
                        <input :id="`eInfoSubmitFile${_uid}`" type="checkbox" class="form-check-input" v-model="value.submitFile" @change="submitFile_changed()">
                        <label :for="`eInfoSubmitFile${_uid}`" class="form-check-label" style="margin-right:15px">
                            {{$t('submitFile')}}
                        </label>
                    </div>
                    <template v-if="value.submitFile">
                        <div class="form-group">
                            <label class="control-label required" :for="`uploadCount${_uid}`">
                                {{$t('uploadLimit')}}
                            </label>
                            <div class="col-md-4" style="padding-left: 0px">
                                <i-input class="form-control" :id="`uploadCount${_uid}`" v-model="value.uploadCount" min="0" required></i-input>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label" :for="`validatorGroup${_uid}`">
                                {{$t('validator.name')}}: {{value.validator}}
                            </label>
                            <div class="form-inline" :id="`validatorGroup${_uid}`">
                                <label class="btn btn-primary">
                                    <span class="fa fa-sync fa-spin" v-if="loadingValidatorUpload"></span>
                                    <span class="fas fa-upload" v-else></span>
                                    {{$t('submitFile')}}
                                    <input type="file" class="d-none" :id="`validator${_uid}`" :ref="`validator${_uid}`" accept=".jar" @change="submitValidator()"/>
                                </label>
                                <template v-if="value.validator">
                                    <a href="#" @click.prevent="downloadValidator()" :title="$t('download')" style="margin-left: 10px">
                                        <span class="fa fa-download fa-2x"></span>
                                    </a>
                                    <a href="#" @click.prevent="deleteValidator()" :title="$t('delete')" style="margin-left: 10px">
                                        <span class="fa fa-trash fa-2x"></span>
                                    </a>
                                </template>
                            </div>
                        </div>
                        <div class="form-group" required>
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
                    </template>
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
                <table class="table table-hover" :aria-describedby="`eInfoHeader${_uid}`">
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
                                <a href="#" :title="$t('edit')" @click.prevent="setExample(subExample)">
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
import {orderManagement, fileManagement} from '@/plugins/global';
import Sortable from "sortablejs";
import Editor from '@/components/Editor.vue';

export default {
    name: 'example-info',
    props: ['value', 'setSelectedExample', 'isSubExample', 'deleteExample', 'setDeleteExample', 'buildExample', 'uploadCount'],
    components: {
        Multiselect,
        Editor
    },
    mounted(){
        if(!this.isSubExample){
            const el = document.querySelector(`#dragtable${this._uid}`);
            const sortable = Sortable.create(el, {
                handle: '.handle',
                animation: 200,
                onEnd: async evt =>{
                    if(evt.oldIndex !== evt.newIndex){
                        const changedOrders = orderManagement.moveTo(this.value.subExamples, evt.oldIndex, evt.newIndex);
                        try{
                            await this.$store.dispatch('updateExampleOrder', changedOrders);
                            orderManagement.sort(this.value.subExamples);
                        }
                        catch{
                            orderManagement.revertSort(this.value.subExamples);
                            const array = sortable.toArray();
                            orderManagement.move(array, evt.newIndex, evt.oldIndex);
                            sortable.sort(array);
                            
                            this.$bvToast.toast(this.$t('sortError'), {
                                title: this.$t('error'),
                                variant: 'danger',
                                appendToast: true
                            });
                        }
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
            fileTypes: [],
            loadingValidatorUpload: false
        }
    },
    methods:{
        setExample(example){
            this.setSelectedExample(example);
            this.$nextTick(()=>{ //else selectedExample isn't changed in time and file types of selectedExample before are being used
                this.setFileTypes();
            });
            
        },
        async getFileTypes(){
            try{
                const response = await this.$store.dispatch('getFileTypes');
                this.fileTypes = response.data;
                this.setFileTypes();
            }
            catch{
                this.$bvToast.toast(this.$t('fileTypes.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
        },
        setFileTypes(){
            let types = this.fileTypes.filter(fileType => this.value.supportedFileTypes.some(sfileType => sfileType === fileType.id));
            this.selectedfileTypes = types.concat(this.value.customFileTypes.map(type => {return {value: type, name: type}}));
        },
        submitFile_changed(){
            if(!this.value.submitFile){
                this.value.supportedFileTypes = [];
                this.value.uploadCount = undefined;
            }
            else{
                this.$set(this.value, 'uploadCount', this.uploadCount);
                this.$nextTick(()=>{
                    document.getElementById(`eInfoSupportedFileTypes${this._uid}`).setAttribute('required', true);
                })
            }
        },
        async newSubExample(){
            if(this.value.subExamples.length === 0){
                this.value.mandatory = this.value.submitFile = false;
                this.value.customFileTypes = [];
                this.value.supportedFileTypes = [];
                this.setFileTypes();
            }
            const example = this.buildExample(this.$t('subExample.name'), this.value.subExamples.length);
            try{
                const response = await this.$store.dispatch('createExample', example);
                example.id = response.data.id;
                this.value.subExamples.push(example);
            }
            catch{
                this.$bvToast.toast(this.$t('example.error.create'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
            
        },
        async submitValidator(){
            this.loadingValidatorUpload = true;
            const file = this.$refs[`validator${this._uid}`].files[0];
            if(file.name.toLowerCase().endsWith('.jar')){
                const formData = new FormData();
                formData.append('file',file);
                formData.append('id', this.value.id);
                this.$refs[`validator${this._uid}`].value = '';
                try{
                    await this.$store.dispatch('addExampleValidator', formData);
                    this.value.validator = file.name;
                    this.$bvToast.toast(this.$t('validator.saved'), {
                        title: this.$t('success'),
                        variant: 'success',
                        appendToast: true
                    });
                }
                catch{
                    this.$bvToast.toast(this.$t('validator.error.save'), {
                        title: this.$t('error'),
                        variant: 'danger',
                        appendToast: true
                    });
                }
                finally{
                    this.loadingValidatorUpload = false;
                }
            }
        },
        async deleteValidator(){
            try{
                await this.$store.dispatch('deleteExampleValidator', this.value.id);
                this.$bvToast.toast(this.$t('validator.deleted'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }
            catch{
                this.$bvToast.toast(this.$t('validator.error.delete'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
        },
        async downloadValidator(){
            try{
                const response = await this.$store.dispatch('getExampleValidator', this.value.id);
                fileManagement.download(response);
            }
            catch{
                this.$bvToast.toast(this.$t('validator.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
        },
        addCustomFileType(test){
            let type = test.split(' ')[0];
            while(type.startsWith('*')){
                type = type.substring(1);
            }
            if(!type.startsWith('.')){
                type = `.${type}`;
            }
            this.value.customFileTypes.push(type);
            this.selectedfileTypes.push({name: type, value: type});
        },
        fileTypesChanged(fileTypes){
            this.value.supportedFileTypes = fileTypes.filter(type => type.id !== undefined).map(type => type.id);
            this.value.customFileTypes = fileTypes.filter(type => type.id === undefined).map(type => type.value);
            if(fileTypes.length === 0){
                document.getElementById(`eInfoSupportedFileTypes${this._uid}`).setAttribute('required', true);
            }
            else{
                document.getElementById(`eInfoSupportedFileTypes${this._uid}`).removeAttribute('required');
            }
            
        }
    }
}
</script>