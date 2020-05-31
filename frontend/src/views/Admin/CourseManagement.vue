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
                    <label for="courseSemester_edit" class="control-label">{{ $t('semester.name') }}</label>
                    <select class="form-control" v-model="selectedSemester_edit" id="courseSemester_edit" @change="getCourses(selectedSemester_edit); selectedCourse = selectedCourseId = undefined">
                        <option v-for="semester in semesters" :value="semester.id" :key="semester.id">
                            {{semester.year}} {{semester.type === 'w' ? $t('semester.winterShortcut') : $t('semester.summerShortcut')}}
                        </option>
                    </select>
                </div>
            </div>
            <div class="col-md-4">
                <div class="form-group">
                    <label for="selectedCourse" class="control-label">{{ $t('course.name') }}</label>
                    <select class="form-control" v-model="selectedCourseId" id="selectedCourse" @change="getCourse(selectedCourseId)">
                        <option v-for="course in courses" :value="course.id" :key="course.id">
                            {{course.number}} {{course.name}}
                        </option>
                    </select>
                </div>
            </div>
            <div class="col-md-4" style="margin-top: 31px">
                <button class="btn btn-primary" v-b-modal="'modal-new-course'" style="margin-right: 10px">
                    <span class="fa fa-plus"></span>
                    {{ $t('new') }}
                </button>
                <b-modal id="modal-new-course" :title="$t('course.title.create')" :ok-title="$t('confirm')" :cancel-title="$t('cancel')" @ok="createCourse">
                    <label class="control-label requiredField">{{ $t('requiredField') }}</label>
                    <form @submit.prevent="createCourse()" ref="createCourse">
                        <course-info v-model="courseInfo_create" :users="users"></course-info>
                    </form>
                </b-modal>
                <button class="btn btn-primary" v-b-modal="'modal-copy-course'" style="margin-right: 10px" v-show="selectedCourseId" @click="courseCopyId = semestersWithoutSelected[0].id">
                    <span class="fa fa-copy"></span>
                    {{ $t('copy') }}
                </button>
                <b-modal id="modal-copy-course" :title="$t('course.question.copy')" :ok-title="$t('confirm')" :cancel-title="$t('cancel')" @ok="copyCourse(selectedCourseId, courseCopyId)">
                    <label for="selectedSemester_copy_course" class="control-label">{{ $t('semester.name') }}</label>
                    <select class="form-control" v-model="courseCopyId" id="selectedSemester_copy_course">
                        <option v-for="semester in semestersWithoutSelected" :value="semester.id" :key="semester.id">
                            {{semester.year}} {{semester.type === 'w' ? $t('semester.winterShortcut') : $t('semester.summerShortcut')}}
                        </option>
                    </select>
                </b-modal>

                <button class="btn btn-danger" v-b-modal="'modal-delete-course'" v-show="selectedCourseId">
                    <span class="fa fa-sync fa-spin" v-if="loading_delete"></span>
                    <span class="fa fa-trash" v-else></span>
                    
                    {{ $t('delete') }}
                </button>
                <b-modal id="modal-delete-course" :title="$t('title.delete')" :ok-title="$t('yes')" :cancel-title="$t('no')" @ok="deleteCourse(selectedCourse.id)">
                    {{$t('course.question.delete')}}
                </b-modal>
            </div>
        </div>
        <b-tabs content-class="mt-3" v-if="selectedCourse">
            <b-tab :title="$t('information')" active>
                <label class="control-label requiredField" style="margin-left: 10px">{{ $t('requiredField') }}</label>
                <div class="form-horizontal col-md-4">
                    <form @submit.prevent="updateCourse()">
                        <course-info v-model="selectedCourse" :users="users"></course-info>
                        
                        <div class="form-inline">
                            <button class="btn btn-primary" type="submit">
                                <span class="fa fa-sync fa-spin" v-if="loading_edit"></span>
                                <span class="fa fa-save" v-else></span>
                                {{ $t('save') }}
                            </button>
                        </div>
                    </form>
                </div>
            </b-tab>
            <b-tab :title="$t('user.assigned')" id="assignedUsers">
                <div class="form-horizontal col-md-7">
                    <div class="form-group">
                        <button class="btn btn-primary" type="button" @click="getAttendanceList(selectedCourseId)">
                            <span class="fa fa-sync fa-spin" v-if="loading_attendanceList"></span>
                            <span class="fas fa-download" v-else></span>
                            {{$t('attendance.list')}}
                        </button>
                    </div>
                    <div class="form-group">
                        <label class="btn btn-primary finger">
                            <span class="fa fa-sync fa-spin" v-if="loadingFileUpload"></span>
                            <span class="fas fa-upload" v-else></span>
                            {{ $t('uploadCSV') }}
                            <input type="file" class="d-none" id="file" ref="file" accept=".csv" @change="submitUsers()"/>
                        </label>
                    </div>
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
                            <th scope="col">{{$t('matriculationNumber')}}</th>
                            <th scope="col">{{$t('surname')}}</th>
                            <th scope="col">{{$t('forename')}}</th>
                            <th scope="col">{{$t('role')}}</th>
                        </thead>
                        <tbody>
                            <tr v-for="user in filteredUsers" :key="user.matriculationNumber">
                                <td>
                                    <input type="checkbox" class="form-check-input" id="showCheckedUsers" :checked="user.courseRole !== 'n'" @click="user.courseRole = user.courseRole === 'n' ? 's' : 'n'" style="margin-left: 0px">
                                </td>
                                <td>
                                    {{user.matriculationNumber}}
                                </td>
                                <td>
                                    {{user.surname}}
                                </td>
                                <td>
                                    {{user.forename}}
                                </td>
                                <td>
                                    <select class="form-control" v-model="user.courseRole">
                                        <option v-for="role in roles" :value="role.key" :key="role.key">
                                            {{role.value}}
                                        </option>
                                    </select>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <div class="form-inline">
                        <button class="btn btn-primary" @click="updateCourseUsers()">
                            <span class="fa fa-sync fa-spin" v-if="loading_edit_updateUsers"></span>
                            <span class="fa fa-save" v-else></span>
                            {{ $t('save') }}
                        </button>
                    </div>
                </div>
            </b-tab>
            <b-tab :title="$t('exerciseSheets.name')" id="exerciseSheets">
                <div class="form-horizontal">
                    <div class="form-group col-md-6">
                        <button class="btn btn-primary" v-b-modal="'modal-new-exerciseSheet'">
                            <span class="fa fa-plus"></span>
                            {{$t('new')}}
                        </button>
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
                                        {{new Date(sheet.submissionDate).toLocaleString()}}
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
                                        <a href.prevent="#" :title="$t('delete')" v-b-modal="'modal-delete-exerciseSheet'">
                                            <span class="fa fa-trash fa-2x"></span>
                                        </a>
                                        <b-modal id="modal-delete-exerciseSheet" :title="$t('title.delete')" :ok-title="$t('confirm')" :cancel-title="$t('cancel')" @ok="deleteExerciseSheet(sheet.id)">
                                            {{$t('exerciseSheet.question.delete')}}
                                        </b-modal>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </b-tab>
            <b-tab :title="$t('templates')">
                <div class="form-horizontal col-md-6">
                    <div class="form-group">
                        <label class="control-label" for="defaultDescription">{{$t('descriptionExerciseSheets')}}</label>
                        <div class="document-editor__editable-container">
                            <ckeditor :id="`defaultDescription`" v-model="selectedCourseTemplate" :editor="editor" @ready="onReady" :config="editorConfig" ></ckeditor>
                        </div>
                    </div>
                    <button class="btn btn-primary" type="button" @click="saveDefaultTemplate()">
                        <span class="fa fa-sync fa-spin" v-if="loading_defaultTemplate"></span>
                        <span class="fa fa-save" v-else></span>
                        {{ $t('save') }}
                    </button>
                </div>
            </b-tab>
        </b-tabs>
    </div>
