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
            this.getSemesters();
        },
        methods: {
            getSemesters(){
                this.$store.dispatch("getSemesters").then(response=>{
                    this.semesters = response.data;
                    this.selectedSemester = this.semesters[this.semesters.length - 1].id; //this.$store.selectedSemester
                    this.getCourses(this.selectedSemester);
                }).catch(()=>{
                    this.$bvToast.toast(`Semester konnten nicht geladen werden`, {
                        title: 'Fehler',
                        variant: 'danger',
                        appendToast: true
                    });
                })
            },
            getCourses(id){
                this.$store.dispatch("getCourses", {id}).then(response=>{
                    this.courses = response.data;
                }).catch(() =>{
                    this.$bvToast.toast(`Kurse konnten nicht geladen werden`, {
                        title: 'Fehler',
                        variant: 'danger',
                        appendToast: true
                    });
                })
            }
        }

    }
</script>