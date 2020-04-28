<template>
  <div class="courses">
    <h1>This is the courses page</h1>
  </div>
</template>

<script>

    export default {
        data() {
            return {
            courses: [],
            semesters: [],
            selectedSemester: undefined
            };
        },
        created() {
            this.fetchSemesters();
            this.fetchCourses();
        },
        methods: {
            fetchSemesters(){
                this.$store.dispatch("getSemesters").then(response=>{
                    this.semesters = response.semesters;
                    this.selectedSemester = this.semesters[this.semesters.length - 1]; //this.$store.selectedSemester
                }).then(()=>{
                    this.fetchCourses();
                }).catch(()=>{
                    this.$bvToast.toast(`Semester konnten nicht geladen werden`, {
                        title: 'Fehler',
                        autoHideDelay: this.$store.getters.toastDelay,
                        variant: 'danger',
                        appendToast: true
                    });
                })
            },
            fetchCourses(){
                this.$store.dispatch("getCourses").then(response=>{
                    this.courses = response.data;
                }).catch(() =>{
                    this.$bvToast.toast(`Kurse konnten nicht geladen werden`, {
                        title: 'Fehler',
                        autoHideDelay: this.$store.getters.toastDelay,
                        variant: 'danger',
                        appendToast: true
                    });
                })
            }
        }

    }
</script>