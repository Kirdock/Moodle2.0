<template>
    <tr class="kreuzelInfo">
        <td>
            <template v-if="isParent">
                {{value.name}}
            </template>
        </td>
        <td v-if="hasSubExamples">
            <template v-if="!isParent">
                {{value.name}}
            </template>
        </td>
        <template v-if="value.subExamples.length === 0">
            <td>
                {{value.mandatory ? $t('yes') : $t('no')}}
            </td>
            <td>
                {{value.weighting}}
            </td>
            <td>
                {{value.points}}
            </td>
        </template>
        <template v-else>
            <td></td>
            <td></td>
            <td></td>
        </template>
        <td>
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
                            <input :id="`kInfoNotSure${_uid}`" type="radio" value="m" class="form-check-input"  v-model="value.state" :disabled="deadlineReached">
                            <label :for="`kInfoNotSure${_uid}`" class="form-check-label help" v-b-tooltip.hover :title="$t('notSureTooltip')">
                                {{$t('notSure')}}
                            </label>
                        </div>
                        <div class="form-inline" v-if="value.state === 'm'" style="margin-left: 15px">
                            <label :for="`kInfoDescription${_uid}`" class="control-label required">
                                {{$t('reason')}}:
                            </label>
                            <textarea :for="`kInfoDescription${_uid}`" class="form-control" v-model="value.submitDescription" :disabled="deadlineReached" required maxlength="200"> </textarea>
                        </div>
                    </div>
                </template>
                <template v-else style="margin-right: 20px">
                    <input :true-value="'y'" :false-value="'n'" type="checkbox" class="form-check-input" v-model="value.state" :disabled="deadlineReached">
                </template>
            </template>
        </td>
        <td v-if="hasFileUpload">
            <template v-if="value.submitFile">
                {{value.remainingUploadCount}}/{{value.uploadCount}}
            </template>
        </td>
        <td>
            <template v-if="value.submitFile">
                <label class="btn btn-primary">
                    <span class="fa fa-sync fa-spin" v-if="loading.fileUpload"></span>
                    <span class="fas fa-upload" v-else></span>
                    {{$t('submitFile')}}
                    <input type="file" class="d-none" :id="`file${_uid}`" :ref="`file${_uid}`" :accept="supportedTypes" @change="submitFile()" :disabled="deadlineReached"/>
                </label>
                <a href.prevent="#" style="color: red; font-size: 25px" v-b-tooltip.hover :title="$t('noFileUploaded')">
                    <span class="fas fa-exclamation-circle" v-show="value.state !== 'n' && !value.hasAttachment"></span>
                </a>
                <a href="#" @click.prevent="downloadFile(value.id)" :title="$t('download')" v-if="value.hasAttachment">
                    <span class="fa fa-sync fa-spin fa-2x" v-if="loading.fileDownload"></span>
                    <span class="fa fa-download fa-2x" v-else></span>
                </a>
                <a href="#" @click.prevent="setSelectedKreuzelResult(value.result)" :title="$t('result')" v-if="value.result.length !== 0">
                    <span class="fa fa-list fa-2x"></span>
                </a>
            </template>
        </td>
    </tr>
</template>

<script>
import {fileManagement} from '@/plugins/global';
import KreuzelResult from '@/components/KreuzelResult.vue';
export default {
    name: 'kreuzel-info',
    components:{
        KreuzelResult
    },
    props: ['value','includeThird', 'supportedFileTypes', 'deadlineReached', 'isDeadlineReached', 'isParent', 'hasSubExamples', 'hasFileUpload', 'setSelectedKreuzelResult'],
    data(){
        return {
            loading: {
                fileUpload: false,
                fileDownload: false
            }
        }
    },
    created(){
        const allTypes = this.supportedFileTypes.filter(fileType => this.value.supportedFileTypes.some(sfileType => sfileType === fileType.id)).map(filetype => filetype.value).concat(this.value.customFileTypes);
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
            else if(this.value.submitFile && this.value.uploadCount !== 0 && this.value.remainingUploadCount === 0){
                this.$bvToast.toast(this.$t('maxAttemptsReached'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
            else{
                this.loading.fileUpload = true;
                const formData = new FormData();
                formData.append('file',this.$refs[`file${this._uid}`].files[0]);
                formData.append('id', this.value.id);
                this.$refs[`file${this._uid}`].value = '';
                try{
                    await this.$store.dispatch('addExampleAttachment', formData);
                    if(this.value.remainingUploadCount > 0){
                        this.value.remainingUploadCount--;
                    }
                    this.value.hasAttachment = true;
                    this.$bvToast.toast(this.$t('attachment.saved'), {
                        title: this.$t('success'),
                        variant: 'success',
                        appendToast: true
                    });
                }
                catch{
                    this.$bvToast.toast(this.$t('attachment.error.save'), {
                        title: this.$t('error'),
                        variant: 'danger',
                        appendToast: true
                    });
                }
                finally{
                    this.loading.fileUpload = false;
                }
            }
        },
        async downloadFile(id){
            this.loading.fileDownload = true;
            try{
                const response = await this.$store.dispatch('getExampleAttachment', id);
                fileManagement.download(response);
            }
            catch{
                this.$bvToast.toast(this.$t('attachment.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
            finally{
                this.loading.fileDownload = false;
            }
        }
    }
}
</script>