<template>
    <div class="courseManagement">
        <h1>{{$t('course.management')}}</h1>
        <ol class="breadcrumb">
            <li class="breadcrumb-item">
                <router-link to="/">{{ $t('home') }}</router-link>
            </li>
            <li class="breadcrumb-item">
                <router-link to="/Admin" >{{ $t('admin') }}</router-link>
            </li>
            <li class="breadcrumb-item active">{{$t('course.management')}}</li>
        </ol>
        <div class="row col-md-12">
            <div class="col-md-4">
                <div class="form-group">
                    <label for="courseSemester_edit" class="control-label">{{ $t('semester.semester') }}</label>
                    <select class="form-control" v-model="selectedSemester_edit" id="courseSemester_edit" @change="getCourses(selectedSemester_edit)">
                        <option v-for="semester in semesters" :value="semester.id" :key="semester.id">
                            {{semester.year}} {{semester.type === 'w' ? $t('semester.winterShortcut') : $t('semester.summerShortcut')}}
                        </option>
                    </select>
                </div>
            </div>
            <div class="col-md-4">
                <div class="form-group">
                    <label for="selectedCourse" class="control-label">{{ $t('course.course') }}</label>
                    <select class="form-control" v-model="selectedCourseId" id="selectedCourse" @change="getCourseUsers(selectedCourseId); getCourse(selectedCourseId)">
                        <option v-for="course in courses" :value="course.id" :key="course.id">
                            {{course.number}} {{course.name}}
                        </option>
                    </select>
                </div>
            </div>
            <div class="col-md-4" style="margin-top: 31px">
                <b-button variant="primary" v-b-modal="'modal-new-course'" style="margin-right: 10px">
                    <span class="fa fa-plus"></span>
                    {{ $t('new') }}
                </b-button>
                <b-modal id="modal-new-course" :title="$t('course.title.create')" :ok-title="$t('confirm')" :cancel-title="$t('cancel')" @ok="createCourse">
                    <label class="control-label requiredField">{{ $t('requiredField') }}</label>
                    <form @submit.prevent="createCourse()" ref="createCourse">
                        <course-info v-model="courseInfo_create"></course-info>
                    </form>
                </b-modal>
                <b-button variant="primary" v-b-modal="'modal-copy-course'" style="margin-right: 10px" v-show="selectedCourseId">
                    <span class="fa fa-copy"></span>
                    {{ $t('copy') }}
                </b-button>
                <b-modal id="modal-copy-course" :title="$t('course.question.copy')" :ok-title="$t('confirm')" :cancel-title="$t('cancel')" @ok="copyCourse(selectedSemester_edit, courseCopyId)">
                    <label for="selectedSemester_copy_course" class="control-label">{{ $t('semester') }}</label>
                    <select class="form-control" v-model="courseCopyId" id="selectedSemester_copy_course">
                        <option v-for="semester in semesters" :value="semester.id" :key="semester.id">
                            {{semester.year}} {{semester.type === 'w' ? $t('semester.winterShortcut') : $t('semester.summerShortcut')}}
                        </option>
                    </select>
                </b-modal>

                <b-button variant="danger" v-b-modal="'modal-delete-course'" v-show="selectedCourseId">
                    <span class="fa fa-trash"></span>
                    {{ $t('delete') }}
                </b-button>
                <b-modal id="modal-delete-course" :title="$t('title.delete')" :ok-title="$t('yes')" :cancel-title="$t('no')" @ok="deleteCourse(selectedCourse.id)">
                    {{$t('course.question.delete')}}
                </b-modal>
                
                <div class="offset-md-1 form-inline" v-if="loading_delete">
                    <span class="fa fa-sync fa-spin"></span>
                    <label class="control-label">{{ $t('loading') }}...</label>
                </div>
            </div>
        </div>
        <b-tabs content-class="mt-3" v-if="selectedCourse">
            <b-tab :title="$t('information')" active>
                <label class="control-label requiredField" style="margin-left: 10px" v-show="selectedCourse">{{ $t('requiredField') }}</label>
                <div class="form-horizontal col-md-4">
                    <form @submit.prevent @submit="updateCourse()">
                        <course-info v-model="selectedCourse"></course-info>
                        
                        <div class="form-inline">
                            <b-button variant="primary" type="submit">{{ $t('update') }}</b-button>
                            <div class="offset-md-1 form-inline" v-if="loading_edit">
                                <span class="fa fa-sync fa-spin"></span>
                                <label class="control-label">{{ $t('loading') }}...</label>
                            </div>
                        </div>
                    </form>
                </div>
            </b-tab>
            <b-tab :title="$t('user.assigned')" id="assignedUsers">
                <div class="form-horizontal col-md-7">
                    <div class="form-group">
                        <label for="searchUserText" class="control-label">
                            <span class="fas fa-search"></span>
                            {{ $t('search') }}
                        </label>
                        <input id="searchUserText" type="text" class="form-control" v-model="searchUserText">
                    </div>
                    <div class="form-group">
                        <label class="control-label" for="showRoles">
                            <span class="fas fa-filter"></span>
                            {{$t('show')}}
                        </label>
                        <select class="form-control" id="showRoles" v-model="showRoles">
                            <option v-for="role in rolesWithAll" :value="role.key" :key="role.key">
                                {{role.value}}
                            </option>
                        </select>
                    </div>
                    <table class="table" aria-describedby="assignedUsers">
                        <thead>
                            <th scope="col"></th>
                            <th scope="col">{{$t('matrikelnummer')}}</th>
                            <th scope="col">{{$t('surname')}}</th>
                            <th scope="col">{{$t('forename')}}</th>
                            <th scope="col">{{$t('role')}}</th>
                        </thead>
                        <tbody>
                            <tr v-for="user in filteredUsers" :key="user.matrikelNummer">
                                <td>
                                    <input type="checkbox" class="form-check-input" id="showCheckedUsers" :checked="user.role !== 'n'" @click="user.role = user.role === 'n' ? 's' : 'n'" style="margin-left: 0px">
                                </td>
                                <td>
                                    {{user.matrikelnummer}}
                                </td>
                                <td>
                                    {{user.surname}}
                                </td>
                                <td>
                                    {{user.forename}}
                                </td>
                                <td>
                                    <select class="form-control" v-model="user.role">
                                        <option v-for="role in roles" :value="role.key" :key="role.key">
                                            {{role.value}}
                                        </option>
                                    </select>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <div class="form-inline">
                        <b-button variant="primary" @click="updateCourseUsers()">{{ $t('update') }}</b-button>
                        <div class="offset-md-1 form-inline" v-if="loading_edit_updateUsers">
                            <span class="fa fa-sync fa-spin"></span>
                            <label class="control-label">{{ $t('loading') }}...</label>
                        </div>
                    </div>
                </div>
            </b-tab>
            <b-tab :title="$t('exerciseSheets.name')" id="exerciseSheets">
                <div class="form-horizontal">
                    <div class="form-group col-md-6">
                        <b-button variant="primary" v-b-modal="'modal-new-exerciseSheet'">
                            <span class="fa fa-plus"></span>
                            {{$t('new')}}
                        </b-button>
                        <b-modal id="modal-new-exerciseSheet" :title="$t('exerciseSheet.title.create')" :ok-title="$t('confirm')" :cancel-title="$t('cancel')" @ok="createExerciseSheet">
                            <form ref="exerciseSheet" @submit.prevent="createExerciseSheet()">
                                <es-info v-model="exerciseSheet_create"></es-info>
                            </form>
                        </b-modal>
                    </div>
                    <div class=" col-md-6">
                        <table class="table" aria-describedby="exerciseSheets">
                            <thead>
                                <th scope="col">{{$t('name')}}</th>
                                <th scope="col">{{$t('submissionDate')}}</th>
                                <th scope="col">{{$t('actions')}}</th>
                            </thead>
                            <tbody>
                                <tr v-for="sheet in selectedCourse.exerciseSheets" :key="sheet.id">
                                    <td>
                                        {{sheet.name}}
                                    </td>
                                    <td>
                                        {{sheet.submissionDate}}
                                    </td>
                                    <td>
                                        <router-link :title="$t('edit')" :to="{
                                            name: 'SheetManagement',
                                            params: {
                                                courseId: selectedCourseId,
                                                sheetId: sheet.id
                                            }
                                        }">
                                            <span class="fa fa-edit fa-2x"></span>
                                        </router-link>
                                        <a href="#" :title="$t('delete')" v-b-modal="'modal-delete-exerciseSheet'">
                                            <span class="fa fa-trash fa-2x"></span>
                                        </a>
                                        <b-modal id="'modal-delete-exerciseSheet" :title="$t('title.delete')" :ok-title="$t('confirm')" :cancel-title="$t('cancel')" @ok="deleteExerciseSheet(sheet.id)">
                                            {{$t('exerciseSheet.question.delete')}}
                                        </b-modal>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </b-tab>
        </b-tabs>
    </div>
