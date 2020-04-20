<template>
  <div class="courses">
    <h1>This is the courses page</h1>
  </div>
</template>

<script>
    import api from '../components/backend-api';

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
                api.fetchSemesters().then(response=>{
                    this.semesters = response.semesters;
                }).then(()=>{
                    this.fetchCourses();
                }).catch(()=>{
                    this.$bvToast.toast(`Semester konnten nicht geladen werden`, {
                        title: 'Fehler',
                        autoHideDelay: this.$store.toastDelay,
                        variant: 'danger',
                        appendToast: true
                    });
                })
            },
            fetchCourses(){
                api.fetchCourses().then(response =>{
                    this.courses = response.data;
                }).catch(() =>{
                    this.$bvToast.toast(`Kurse konnten nicht geladen werden`, {
                        title: 'Fehler',
                        autoHideDelay: this.$store.toastDelay,
                        variant: 'danger',
                        appendToast: true
                    });
                })
            }
        }

    }
</script>