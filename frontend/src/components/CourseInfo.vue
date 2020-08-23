<template>
    <div class="course_info">
        <div class="form-group" v-if="$store.getters.userInfo.isAdmin">
            <label :for="`cInfoOwner${_uid}`" class="control-label required">{{ $t('owner') }}</label>
            <multiselect v-model="owner" :id="`cInfoOwner${_uid}`" open-direction="bottom" @input="ownerChanged"
                        :custom-label="userFormat"
                        :placeholder="$t('typeToSearch')"
                        track-by="matriculationNumber"
                        selectLabel=""
                        :selectedLabel="$t('selected')"
                        :options="users"
                        :searchable="true"
                        :clear-on-select="true"
                        :close-on-select="true"
                        :options-limit="300"
                        :allow-empty="false"
                        :max-height="600"
                        deselect-label=""
                        :show-no-results="false"
                        >
            </multiselect>
        </div>
        <div class="form-group">
            <label :for="`cInfoNumber${_uid}`" class="control-label required">{{ $t('number') }} ({{$t('format')}}: 123.456)</label>
            <input :id="`cInfoNumber${_uid}`" type="text" class="form-control" :disabled="!$store.getters.userInfo.isAdmin" pattern="[0-9]{3}\.[0-9]{3}" :title="`${$t('format')} : 123.456`" v-model="value.number" required>
        </div>
        <div class="form-group">
            <label :for="`cInfoName${_uid}`" class="control-label required">{{ $t('name') }}</label>
            <input :id="`cInfoName${_uid}`" type="text" class="form-control" v-model="value.name" :disabled="!$store.getters.userInfo.isAdmin" required>
        </div>
        <label :for="`cInfoMinKreuzel${_uid}`" class="control-label">{{$t('minRequireKreuzel')}}</label>
        <div class="col-md-4 input-group mb-3" style="padding-left: 0px">
            <i-input :id="`cInfoMinKreuzel${_uid}`" class="form-control" min="0" max="100" v-model="value.minKreuzel"> </i-input>
            <div class="input-group-append">
                <span class="input-group-text">%</span>
            </div>
        </div>
        
        <label :for="`cInfoMinPoints${_uid}`" class="control-label">{{$t('minRequirePoints')}}</label>    
        <div class="col-md-4 input-group mb-3" style="padding-left: 0px">
            <i-input :id="`cInfoMinPoints${_uid}`" class="form-control" min="0" max="100" v-model="value.minPoints"> </i-input>
            <div class="input-group-append">
                <span class="input-group-text">%</span>
            </div>
        </div>
        <div class="form-group">
            <label class="control-label" :for="`description${_uid}`">{{$t('description')}}</label>
            <editor :id="`description${_uid}`" v-model="value.description"></editor>
        </div>
    </div>
</template>
<script>
import Multiselect from 'vue-multiselect';
import 'vue-multiselect/dist/vue-multiselect.min.css';
import Editor from '@/components/Editor.vue';
export default {
    name: 'course-info',
    props: ['value', 'users'],
    components: {
        Multiselect,
        Editor
    },
    data(){
        return {
            owner: undefined
        }
    },
    created(){
        this.setOwner();
    },
    methods: {
        userFormat(user){
            return `${user.matriculationNumber} ${user.surname} ${user.forename}`;
        },
        ownerChanged(ownerUser){
            this.value.owner = ownerUser.matriculationNumber;
            const multiselect = document.getElementById(`cInfoOwner${this._uid}`);
            if(multiselect){
                multiselect.removeAttribute('required');
            }
        },
        setOwner(){
            this.owner = this.users.find(user => user.matriculationNumber == this.value.owner);
            if(!this.owner){
                this.$nextTick(()=>{
                    const multiselect = document.getElementById(`cInfoOwner${this._uid}`);
                    if(multiselect){
                        multiselect.setAttribute('required', true);
                    }
                })
            }
        }
    },
    watch:{
        'value.id': function(){
            this.setOwner();
        }
    }
}
</script>