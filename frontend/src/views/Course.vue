<template>
    <div class="course">
        <h1>{{$t('course.name')}}: {{courseInfo.number}} {{courseInfo.name}}</h1>
        <ol class="breadcrumb">
            <li class="breadcrumb-item">
                <router-link :to="{name:'Courses'}" >{{ $t('courses.name') }}</router-link>
            </li>
            <li class="breadcrumb-item active">{{courseInfo.number}} {{courseInfo.name}}</li>
        </ol>
        <h2>{{$t('description')}}</h2>
        <div v-html="courseInfo.description"> </div>
        <h2>{{$t('requirements')}}</h2>
        <div class="form-group">
            <label class="control-label">{{$t('minKreuzel')}}: <strong>{{courseInfo.minKreuzel || 0}}%</strong></label>
        </div>
        <div class="form-group">
            <label class="control-label">{{$t('minPoints')}}: <strong>{{courseInfo.minPoints || 0}}%</strong></label>
        </div>
        <h2 id="exerciseSheets">{{$t('exerciseSheets.name')}}</h2>
        <table class="table" aria-describedby="exerciseSheets" v-if="courseInfo.id">
            <thead>
                <th scope="col">{{$t('name')}}</th>
                <th scope="col">{{$t('submissionDate')}}</th>
                <th scope="col">{{$t('examples')}}</th>
                <th scope="col">{{$t('minRequireKreuzel')}}</th>
                <th scope="col">{{$t('points')}}</th>
                <th scope="col">{{$t('minRequirePoints')}}</th>
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
                    <td :style="minimumRequired(exerciseSheet.minKreuzel, exerciseSheet.kreuzel, exerciseSheet.exampleCount)">
                        {{exerciseSheet.kreuzel}}/{{exerciseSheet.exampleCount}}
                    </td>
                    <td>
                        {{exerciseSheet.minKreuzel || 0}}
                    </td>
                    <td :style="minimumRequired(exerciseSheet.minPoints, exerciseSheet.points, exerciseSheet.pointsTotal)">
                        {{exerciseSheet.points}}/{{exerciseSheet.pointsTotal}}
                    </td>
                    <td>
                        {{exerciseSheet.minPoints || 0}}
                    </td>
                    <td>
                        <a href="#" :title="$t('download')" @click.prevent="getExerciseSheetPdf(exerciseSheet.id)">
                            <span class="fa fa-eye fa-2x"></span>
                        </a>
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
                <tr style="font-weight: bold">
                    <td>{{$t('total')}}</td>
                    <td></td>
                    <td :style="minimumRequired(courseInfo.minKreuzel, exampleCount, exampleCountTotal)">
                        {{exampleCount}}/{{exampleCountTotal}}
                    </td>
                    <td>
                        {{courseInfo.minKreuzel}}
                    </td>
                    <td :style="minimumRequired(courseInfo.minPoints, points, pointsTotal)">
                        {{points}}/{{pointsTotal}}
                    </td>
                    <td>
                        {{courseInfo.minPoints}}
                    </td>
                    <td></td>
                </tr>
            </tbody>
        </table>
    </div>
</template>

<script>
import { calcManagement, fileManagement } from "@/plugins/global";
export default {
    props: ['id'],
    data(){
        return {
            courseInfo: {}
        }
    },
    computed:{
        exampleCount(){
            return this.sumByKey(this.courseInfo.exerciseSheets, 'kreuzel');
        },
        exampleCountTotal(){
            return this.sumByKey(this.courseInfo.exerciseSheets, 'exampleCount');
        },
        points(){
            return this.sumByKey(this.courseInfo.exerciseSheets, 'points');
        },
        pointsTotal(){
            return this.sumByKey(this.courseInfo.exerciseSheets, 'pointsTotal');
        }
    },
    created(){
        this.getCourse(this.id);
    },
    methods: {
        minimumRequired: calcManagement.minimumRequired,
        sumByKey(array, key){
            let total = 0;
            for(let sheet of array){
                total += (sheet[key] || 0);
            }
            return total;
        },
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
        },
        async getExerciseSheetPdf(id){
            try{
                const response = await this.$store.dispatch('getExerciseSheetPdf', id);
                fileManagement.download(response.data, response.headers);
            }
            catch{
                this.$bvToast.toast(this.$t('exerciseSheet.error.get'), {
                    title: 'Fehler',
                    variant: 'danger',
                    appendToast: true
                });
            }
        }
    }
}
</script>