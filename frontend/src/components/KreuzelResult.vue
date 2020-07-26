<template>
    <div class="kreuzelResult">
        <div class="table_scroll">
            <table class="table nowrap table-hover" aria-describedby="modal-kreuzelResult" v-if="value && value.length > 0 && value[0].violations && value[0].violations.length > 0">
                <thead>
                    <th scope="col">{{$t('date')}}</th>
                    <th v-for="key in Object.keys(value[0].violations[0])" :key="key" scope="col">{{key}}</th>
                </thead>
                <tbody>
                    <template v-for="row in value">
                        <tr :key="row.date">
                            <td>
                                {{new Date(row.date).toLocaleString()}}
                            </td>
                        </tr>
                        <tr v-for="violation in row.violations" :key="violation">
                            <td></td>
                            <td v-for="key in Object.keys(violation)" :key="key">
                                {{row.violations[key]}}
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
    props: ['value']
}
</script>