</template>

<script>
import CourseInfo from '@/components/CourseInfo.vue';
import ExerciseSheetInfo from '@/components/ExerciseSheetInfo.vue';
import {userManagement, dateManagement, editorManagement, fileManagement} from '@/plugins/global';
import Editor from '@/components/ckeditor';
import i18n from '@/plugins/i18n';

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
            exerciseSheet_create: {},
            users: [],
            loadingFileUpload: false,
            loading_defaultTemplate: false,
            selectedCourseTemplate: undefined,
            editor: Editor,
            editorConfig: {
	            language: i18n.locale
            },
            loading_attendanceList: false
        }
    },
    created(){
        //TO-DO: props['courseId']
        //Query or property?
        //if there are props then getCourse(courseId) is called
        //with the response you also have the semesterId for selecting the right semester in Dropdown
        //don't forget to set props: true; in router
        this.getSemesters();
        this.resetExerciseSheet();
        if(this.$store.getters.userInfo.isAdmin){
            this.getUsers();
        }
    },
    computed: {
        roles(){
            return userManagement.roles();
        },
        rolesWithAll(){
            return userManagement.rolesAllAssign();
        },
        filteredUsers(){
            return userManagement.filteredUsers({users: this.courseUsers, role: this.showRoles, searchText: this.searchUserText});
        },
        semestersWithoutSelected(){
            return this.semesters.filter(semester => semester.id !== this.selectedSemester_edit);
        }
    },
    methods:{
        onReady: editorManagement.onReady,
        getAttendanceList(courseId){
            this.loading_attendanceList = true;
            this.$store.dispatch('getAttendanceList', courseId).then(response => {
                const url = window.URL.createObjectURL(response.data);
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', fileManagement.getFileNameOutOfHeader(response.headers));
                document.body.appendChild(link);
                link.click();
            }).catch(()=>{
                this.$bvToast.toast(this.$t('attendance.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }).finally(()=>{
                this.loading_attendanceList = false;
            });
        },
        getUsers(){
            this.$store.dispatch('getUsers').then(({data}) =>{
                this.users = data;
            }).catch(()=>{
                this.$bvToast.toast(this.$t('users.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            });
        },
        submitUsers(){
            this.loadingFileUpload = true;
            const formData = new FormData();
            formData.append('file',this.$refs.file.files[0]);
            formData.append('id', this.selectedCourseId)
            this.$refs.file.value = '';
            this.$store.dispatch('assignCourseUsers', formData).then(response =>{
                this.$bvToast.toast(this.$t('course.usersSaved'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }).catch(()=>{
                this.$bvToast.toast(this.$t('course.error.usersSave'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }).finally(()=>{
                this.loadingFileUpload = false;
            });
        },
        resetExerciseSheet(){
            this.exerciseSheet_create = {
                submissionDate: dateManagement.currentDateTime(),
                issueDate: dateManagement.currentDateTime()
            }
        },
        createExerciseSheet(modal){
            if(modal && !this.$refs.exerciseSheet.checkValidity()){
                modal.preventDefault();
                this.$refs.exerciseSheet.reportValidity();
            }
            else{
                this.exerciseSheet_create.courseId = this.selectedCourseId;
                this.exerciseSheet_create.description = this.selectedCourse.descriptionTemplate;
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
            const data = this.courseUsers.filter(user => user.courseRole !== user.oldRole).map(user =>{
                return {
                    courseId: id,
                    matriculationNumber: user.matriculationNumber,
                    role: user.courseRole
                };
            });
            this.$store.dispatch('updateCourseUsers', data).then(response=>{
                this.$bvToast.toast(this.$t('course.usersSaved'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }).catch(()=>{
                this.$bvToast.toast(this.$t('course.error.usersSave'), {
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
                        this.selectedCourseId = response.data.id;
                        this.getCourse(response.data.id);
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
                this.$bvToast.toast(this.$t('course.saved'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }).catch(()=>{
                this.$bvToast.toast(this.$t('course.error.save'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }).finally(()=>{
                this.loading_edit = false;
            });
        },
        saveDefaultTemplate(){
            this.$store.dispatch('updateCourseDefaultTemplate', {id: this.selectedCourse.id, description: this.selectedCourseTemplate}).then(()=>{
                this.selectedCourse.descriptionTemplate = this.selectedCourseTemplate;
                this.$bvToast.toast(this.$t('course.templateUpdated'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }).catch(()=>{
                this.$bvToast.toast(this.$t('course.error.templateUpdate'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            })
        },
        copyCourse(courseId, semesterId){
            this.loading_delete = true;
            this.$store.dispatch('copyCourse', {courseId, semesterId}).then(response=>{
                this.selectedSemester_edit = semesterId;
                this.getCourses(semesterId);
                this.selectedCourseId = response.data.id;
                this.getCourse(response.data.id);
                
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
            this.getCourseUsers(courseId);
            this.$store.dispatch('getCourse',{courseId}).then(response =>{
                this.selectedCourse = response.data;
                this.selectedCourseTemplate = this.selectedCourse.descriptionTemplate; //no reference; update on save
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
                if(id === this.selectedCourseId){
                    this.selectedCourse = undefined;
                }
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
                this.$bvToast.toast(this.$t('users.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            });
        },
        getSemesters(){
            this.$store.dispatch('getSemesters').then(response =>{
                this.semesters = response.data;
                if(this.semesters.length !== 0){
                    this.courseInfo_create.semesterId = this.courseCopyId = this.selectedSemester_edit = this.semesters[0].id;
                    this.getCourses(this.selectedSemester_edit);
                }
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