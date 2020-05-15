<template>
    <div>
        <div class="form-group">
            <label for="name" class="control-label required">{{ $t('name') }}</label>
            <input id="name" type="text" class="form-control"  v-model="value.name" required>
        </div>
        <div class="form-group">
            <label for="submissionDate" class="control-label required">{{ $t('submissionDate') }}</label>
            <input id="submissionDate" type="datetime-local" class="form-control" :min="minDate" v-model="value.submissionDate" required>
        </div>
        <div class="form-group">
            <label for="minKreuzel" class="control-label">{{ $t('minRequireKreuzel') }}</label>
            <div class="col-md-4" style="padding-left: 0px">
                <i-input id="minKreuzel" class="form-control" v-model="value.minKreuzel"></i-input>
            </div>
        </div>
        <div class="form-group">
            <label for="minPoints" class="control-label">{{ $t('minRequirePoints') }}</label>
            <div class="col-md-4" style="padding-left: 0px">
                <i-input id="minPoints" class="form-control" v-model="value.minPoints"></i-input>
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