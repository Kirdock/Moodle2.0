<template>
  <div class="courses">
    <div class="form-horizontal">
        <div class="form-group col-md-4">
            <label for="semester" class="control-label">{{ $t('semester.name') }}</label>
            <select class="form-control" v-model="selectedSemester" id="semester" @change="getCourses(selectedSemester)">
                <option v-for="semester in semesters" :value="semester.id" :key="semester.id">
                    {{semester.year}} {{semester.type === 'w' ? $t('semester.winterShortcut') : $t('semester.summerShortcut')}}
                </option>
            </select>
        </div>
        <div class="form-group col-md-12">
            <h1 id="courses">{{$t('courses.name')}}</h1>
        </div>
        <div class="form-group col-md-6" v-for="course in courses" :key="course.id">
            <router-link :to="{
                                name: 'Course',
                                params: {
                                    id: course.id,
                                }
                            }">
                {{ `${course.number} ${course.name}` }}
            </router-link>
        </div>
    </div>
  </div>
</template>

<script>

    export default {
        data() {
            return {
                courses: [],
                semesters: [],
                selectedSemester: undefined,

            };
        },
        created() {
            this.getSemesters();
        },
        methods: {
            getSemesters(){
                this.$store.dispatch("getSemestersAssigned").then(response=>{
                    this.semesters = response.data;
                    if(this.semesters.length > 0){
                        this.selectedSemester = this.semesters[this.semesters.length - 1].id; //this.$store.selectedSemester
                        this.getCourses(this.selectedSemester);
                    }
                }).catch(()=>{
                    this.$bvToast.toast(this.$t('semester.error.get'), {
                        title: 'Fehler',
                        variant: 'danger',
                        appendToast: true
                    });
                })
            },
            getCourses(id){
                this.$store.dispatch("getCoursesAssigned", {id}).then(response=>{
                    this.courses = response.data;
                }).catch(() =>{
                    this.$bvToast.toast(this.$t('courses.error.get'), {
                        title: 'Fehler',
                        variant: 'danger',
                        appendToast: true
                    });
                })
            }
        }

    }
</script>