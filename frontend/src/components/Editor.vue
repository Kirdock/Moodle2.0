<template>
    <div>
        <div :id="`summernote_${_uid}`">
        </div>
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
                vm.$nextTick(()=>{
                    setTimeout(()=>{
                        $(`#summernote_${vm._uid}`).summernote('code', vm.value || '');
                    },500)
                });
            });
        },
        methods:{
            update(value){
                this.$emit('input', value);
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