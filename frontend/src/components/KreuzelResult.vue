<template>
    <div class="kreuzelResult">
        <div class="table_scroll">
            <table class="table nowrap table-hover" aria-describedby="modal-kreuzelResult" v-if="value && value.length > 0 && value[0].violations && value[0].violations.length > 0">
                <thead>
                    <th scope="col">{{$t('date')}}</th>
                    <th v-for="key in keys" :key="key" scope="col">{{key}}</th>
                </thead>
                <tbody>
                    <template v-for="row in value">
                        <tr :key="row.date">
                            <td>
                                {{new Date(row.date).toLocaleString()}}
                            </td>
                            <td v-for="key in keys" :key="key">
                                {{row.violations[0][key]}}
                            </td>
                        </tr>
                        <tr v-for="(violation, $index) in row.violations.slice(1)" :key="$index">
                            <td></td>
                            <td v-for="key in keys" :key="key">
                                {{violation[key] || ''}}
                            </td>
                        </tr>
                    </template>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script>
export default {
    name: 'kreuzel-result',
    props: ['value'],
    data(){
        return {
            keys: []
        }
    },
    created(){
        const keys = {};
        for(const row of this.value){ //create a dictionary with all keys
            for(const entry of row.violations){
                for(const key in entry){
                    keys[key] = undefined;
                }
            }
        }
        this.keys = Object.keys(keys);
    }
}
</script>