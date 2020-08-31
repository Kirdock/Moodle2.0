<template>
    <div>
        <textarea :id="`summernote_${_uid}`" v-model="value">
        </textarea>
    </div>
</template>

<script>
    import i18n from '@/plugins/i18n';
    import 'summernote';
    import 'summernote/dist/summernote.css';
    import 'summernote/lang/summernote-de-DE.js';

    export default {
        name: 'editor',
        props: ['value', 'required'],
        data(){
            return {
                triggerByForceUpdate: false
            }
        },
        computed:{
            locale(){
                return i18n.locale;
            },
            options(){
                const vm = this;
                return {
                    lang: vm.$t('lang'),
                    callbacks: {
                        onChange: vm.update
                    }
                }
            }
        },
        mounted(){
            const vm = this;
            $(document).ready(()=> {
                $(`#summernote_${vm._uid}`).summernote(vm.options);
            });
        },
        methods:{
            update(value){
                if(!this.triggerByForceUpdate){
                    this.$emit('input', value);
                }
                this.triggerByForceUpdate = false;
            },
            forceUpdate(value){ //called from parent, if v-model changes
                this.triggerByForceUpdate = true;
                $(`#summernote_${this._uid}`).summernote(this.options); //has to be called in order to prevent the bug that update callback is broken
                $(`#summernote_${this._uid}`).summernote('code', value);
            }
        },
        watch:{
            'options.lang': function(newVal, oldVal){
                $(`#summernote_${this._uid}`).summernote('destroy');
                $(`#summernote_${this._uid}`).summernote(this.options);
            }
        }
    }
</script>

<style>
.note-editor.note-frame.panel.panel-default.fullscreen {
    background-color: #F5F7F7 !important;
}
</style>