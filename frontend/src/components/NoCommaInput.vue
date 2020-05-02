<template>
    <input :value="value" type="number" @input="preventComma" @keydown="filterKey">
</template>

<script>
export default {
    name: 'nc-input',
    props: ['value'], //https://vuejs.org/v2/guide/components.html#Using-v-model-on-Components
    methods:{
        filterKey(e){ //onInput
            //TO-DO: No-Comma-Input-Component
            if (e.key === ',' || e.key === '.'){
                e.preventDefault();
                this.update(e);
            }
        },
        preventComma(e){ //onPaste
            e.target.value = e.target.value.split(/\.|\,/)[0];
            this.update(e);
        },
        update(e){
            this.$emit('input', e.target.value)
        }
    }
}
</script>