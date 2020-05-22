<template>
    <div>
        <div class="form-group">
            <label for="esInfoName" class="control-label required">{{ $t('name') }}</label>
            <input id="esInfoName" type="text" class="form-control"  v-model="value.name" required>
        </div>
        <div class="form-group">
            <label for="esInfoIssueDate" class="control-label required">{{ $t('issueDate') }}</label>
            <input id="esInfoIssueDate" type="datetime-local" class="form-control" v-model="value.issueDate" required>
        </div>
        <div class="form-group">
            <label for="esInfoSubmissionDate" class="control-label required">{{ $t('submissionDate') }}</label>
            <input id="esInfoSubmissionDate" type="datetime-local" class="form-control" :min="minDate" v-model="value.submissionDate" required>
        </div>
        <div class="form-group">
            <label for="esInfoMinKreuzel" class="control-label">{{ $t('minRequireKreuzel') }}</label>
            <div class="col-md-4" style="padding-left: 0px">
                <i-input id="esInfoMinKreuzel" class="form-control" v-model="value.minKreuzel"></i-input>
            </div>
        </div>
        <div class="form-group">
            <label for="esInfoMinPoints" class="control-label">{{ $t('minRequirePoints') }}</label>
            <div class="col-md-4" style="padding-left: 0px">
                <i-input id="esInfoMinPoints" class="form-control" v-model="value.minPoints"></i-input>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'es-info',
    props: ['value'],
    data(){
        return {
            minDate: this.$store.getters.currentDateTime
        }
    }
}
</script>