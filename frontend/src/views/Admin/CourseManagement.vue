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
        <label class="control-label requiredField" style="margin-left: 10px">{{ $t('requiredField') }}</label>
        <b-tabs content-class="mt-3">
            <b-tab :title="$t('create')" active>
                <div class="form-horizontal col-md-4">
                    <form @submit.prevent @submit="createCourse()">
                        <div class="form-group">
                            <label for="semester_create" class="control-label required">{{$t('semester.semester')}}</label>
                            <select class="form-control" v-model="courseInfo_create.semesterId" id="semester_create" required>
                                <option v-for="semester in semesters" :value="semester.id" :key="semester.id">
                                    {{semester.year}} {{semester.type === 'w' ? $t('semester.winterShortcut') : $t('semester.summerShortcut')}}
                                </option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="number_create" class="control-label required">{{ $t('number') }} ({{$t('format')}} : 123.456)</label>
                            <input id="number_create" type="text" class="form-control"  pattern="[0-9]{3}\.[0-9]{3}" :title="$t('format')+': 123.456'" v-model="courseInfo_create.number" required>
                        </div>
                        <div class="form-group">
                            <label for="name_create" class="control-label required">{{ $t('name') }}</label>
                            <input id="name_create" type="text" class="form-control" v-model="courseInfo_create.name" required>
                        </div>
                        <div class="form-group">
                            <label for="minKreuzel_create" class="control-label">{{$t('minRequireKreuzel')}}</label>
                            <div class="col-md-6" style="padding-left: 0px">
                                <i-input id="minKreuzel_create" class="form-control" min="0" max="100" v-model="courseInfo_create.minKreuzel"> </i-input>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="minPoints_create" class="control-label">{{$t('minRequirePoints')}}</label>
                            <div class="col-md-6" style="padding-left: 0px">
                                <i-input id="minPoints_create" class="form-control" min="0" max="100" v-model="courseInfo_create.minPoints"> </i-input>
                            </div>
                        </div>
                        <div class="form-inline">
                            <b-button variant="primary" type="submit">
                                <span class="fa fa-plus"></span>
                                {{ $t('create') }}
                            </b-button>
                            <div class="offset-md-1 form-inline" v-if="loading_create">
                                <span class="fa fa-sync fa-spin"></span>
                                <label class="control-label">{{ $t('loading') }}...</label>
                            </div>
                        </div>
                    </form>
                </div>
            </b-tab>
            <b-tab :title="$t('edit')">
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
                    <div class="col-md-4" style="margin-top: 31px" v-show="selectedCourseId">
                        <b-button variant="primary" v-b-modal="'modal-copy-course'" style="margin-right: 10px">
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

                        <b-button variant="danger" v-b-modal="'modal-delete-course'">
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
                        <div class="form-horizontal col-md-4">
                            <form @submit.prevent @submit="updateCourse()">
                                <div class="form-group">
                                    <label for="courseNumber_edit" class="control-label required">{{ $t('number') }} ({{$t('format')}} : 123.456)</label>
                                    <input id="courseNumber_edit" type="text" class="form-control"  pattern="[0-9]{3}\.[0-9]{3}" :title="$t('format') +': 123.456'" v-model="selectedCourse.number" required>
                                </div>
                                <div class="form-group">
                                    <label for="courseName_edit" class="control-label required">{{ $t('name') }}</label>
                                    <input id="courseName_edit" type="text" class="form-control" v-model="selectedCourse.name" required>
                                </div>
                                <div class="form-group">
                                    <label for="minKreuzel_edit" class="control-label">{{$t('minRequireKreuzel')}}</label>
                                    <div class="col-md-6" style="padding-left: 0px">
                                        <i-input id="minKreuzel_edit" class="form-control" min="0" max="100" v-model="selectedCourse.minKreuzel"> </i-input>
                                    </div>
                                </div>
                                <div class="form-group" >
                                    <label for="minPoints_edit" class="control-label">{{$t('minRequirePoints')}}</label>
                                    <div class="col-md-6" style="padding-left: 0px">
                                        <i-input id="minPoints_edit" class="form-control" min="0" max="100" v-model="selectedCourse.minPoints"> </i-input>
                                    </div>
                                </div>
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
                                    <th scope="col">{{$t('change')}}</th>
                                </thead>
                                <tbody>
                                    <tr v-for="user in filteredUsers" :key="user.matrikelNummer">
                                        <td>
                                            <input type="checkbox" class="form-check-input" id="showCheckedUsers" :checked="user.role !== 'n'" @click="user.role = user.role === 'n' ? 's' : 'n'">
                                        </td>
                                        <td>
                                            {{user.matrikelNummer}}
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
                                        <td>
                                            {{user.role !== user.oldRole ? $t('yes') : $t('no')}}
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
                    <b-tab :title="$t('exerciseSheet.exerciseSheets')" id="exerciseSheets">
                        <div class="form-horizontal">
                            <div class="form-group col-md-6">
                                <b-button variant="primary">
                                    <span class="fa fa-plus"></span>
                                    {{$t('new')}}
                                </b-button>
                            </div>
                            <div class=" col-md-6">
                                <table class="table" aria-describedby="exerciseSheets">
                                    <thead>
                                        <th scope="col">{{$t('name')}}</th>
                                        <th scope="col">{{$t('submissionDate')}}</th>
                                        <th scope="col">{{$t('actions')}}</th>
                                    </thead>
                                    <tbody>
                                        <tr v-for="sheet in exerciseSheets" :key="sheet.id">
                                            <td>
                                                {{sheet.name}}
                                            </td>
                                            <td>
                                                {{sheet.submissionDate}}
                                            </td>
                                            <td>
                                                <a href="#" :title="$t('edit')" @click.prevent="">
                                                    <span class="fa fa-edit fa-2x"></span>
                                                </a>
                                                <a href="#" :title="$t('delete')" @click.prevent="">
                                                    <span class="fa fa-trash fa-2x"></span>
                                                </a>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </b-tab>
                </b-tabs>
            </b-tab>
        </b-tabs>
    </div>
</template>

<script>
export default {
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
            exerciseSheets: []
        }
    },
    created(){
        this.getSemesters();
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
        createCourse(){
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
        },
        updateCourse(){
            this.loading_edit = true;
            this.$store.dispatch('updateCourse', this.selectedCourse).then(response=>{
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