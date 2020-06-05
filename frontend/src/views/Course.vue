<template>
    <div class="course">
        <h1 id="exerciseSheets">{{$t('exerciseSheets.name')}}</h1>
        <table class="table" aria-describedby="exerciseSheets" v-if="courseInfo.id">
            <thead>
                <th scope="col">{{$t('name')}}</th>
                <th scope="col">{{$t('submissionDate')}}</th>
                <th scope="col">{{$t('examples')}}</th>
                <th scope="col">{{$t('points')}}</th>
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
                        {{exerciseSheet.kreuzel}}/{{exerciseSheet.exampleCount}}
                    </td>
                    <td>
                        {{exerciseSheet.points}}/{{exerciseSheet.pointsTotal}}
                    </td>
                    <td>
                        <router-link :title="$t('edit')" :to="{
                                            name: 'ExerciseSheet',
                                            params: {
                                                courseId: id,
                                                exerciseSheetId: exerciseSheet.id
                                            }
                                        }">
                            <span class="fas fa-edit fa-2x"></span>
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
                const response = await this.$store.dispatch('getCourseAssigned', {courseId});
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