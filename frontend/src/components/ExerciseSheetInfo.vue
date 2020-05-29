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
            <textarea :id="`esInfoDescription${_uid}`" class="form-control" v-model="value.description"></textarea>
        </div>
        <div class="form-group">
            <label :for="`esInfoMinKreuzel${_uid}`" class="control-label">{{ $t('minRequireKreuzel') }}</label>
            <div class="col-md-4" style="padding-left: 0px">
                <i-input :id="`esInfoMinKreuzel${_uid}`" class="form-control" v-model="value.minKreuzel"></i-input>
            </div>
        </div>
        <div class="form-group">
            <label :for="`esInfoMinPoints${_uid}`" class="control-label">{{ $t('minRequirePoints') }}</label>
            <div class="col-md-4" style="padding-left: 0px">
                <i-input :id="`esInfoMinPoints${_uid}`" class="form-control" v-model="value.minPoints"></i-input>
            </div>
        </div>
    </div>
</template>

<script>
import {dateManagement} from '@/plugins/global';
export default {
    name: 'es-info',
    props: ['value'],
    data(){
        return {
            minDate: dateManagement.currentDateTime()
        }
    }
}
</script>