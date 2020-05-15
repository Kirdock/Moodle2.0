<template>
    <div>
        <div class="form-group">
            <label for="number" class="control-label required">{{ $t('number') }} ({{$t('format')}} : 123.456)</label>
            <input id="number" type="text" class="form-control"  pattern="[0-9]{3}\.[0-9]{3}" :title="$t('format') +': 123.456'" v-model="value.number" required>
        </div>
        <div class="form-group">
            <label for="name" class="control-label required">{{ $t('name') }}</label>
            <input id="name" type="text" class="form-control" v-model="value.name" required>
        </div>
        <div class="form-group">
            <label for="minKreuzel" class="control-label">{{$t('minRequireKreuzel')}}</label>
            <div class="col-md-6" style="padding-left: 0px">
                <i-input id="minKreuzel" class="form-control" min="0" max="100" v-model="value.minKreuzel"> </i-input>
            </div>
        </div>
        <div class="form-group" >
            <label for="minPoints_edit" class="control-label">{{$t('minRequirePoints')}}</label>
            <div class="col-md-6" style="padding-left: 0px">
                <i-input id="minPoints_edit" class="form-control" min="0" max="100" v-model="value.minPoints"> </i-input>
            </div>
        </div>
    </div>
</template>
<script>
export default {
    name: 'course-info',
    props: ['value']
}
</script>