<template>
    <div>
        <div class="form-group">
            <label :for="`esInfoName${_uid}`" class="control-label required">{{ $t('name') }}</label>
            <input :id="`esInfoName${_uid}`" type="text" class="form-control"  v-model="value.name" required>
        </div>
        <div class="form-group">
            <label :for="`esInfoIssueDate${_uid}`" class="control-label required">{{ $t('issueDate') }}</label>
            <input :id="`esInfoIssueDate${_uid}`" type="datetime-local" class="form-control" v-model="value.issueDate" required>
        </div>
        <div class="form-group">
            <label :for="`esInfoSubmissionDate${_uid}`" class="control-label required">{{ $t('submissionDate') }}</label>
            <input :id="`esInfoSubmissionDate${_uid}`" type="datetime-local" class="form-control" :min="minDate" v-model="value.submissionDate" required>
        </div>
        <div class="form-group">
            <label :for="`esInfoDescription${_uid}`" class="control-label">{{ $t('description') }}</label>
            <div class="document-editor__editable-container">
                <editor :id="`esInfoDescription${_uid}`" v-model="value.description"></editor>
            </div>
        </div>
        <div class="form-group">
            <label :for="`esInfoMinKreuzel${_uid}`" class="control-label">{{ $t('minRequireKreuzel') }}</label>
            <div class="col-md-4" style="padding-left: 0px">
                <i-input :id="`esInfoMinKreuzel${_uid}`" class="form-control" min="0" max="100" v-model="value.minKreuzel"></i-input>
            </div>
        </div>
        <div class="form-group">
            <label :for="`esInfoMinPoints${_uid}`" class="control-label">{{ $t('minRequirePoints') }}</label>
            <div class="col-md-4" style="padding-left: 0px">
                <i-input :id="`esInfoMinPoints${_uid}`"  class="form-control" min="0" max="100" v-model="value.minPoints"></i-input>
            </div>
        </div>
        <div class="form-group">
            <label :for="`esInfoKreuzelType${_uid}`" class="control-label">{{ $t('kreuzel.type') }}</label>
            <div :id="`esInfoKreuzelType${_uid}`" class="form-inline">
                <div class="form-check">
                    <input :id="`esInfoIncludeThirdYes${_uid}`" type="radio" :value="false" class="form-check-input"  v-model="value.includeThird">
                    <label :for="`esInfoIncludeThirdYes${_uid}`" class="form-check-label">
                        {{$t('kreuzel.option.1')}}
                    </label>
                </div>
                <div class="form-check" style="margin-left:15px">
                    <input :id="`esInfoIncludeThirdNo${_uid}`" type="radio" :value="true" class="form-check-input"  v-model="value.includeThird">
                    <label :for="`esInfoIncludeThirdNo${_uid}`" class="form-check-label">
                        {{$t('kreuzel.option.2')}}
                    </label>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import {dateManagement} from '@/plugins/global';
import Editor from '@/components/Editor.vue';

export default {
    name: 'es-info',
    props: ['value'],
    components:{
        Editor
    },
    data(){
        return {
            minDate: dateManagement.currentDateTime()
        }
    }
}
</script>