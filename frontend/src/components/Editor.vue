<template>
    <div class="document-editor__editable-container">
        <ckeditor :id="`description${_uid}`" :value="value" :editor="editor" @ready="onReady" :config="editorConfig" @input="update" :required="required"></ckeditor>
    </div>
</template>

<script>
    import Editor from '@/components/editor';
    import i18n from '@/plugins/i18n';

    export default {
        name: 'editor',
        props: ['value', 'required'],
        data(){
            return {
                editor: Editor,
                editorConfig: {
                    language: i18n.locale
                }
            }
        },
        methods:{
            onReady(editor) {
                editor.ui.getEditableElement().parentElement.insertBefore(
                    editor.ui.view.toolbar.element,
                    editor.ui.getEditableElement()
                );
            },
            update(value){
                this.$emit('input', value);
            }
        }
    }
</script>