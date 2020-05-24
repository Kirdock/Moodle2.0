<template>
    <div class="form-horizontal">
        <div class="form-check" v-if="$store.getters.userInfo.isAdmin">
            <label for="uInfoIsAdmin"   class="form-check-label">
                <input id="uInfoIsAdmin" type="checkbox" class="form-check-input" v-model="value.isAdmin">
                {{$t('admin')}}
            </label>
        </div>
        <div class="form-group">
            <label for="uInfoUsername" class="control-label required">{{ $t('username') }}</label> 
            <input id="uInfoUsername" type="text" class="form-control" v-model="value.username" :disabled="isEdit" required>
        </div>
        <div class="form-group">
            <label for="uInfoMatriculationNumber" class="control-label required">{{ $t('matriculationNumber') }}</label>
            <input id="uInfoMatriculationNumber" type="text" class="form-control"  pattern="[0-9]{8}" v-model="value.matriculationNumber" :disabled="isEdit" :title="$t('eightDigitNumber')" required>
        </div>
        <div class="form-group">
            <label for="uInfoSurname" class="control-label required">{{ $t('surname') }}</label>
            <input id="uInfoSurname" type="text" class="form-control" v-model="value.surname" required>
        </div>
        <div class="form-group">
            <label for="uInfoForename" class="control-label required">{{ $t('forename') }}</label>
            <input id="uInfoForename" type="text" class="form-control" v-model="value.forename" required>
        </div>
        <div class="form-group">
            <label for="uInfoEmail" class="control-label required">{{ $t('email') }}</label>
            <input id="uInfoEmail" type="email" class="form-control" v-model="value.email" required>
        </div>
    </div>
</template>

<script>
export default {
    name: 'user-info',
    props: ['value', 'isEdit']
}
</script>