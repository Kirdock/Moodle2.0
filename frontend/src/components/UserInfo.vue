<template>
    <div class="form-horizontal user_info">
        <div class="form-check" v-if="$store.getters.userInfo.isAdmin">
            <label :for="`uInfoIsAdmin${_uid}`"   class="form-check-label">
                <input :id="`uInfoIsAdmin${_uid}`" type="checkbox" class="form-check-input" v-model="value.isAdmin">
                {{$t('admin')}}
            </label>
        </div>
        <div class="form-group">
            <label :for="`uInfoUsername${_uid}`" class="control-label required">{{ $t('username') }}</label> 
            <input :id="`uInfoUsername${_uid}`" type="text" class="form-control" v-model="value.username" :disabled="isEdit" required>
        </div>
        <div class="form-group">
            <label :for="`uInfoMatriculationNumber${_uid}`" class="control-label required">{{ $t('matriculationNumber') }}</label>
            <input :id="`uInfoMatriculationNumber${_uid}`" type="text" class="form-control"  pattern="[0-9]{8}" v-model="value.matriculationNumber" :disabled="isEdit" :title="$t('eightDigitNumber')" required>
        </div>
        <div class="form-group">
            <label :for="`uInfoSurname${_uid}`" class="control-label required">{{ $t('surname') }}</label>
            <input :id="`uInfoSurname${_uid}`" type="text" class="form-control" v-model="value.surname" required>
        </div>
        <div class="form-group">
            <label :for="`uInfoForename${_uid}`" class="control-label required">{{ $t('forename') }}</label>
            <input :id="`uInfoForename${_uid}`" type="text" class="form-control" v-model="value.forename" required>
        </div>
        <div class="form-group">
            <label :for="`uInfoEmail${_uid}`" class="control-label required">{{ $t('email') }}</label>
            <input :id="`uInfoEmail${_uid}`" type="email" class="form-control" v-model="value.email" required>
        </div>
    </div>
</template>

<script>
export default {
    name: 'user-info',
    props: ['value', 'isEdit']
}
</script>