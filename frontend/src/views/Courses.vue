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
            <h1>{{$t('courses.name')}}</h1>
        </div>
        <div id="courseList">
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
            async getSemesters(){
                try{
                    const response = await this.$store.dispatch("getSemestersAssigned");
                    this.semesters = response.data;
                    if(this.semesters.length > 0){
                        let id = this.semesters[this.semesters.length - 1].id;
                        if(this.$store.state.settings.selectedSemester !== undefined){
                            const semester = this.semesters.find(semester => semester.id === this.$store.state.settings.selectedSemester);
                            if(semester !== undefined){
                                id = this.$store.state.settings.selectedSemester;
                            }
                            else{
                                this.$store.commit('updateSettings', {selectedSemester: id});
                            }
                        }
                        else{
                            this.$store.commit('updateSettings', {selectedSemester: id});
                        }
                        this.selectedSemester =  id;
                        this.getCourses(this.selectedSemester);
                    }
                }
                catch{
                    this.$bvToast.toast(this.$t('semester.error.get'), {
                        title: 'Fehler',
                        variant: 'danger',
                        appendToast: true
                    });
                }
            },
            async getCourses(id){
                try{
                    this.$store.commit('updateSettings', {selectedSemester: id});
                    const response = await this.$store.dispatch("getCoursesAssigned", {id});
                    this.courses = response.data;
                }
                catch{
                    this.$bvToast.toast(this.$t('courses.error.get'), {
                        title: 'Fehler',
                        variant: 'danger',
                        appendToast: true
                    });
                }
            }
        }

    }
</script>