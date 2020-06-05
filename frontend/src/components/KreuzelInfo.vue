<template>
    <td class="kreuzelInfo">
        <template v-if="value.subExamples.length === 0">
            <template v-if="includeThird">
                <div class="form-inline">
                    <div class="form-check">
                        <input :id="`kInfoYes${_uid}`" type="radio" value="y" class="form-check-input"  v-model="value.state" :disabled="deadlineReached">
                        <label :id="`kInfoYes${_uid}`" class="form-check-label">
                            {{$t('yes')}}
                        </label>
                    </div>
                    <div class="form-check" style="margin-left:15px">
                        <input :id="`kInfoNo${_uid}`" type="radio" value="n" class="form-check-input"  v-model="value.state" :disabled="deadlineReached">
                        <label :for="`kInfoNo${_uid}`" class="form-check-label">
                            {{$t('no')}}
                        </label>
                    </div>
                    <div class="form-check" style="margin-left:15px">
                        <input :id="`kInfoMaybe${_uid}`" type="radio" value="m" class="form-check-input"  v-model="value.state" :disabled="deadlineReached">
                        <label :for="`kInfoMaybe${_uid}`" class="form-check-label">
                            {{$t('maybe')}}
                        </label>
                    </div>
                    <div class="form-inline" v-show="value.state === 'm'" style="margin-left: 15px">
                        <label :for="`kInfoDescription${_uid}`" class="control-label">
                            {{$t('reason')}}:
                        </label>
                        <input :for="`kInfoDescription${_uid}`" type="text" class="form-control" v-model="value.submitDescription" :disabled="deadlineReached">
                    </div>
                </div>
            </template>
            <template v-else style="margin-right: 20px">
                <input :true-value="'y'" :false-value="'n'" type="checkbox" class="form-check-input" v-model="value.state" :disabled="deadlineReached">
            </template>
            <template v-if="value.submitFile">
                <label class="btn btn-primary">
                    <span class="fa fa-sync fa-spin" v-if="loadingFileUpload"></span>
                    <span class="fas fa-upload" v-else></span>
                    {{$t('submitFile')}}
                    <input type="file" class="d-none" :id="`file${_uid}`" :ref="`file${_uid}`" :accept="supportedTypes" @change="submitFile()" :disabled="deadlineReached"/>
                </label>
                <a href="#" @click.prevent="downloadFile(value.id)" :title="$t('download')" v-if="value.hasAttachement">
                    <span class="fa fa-download"></span>
                </a>
            </template>
        </template>
    </td>
</template>

<script>
import {fileManagement} from '@/plugins/global';
export default {
    name: 'kreuzel-info',
    props: ['value','includeThird', 'supportedFileTypes', 'deadlineReached', 'isDeadlineReached'],
    data(){
        return {
            loadingFileUpload: false
        }
    },
    created(){
        this.$set(this.value, 'state', this.value.state || 'n');
        const allTypes = this.supportedFileTypes.filter(fileType => this.value.supportedFileTypes.some(sfileType => sfileType.id === fileType.id)).map(filetype => filetype.value).concat(this.value.customFileTypes);
        this.supportedTypes = allTypes.join(',');
    },
    methods:{
        async submitFile(){
            if(this.isDeadlineReached()){
                this.$bvToast.toast(this.$t('deadlineReached'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
                this.deadlineReached = true;
            }
            else{
                this.loadingFileUpload = true;
                const formData = new FormData();
                formData.append('file',this.$refs[`file${this._uid}`].files[0]);
                formData.append('id', this.value.id);
                this.$refs[`file${this._uid}`].value = '';
                try{
                    const response = await this.$store.dispatch('addExampleAttachement', formData);
                    this.value.hasAttachement = true;
                    this.$bvToast.toast(this.$t('attachement.saved'), {
                        title: this.$t('success'),
                        variant: 'success',
                        appendToast: true
                    });
                }
                catch{
                    this.$bvToast.toast(this.$t('attachement.error.save'), {
                        title: this.$t('error'),
                        variant: 'danger',
                        appendToast: true
                    });
                }
                finally{
                    this.loadingFileUpload = false;
                }
            }
        },
        async downloadFile(id){
            try{
                const response = await this.$store.dispatch('getExampleAttachement', id);
                fileManagement.downloadFile(response.data, response.headers);
            }
            catch{
                this.$bvToast.toast(this.$t('attachement.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
        }
    }
}
</script>