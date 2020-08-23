<template>
    <div class="userCreationError">
        <div class="table_scroll">
            <table class="table nowrap table-hover" aria-describedby="modal-user-creation-error">
                <thead>
                    <th scope="col">{{$t('row')}}</th>
                    <th scope="col">{{$t('error')}}</th>
                </thead>
                <tbody>
                    <tr v-for="user in value" :key="user.lineNumber">
                        <td>
                            {{user.lineNumber}}
                        </td>
                        <td>
                            {{userMessage(user.statusCode)}}
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script>
import {userManagement} from '@/plugins/global';
export default {
    name: 'user-creation-error',
    props: ['value'],
    methods: {
        userMessage: userManagement.modifyMessage
    }
}
</script>