</template>

<script>
import CourseInfo from '@/components/CourseInfo.vue';
import ExerciseSheetInfo from '@/components/ExerciseSheetInfo.vue';
export default {
    components: {
        'course-info': CourseInfo,
        'es-info': ExerciseSheetInfo
    },
    data(){
        return {
            courseInfo_create: {},
            loading_create: false,
            loading_edit: false,
            loading_delete: false,
            loading_edit_updateUsers: false,
            selectedSemester_edit: undefined,
            selectedCourse: undefined,
            selectedCourseId: undefined,
            courseCopyId: undefined,
            courses: [],
            searchUserText: undefined,
            showRoles: 'z',
            courseUsers: [],
            semesters: [],
            exerciseSheet_create: {}
        }
    },
    created(){
        this.getSemesters();
        this.resetExerciseSheet();
    },
    computed: {
        roles(){
            return this.$store.getters.courseRoles;
        },
        rolesWithAll(){
            return [
                {
                    key: 'a',
                    value: this.$t('all')
                },
                {
                    key: 'z',
                    value: this.$t('assigned')
                }
            ].concat(this.roles);
        },
        filteredUsers(){
            let result = this.courseUsers;

            if(this.showRoles === 'z'){
                result = result.filter(user => user.role !== 'n');
            }
            else if(this.showRoles !== 'a'){
                result = result.filter(user => this.showRoles === user.role)
            }
            
            if(this.searchUserText){
                result = result.filter(user => user.matrikelNummer.indexOf(this.searchUserText) !== -1
                                            || user.surname.indexOf(this.searchUserText) !== -1
                                            || user.forename.indexOf(this.searchUserText) !== -1);
            }

            return result;
        }
    },
    methods:{
        resetExerciseSheet(){
            this.exerciseSheet_create = {
                submissionDate: this.$store.getters.currentDateTime
            }
        },
        createExerciseSheet(modal){
            if(modal && !this.$refs.exerciseSheet.checkValidity()){
                modal.preventDefault();
                this.$refs.exerciseSheet.reportValidity();
            }
            else{
                this.exerciseSheet_create.courseId = this.selectedCourseId;
                this.$store.dispatch('createExerciseSheet', this.exerciseSheet_create).then(()=>{
                    this.$bvModal.hide('modal-new-exerciseSheet');
                    if(this.exerciseSheet_create.courseId === this.selectedCourseId){
                        this.getExerciseSheets(this.selectedCourseId);
                    }
                    this.resetExerciseSheet();
                    this.$bvToast.toast(this.$t('exerciseSheet.created'), {
                        title: this.$t('success'),
                        variant: 'success',
                        appendToast: true
                    });
                    
                }).catch(()=>{
                    this.$bvToast.toast(this.$t('exerciseSheet.error.create'), {
                        title: this.$t('error'),
                        variant: 'danger',
                        appendToast: true
                    });
                });
            }
        },
        deleteExerciseSheet(sheetId){
            const courseId = this.selectedCourseId;
            this.$store.dispatch('deleteExerciseSheet', sheetId).then(()=>{
                if(courseId === this.selectedCourseId){
                    this.getExerciseSheets(this.selectedCourseId);
                }
                
                this.$bvToast.toast(this.$t('exerciseSheet.deleted'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
                
            }).catch(()=>{
                this.$bvToast.toast(this.$t('exerciseSheet.error.delete'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            });
        },
        getExerciseSheets(id){
            this.$store.dispatch('getExerciseSheets', {id}).then((response)=>{
                if(this.selectedCourseId === id){
                    this.selectedCourse.exerciseSheets = response.data;
                }
            }).catch(()=>{
                this.$bvToast.toast(this.$t('exerciseSheet.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            })
        },
        updateCourseUsers(){
            this.loading_edit_updateUsers = true;
            const id = this.selectedCourse.id;
            const data = this.courseUsers.filter(user => user.role !== user.oldRole).map(user =>{
                return {
                    courseId: id,
                    matrikelNummer: user.matrikelNummer,
                    role: user.role
                };
            });
            this.$store.dispatch('updateCourseUsers', data).then(response=>{
                this.$bvToast.toast(this.$t('course.usersUpdated'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }).catch(()=>{
                this.$bvToast.toast(this.$t('course.error.usersUpdated'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }).finally(()=>{
                this.loading_edit_updateUsers = false;
            });
        },
        createCourse(modal){
            if(modal && !this.$refs.createCourse.checkValidity()){
                modal.preventDefault();
                this.$refs.createCourse.reportValidity();
            }
            else{
                this.courseInfo_create.semesterId = this.selectedSemester_edit;
                this.loading_create = true;
                this.$store.dispatch('createCourse',this.courseInfo_create).then(response=>{
                    this.$bvToast.toast(this.$t('course.created'), {
                        title: this.$t('success'),
                        variant: 'success',
                        appendToast: true
                    });
                    const {semesterId} = this.courseInfo_create;
                    this.courseInfo_create = {semesterId};
                    if(semesterId === this.selectedSemester_edit){
                        this.getCourses(this.selectedSemester_edit);
                    }
                }).catch(()=>{
                    this.$bvToast.toast(this.$t('course.error.create'), {
                        title: this.$t('error'),
                        variant: 'danger',
                        appendToast: true
                    });
                }).finally(()=>{
                    this.loading_create = false;
                });
            }
        },
        updateCourse(){
            this.loading_edit = true;
            const {exerciseSheets, ...data} = this.selectedCourse;
            this.$store.dispatch('updateCourse', data).then(response=>{
                this.$bvToast.toast(this.$t('course.updated'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }).catch(()=>{
                this.$bvToast.toast(this.$t('course.error.update'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }).finally(()=>{
                this.loading_edit = false;
            });
        },
        copyCourse(courseId, semesterId){
            this.loading_delete = true;
            this.$store.dispatch('copyCourse', {courseId, semesterId}).then(response=>{
                if(id === selectedSemester_edit){
                    this.getCourses(id);
                }
                this.$bvToast.toast(this.$t('course.copied'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }).catch(()=>{
                this.$bvToast.toast(this.$t('course.error.copy'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }).finally(()=>{
                this.loading_delete = false;
            });
            
        },
        getCourse(courseId){
            this.$store.dispatch('getCourse',{courseId}).then(response =>{
                this.selectedCourse = response.data;
            }).catch(()=>{
                this.$bvToast.toast(this.$t('course.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            });
        },
        getCourses(id){
            this.$store.dispatch('getCourses',{id}).then(response =>{
                this.courses = response.data;
            }).catch(()=>{
                this.$bvToast.toast(this.$t('course.errors.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            });
        },
        deleteCourse(id){
            this.loading_delete = true;
            this.$store.dispatch('deleteCourse',{id}).then(response =>{
                this.$bvToast.toast(this.$t('course.deleted'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
                this.getCourses(this.selectedSemester_edit);
            }).catch(()=>{
                this.$bvToast.toast(this.$t('course.error.delete'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }).finally(()=>{
                this.loading_delete = false;
            });
        },
        getCourseUsers(courseId){
            this.$store.dispatch('getUsers',{courseId}).then(response=>{
                this.courseUsers = response.data;
            }).catch(()=>{
                this.$bvToast.toast(this.$t('user.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            });
        },
        getSemesters(){
            this.$store.dispatch('getSemesters').then(response =>{
                this.semesters = response.data;
                this.courseInfo_create.semesterId = this.courseCopyId = this.selectedSemester_edit = this.semesters[0].id;
                this.getCourses(this.selectedSemester_edit);
            }).catch(()=>{
                this.$bvToast.toast(this.$t('semester.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            });
        }
    }
}
</script>