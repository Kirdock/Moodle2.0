<script>
    import Editor from '@/components/editor';
    import i18n from '@/plugins/i18n';

    export default {
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
            }
        }
    }
</script>