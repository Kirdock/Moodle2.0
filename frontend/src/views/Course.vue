<template>
    <div class="course">
        <h1 id="exerciseSheets">{{$t('exerciseSheets.name')}}</h1>
        <table class="table" aria-describedby="exerciseSheets" v-if="courseInfo.id">
            <thead>
                <th scope="col"></th>
                <th scope="col">{{$t('name')}}</th>
                <th scope="col">{{$t('submissionDate')}}</th>
                <th scope="col">{{$t('actions')}}</th>
            </thead>
            <tbody>
                <tr v-for="exerciseSheet in this.courseInfo.exerciseSheets" :key="exerciseSheet.id">
                    <td>
                        {{exerciseSheet.name}}
                    </td>
                    <td>
                        {{new Date(exerciseSheet.submissionDate).toLocaleString()}}
                    </td>
                    <td>
                        <router-link :title="t('view')" :to="{
                                            name: 'ExerciseSheets',
                                            params: {
                                                courseId: id,
                                                exerciseSheetId: exerciseSheet.id
                                            }
                                        }">
                            <span class="fas fa-eye"></span>
                        </router-link>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</template>

<script>
export default {
    props: ['id'],
    data(){
        return {
            courseInfo: {}
        }
    },
    created(){
        this.getCourse(this.id);
    },
    methods: {
        async getCourse(courseId){
            try{
                const response = await this.$store.dispatch('getCourse', {courseId});
                this.courseInfo = response.data;
            }
            catch{
                this.$bvToast.toast(this.$t('course.error.get'), {
                        title: 'Fehler',
                        variant: 'danger',
                        appendToast: true
                });
            }
        }
    }
}
</script>