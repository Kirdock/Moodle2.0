<template>
    <input :value="value" type="number" @input="preventComma" @keydown="filterKey">
</template>

<script>
export default {
    name: 'i-input',
    props: ['value'],
    methods:{
        filterKey(e){ //onInput
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