<template>
  <div class="admin">
      <label class="control-label requiredField" style="margin-left: 10px">{{ $t('requiredField') }}</label>
      <b-tabs content-class="mt-3">
            <b-tab :title="$t('createUser')" active>
                <div class="row" style="margin-left: 0px">
                    <div class="form-horizontal col-md-4">
                        <form @submit.prevent @submit="createUser()">
                            <div class="form-group">
                                <label for="username" class="control-label required">{{ $t('username') }}</label> 
                                <input id="username" type="text" class="form-control" v-model="username" required>
                            </div>
                            <div class="form-group">
                                <label for="matrikelnummer" class="control-label required">{{ $t('matrikelnummer') }}</label>
                                <input id="matrikelnummer" type="text" class="form-control"  pattern="[0-9]{8}" v-model="matrikelnummer" :title="$t('eightDigitNumber')" required>
                            </div>
                            <div class="form-group">
                                <label for="surname" class="control-label required">{{ $t('surname') }}</label>
                                <input id="surname" type="text" class="form-control" v-model="surname" required>
                            </div>
                            <div class="form-group">
                                <label for="forename" class="control-label required">{{ $t('forename') }}</label>
                                <input id="forename" type="text" class="form-control" v-model="forename" required>
                            </div>
                            <div class="form-inline">
                                <b-button variant="primary" type="submit">{{ $t('create') }}</b-button>
                                <div class="offset-md-1 form-inline" v-if="loadingCreateUser">
                                    <span class="fa fa-sync fa-spin"></span>
                                    <label class="control-label">{{ $t('loading') }}...</label>
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="form-horizontal col-md-4 offset-md-1">
                        <div class="form-inline">
                            <label class="btn btn-primary col-md-5 finger">
                                {{ $t('uploadCSV') }} <input type="file" class="d-none" id="file" ref="file" accept=".csv" @change="submitFile()"/>
                            </label>
                            <div class="offset-md-1 form-inline" v-if="loadingFileUpload">
                                <span class="fa fa-sync fa-spin"></span>
                                <label class="control-label">{{ $t('loading') }}...</label>
                            </div>
                        </div>
                    </div>
                </div>
            </b-tab>
            <b-tab :title="$t('courseManagement')">
                <b-tabs content-class="mt-3">
                    <b-tab :title="$t('create')" active>
                        <div class="form-horizontal col-md-4">
                            <form @submit.prevent @submit="createCourse()">
                                <div class="form-group">
                                    <label for="courseSemester_create" class="control-label required">{{$t('semester')}}</label>
                                    <select class="form-control" v-model="selectedSemester_create" id="courseSemester_create" required>
                                        <option v-for="semester in semesters" :value="semester.id" :key="semester.id">
                                            {{semester.year}} {{semester.type === 'w' ? $t('winterSemesterShortcut') : $t('summerSemesterShortcut')}}
                                        </option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label for="courseNumber_create" class="control-label required">{{ $t('number') }} ({{$t('format')}} : 123.456)</label>
                                    <input id="courseNumber_create" type="text" class="form-control"  pattern="[0-9]{3}\.[0-9]{3}" :title="$t('format')+': 123.456'" v-model="courseNumber_create" required>
                                </div>
                                <div class="form-group">
                                    <label for="courseName_create" class="control-label required">{{ $t('name') }}</label>
                                    <input id="courseName_create" type="text" class="form-control" v-model="courseName_create" required>
                                </div>
                                <div class="form-group col-md-6" style="padding-left:0px">
                                    <label for="minKreuzel_create" class="control-label">{{$t('minRequireKreuzel')}}</label>
                                    <i-input id="minKreuzel_create" class="form-control" min="0" max="100" v-model="minKreuzel_create"> </i-input>
                                </div>
                                <div class="form-group col-md-6" style="padding-left:0px">
                                    <label for="minPoints_create" class="control-label">{{$t('minRequirePoints')}}</label>
                                    <i-input id="minPoints_create" class="form-control" min="0" max="100" v-model="minPoints_create"> </i-input>
                                </div>
                                <div class="form-inline">
                                    <b-button variant="primary" type="submit">{{ $t('create') }}</b-button>
                                    <div class="offset-md-1 form-inline" v-if="loadingCourse_create">
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
                                    <label for="courseSemester_edit" class="control-label">{{ $t('semester') }}</label>
                                    <select class="form-control" v-model="selectedSemester_edit" id="courseSemester_edit" @change="getCourses(selectedSemester_edit)">
                                        <option v-for="semester in semesters" :value="semester.id" :key="semester.id">
                                            {{semester.year}} {{semester.type === 'w' ? $t('winterSemesterShortcut') : $t('summerSemesterShortcut')}}
                                        </option>
                                    </select>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <div class="form-group">
                                    <label for="selectedCourse" class="control-label">{{ $t('course') }}</label>
                                    <select class="form-control" v-model="selectedCourseId" id="selectedCourse" @change="getUsers(selectedCourseId); getCourse(selectedCourseId)">
                                        <option v-for="course in courses" :value="course.id" :key="course.id">
                                            {{course.number}} {{course.name}}
                                        </option>
                                    </select>
                                </div>
                            </div>
                            <div class="col-md-1" style="margin-top: 30px">
                                <b-button variant="danger" v-b-modal="'modal-delete-course'">{{ $t('delete') }}</b-button>
                                <b-modal id="modal-delete-course" :title="$t('confirmDeletion')" :ok-title="$t('yes')" :cancel-title="$t('no')" @ok="deleteCourse(selectedCourse.id)">
                                    {{$t('courseDeleteQuestion')}}
                                </b-modal>
                            </div>
                        </div>
                        <div class="row col-md-12" v-if="selectedCourse">
                            <div class="form-horizontal col-md-4" style="margin-left: 0px">
                                <form @submit.prevent @submit="updateCourse()">
                                    <div class="form-group">
                                        <label for="courseNumber_edit" class="control-label required">{{ $t('number') }} ({{$t('format')}} : 123.456)</label>
                                        <input id="courseNumber_edit" type="text" class="form-control"  pattern="[0-9]{3}\.[0-9]{3}" :title="$t('format') +': 123.456'" v-model="selectedCourse.number" required>
                                    </div>
                                    <div class="form-group">
                                        <label for="courseName_edit" class="control-label required">{{ $t('name') }}</label>
                                        <input id="courseName_edit" type="text" class="form-control" v-model="selectedCourse.name" required>
                                    </div>
                                    <div class="form-group col-md-6" style="padding-left:0px">
                                        <label for="minKreuzel_edit" class="control-label">{{$t('minRequireKreuzel')}}</label>
                                        <i-input id="minKreuzel_edit" class="form-control" min="0" max="100" v-model="selectedCourse.minKreuzel"> </i-input>
                                    </div>
                                    <div class="form-group col-md-6" style="padding-left:0px">
                                        <label for="minPoints_edit" class="control-label">{{$t('minRequirePoints')}}</label>
                                        <i-input id="minPoints_edit" class="form-control" min="0" max="100" v-model="selectedCourse.minPoints"> </i-input>
                                    </div>
                                    <div class="form-inline">
                                        <b-button variant="primary" type="submit">{{ $t('update') }}</b-button>
                                        <div class="offset-md-1 form-inline" v-if="loadingCourse_edit">
                                            <span class="fa fa-sync fa-spin"></span>
                                            <label class="control-label">{{ $t('loading') }}...</label>
                                        </div>
                                    </div>
                                </form>
                            </div>
                            <div class="form-horizontal col-md-7 offset-md-1">
                                <div class="form-group">
                                    <label for="searchUserText" class="control-label">{{ $t('search') }}
                                        <span class="fas fa-search"></span>
                                    </label>
                                    <input id="searchUserText" type="text" class="form-control" v-model="searchUserText">
                                </div>
                                <div class="form-group">
                                    <label class="control-label" for="showRoles">{{$t('show')}}
                                        <span class="fas fa-filter"></span>
                                    </label>
                                    <select class="form-control" id="showRoles" v-model="showRoles">
                                        <option v-for="role in rolesWithAll" :value="role.key" :key="role.key">
                                            {{role.value}}
                                        </option>
                                    </select>
                                </div>
                                <table class="table">
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
                                    <div class="offset-md-1 form-inline" v-if="loadingCourse_edit_updateUsers">
                                        <span class="fa fa-sync fa-spin"></span>
                                        <label class="control-label">{{ $t('loading') }}...</label>
                                    </div>
                                </div>
                            </div>
                            
                        </div>
                    </b-tab>
                </b-tabs>
            </b-tab>
            <b-tab :title="$t('createSemester')">
                <div class="form-horizontal col-md-4">
                    <form @submit.prevent @submit="createSemester()">
                        <div class="form-group">
                            <label for="semesterYear" class="control-label required">{{$t('year')}}</label>
                            <i-input id="semesterYear" class="form-control"  :max="maxYear" :min="maxYear-100" v-model="semesterYear" required></i-input>
                        </div>
                        <div class="form-check">
                            <input id="semesterTypeSummer" type="radio" value="s" class="form-check-input"  v-model="semesterType">
                            <label for="semesterTypeSummer" class="form-check-label">
                                {{$t('summerSemester')}}
                            </label>
                        </div>
                        <div class="form-check">
                            <input id="semesterTypeWinter" type="radio" value="w" class="form-check-input"  v-model="semesterType">
                            <label for="semesterTypeWinter"   class="form-check-label" style="margin-right:15px">
                                {{$t('winterSemester')}}
                            </label>
                        </div>
                        <div class="form-inline">
                            <b-button variant="primary" type="submit">{{$t('create')}}</b-button>
                            <div class="offset-md-1 form-inline" v-if="loadingCreateSemester">
                                <span class="fa fa-sync fa-spin"></span>
                                <label class="control-label">{{$t('loading')}}...</label>
                            </div>
                        </div>
                    </form>
                </div>
            </b-tab>
      </b-tabs>
  </div>
