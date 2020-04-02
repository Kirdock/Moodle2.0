<template>
  <div class="courses">
    <h1>This is the courses page</h1>
  </div>
</template>

<script>
import dataservice from '../../services/dataservice';

export default {
    data() {
        return {
          courses: [],
          semesters: []
        };
    },
    created() {
        this.fetchSemesters();
        this.fetchCourses();
    },
    methods: {
        fetchSemesters(){
            dataservice.fetchSemesters().then(response=>{
                semesters = response.semesters;
            }).then(()=>{
                this.fetchCourses();
            }).catch(()=>{
                this.$bvToast.toast(`Semester konnten nicht geladen werden`, {
                    title: 'Fehler',
                    autoHideDelay: this.$config.toastDelay,
                    variant: 'danger',
                    appendToast: true
                });
            })
        },
        fetchCourses(){
            dataservice.fetchCourses().then(response =>{
                courses = response.data;
            }).catch(() =>{
                this.$bvToast.toast(`Kurse konnten nicht geladen werden`, {
                    title: 'Fehler',
                    autoHideDelay: this.$config.toastDelay,
                    variant: 'danger',
                    appendToast: true
                });
            })
        }
    }

}