</template>

<script>

export default {
    data() {
        return {
            username: undefined,
            matrikelnummer: undefined,
            forename: undefined,
            surname: undefined,
            loadingFileUpload: false,
            loadingCreateUser: false,
            loadingCreateSemester: false,
            loadingCourse_create: false,
            loadingCourse_edit: false,
            loadingCourse_edit_updateUsers: false,
            semesterYear: new Date().getFullYear(),
            maxYear: new Date().getFullYear(),
            semesterType: undefined,
            semesters: [],
            selectedSemester_create: undefined,
            selectedSemester_edit: undefined,
            courseNumber_create: undefined,
            courseName_create: undefined,
            minKreuzel_create: undefined,
            minPoints_create: undefined,
            selectedCourse: undefined,
            selectedCourseId: undefined,
            courses: [],
            searchUserText: undefined,
            showRoles: 'z',
            users: []
        }
    },
    computed:{
        roles(){
            return [
                {
                    key: 'l',
                    value: this.$t('lecturer'),
                },
                {
                    key: 't',
                    value: this.$t('tutor'),
                },
                {
                    key: 's',
                    value: this.$t('student'),
                },
            ];
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
            let result = this.users;

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
    created(){
        this.semesterType = this.getSemesterType();
        this.getSemesters();
    },
    methods:{
        createUser(){
            this.loadingCreateUser = true;
            this.$store.dispatch('createUser', {username: this.username, matrikelnummer: this.matrikelnummer, forename: this.forename, surname: this.surname}).then(response=>{
                this.$bvToast.toast(this.$t('userCreated'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
                this.username = this.matrikelnummer = this.forename = this.surname = undefined;
            }).catch(()=>{
                this.$bvToast.toast(this.$t('userCreatedError'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }).finally(()=>{
                this.loadingCreateUser = false;
            });
        },
        updateCourseUsers(){
            this.loadingCourse_edit_updateUsers = true;
            const id = this.selectedCourse.id;
            const data = this.users.filter(user => user.role !== user.oldRole).map(user =>{
                return {
                    courseId: id,
                    matrikelNummer: user.matrikelNummer,
                    role: user.role
                };
            });
            this.$store.dispatch('updateCourseUsers', data).then(response=>{
                this.$bvToast.toast(this.$t('courseUsersUpdated'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }).catch(()=>{
                this.$bvToast.toast(this.$t('courseUsersUpdatedError'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }).finally(()=>{
                this.loadingCourse_edit_updateUsers = false;
            });
        },
        createSemester(){
            this.loadingCreateSemester = true;
            this.$store.dispatch('createSemester', {year: this.semesterYear, type: this.semesterType}).then(response=>{
                this.$bvToast.toast(this.$t('semesterCreated'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
                this.semesterYear = this.maxYear;
                this.semesterType = this.getSemesterType();
                this.getSemesters();
            }).catch(()=>{
                this.$bvToast.toast(this.$t('semesterCreatedError'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }).finally(()=>{
                this.loadingCreateSemester = false;
            });
        },
        createCourse(){
            this.loadingCourse_create = true;
            this.$store.dispatch('createCourse', 
            {
                semesterId: this.selectedSemester_create,
                number: this.courseNumber_create,
                name: this.courseName_create,
                minKreuzel: this.minKreuzel_create,
                minPoints: this.minPoints_create
            }).then(response=>{
                this.$bvToast.toast(this.$t('courseCreated'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
                this.courseNumber = this.courseName = this.minKreuzel = this.minPoints = undefined;
                this.getCourses(this.selectedSemester_edit);
            }).catch(()=>{
                this.$bvToast.toast(this.$t('courseCreatedError'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }).finally(()=>{
                this.loadingCourse_create = false;
            });
        },
        updateCourse(){
            this.loadingCourse_edit = true;
            this.$store.dispatch('updateCourse', this.selectedCourse).then(response=>{
                this.$bvToast.toast(this.$t('courseUpdated'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }).catch(()=>{
                this.$bvToast.toast(this.$t('courseUpdatedError'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }).finally(()=>{
                this.loadingCourse_edit = false;
            });
        },
        submitFile(){
            this.loadingFileUpload = true;
            const formData = new FormData();
            formData.append('file',this.$refs.file.files[0]);
            this.$refs.file.value = '';
            this.$store.dispatch('createUsers', formData).then(response =>{
                this.$bvToast.toast(this.$t('userCreated'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }).catch(()=>{
                this.$bvToast.toast(this.$t('userCreatedError'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }).finally(()=>{
                this.loadingFileUpload = false;
            });
        },
        getUsers(courseId){
            this.$store.dispatch('getUsers',{courseId}).then(response=>{
                this.users = response.data;
            }).catch(()=>{
                this.$bvToast.toast(this.$t('userGetError'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            });
        },
        getSemesterType(){
            return new Date().getMonth() > 1 && new Date().getMonth() < 9 ? 's' : 'w';
        },
        getSemesters(){
            this.$store.dispatch('getSemesters').then(response =>{
                this.semesters = response.data;
                this.selectedSemester_create = this.selectedSemester_edit = this.semesters[0].id;
                this.getCourses(this.selectedSemester_edit);
            }).catch(()=>{
                this.$bvToast.toast(this.$t('semesterGetError'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            });
        },
        getCourse(courseId){
            this.$store.dispatch('getCourse',{courseId}).then(response =>{
                this.selectedCourse = response.data;
            }).catch(()=>{
                this.$bvToast.toast(this.$t('courseGetError'), {
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
                this.$bvToast.toast(this.$t('coursesGetError'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            });
        },
        deleteCourse(id){
            this.$store.dispatch('deleteCourse',{id}).then(response =>{
                this.$bvToast.toast(this.$t('courseDeleted'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
                this.getCourses(this.selectedSemester_edit);
            }).catch(()=>{
                this.$bvToast.toast(this.$t('courseDeletedError'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            });
        }
    }
}
</script>
