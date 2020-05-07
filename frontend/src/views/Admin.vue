<template>
  <div class="admin">
      <label class="control-label requiredField" style="margin-left: 10px">Pflichtfeld</label>
      <b-tabs content-class="mt-3">
            <b-tab title="Benutzer anlegen" active>
                <div class="row" style="margin-left: 0px">
                    <div class="form-horizontal col-md-4">
                        <form @submit.prevent @submit="createUser()">
                            <div class="form-group">
                                <label for="username" class="control-label required">Benutzername</label> 
                                <input id="username" type="text" class="form-control" v-model="username" required>
                            </div>
                            <div class="form-group">
                                <label for="martikelnummer" class="control-label required">Martikelnummer</label>
                                <input id="martikelnummer" type="text" class="form-control"  pattern="[0-9]{8}" v-model="martikelnummer" title="Achtstellige Nummer" required>
                            </div>
                            <div class="form-group">
                                <label for="surname" class="control-label required">Nachname</label>
                                <input id="surname" type="text" class="form-control" v-model="surname" required>
                            </div>
                            <div class="form-group">
                                <label for="forename" class="control-label required">Vorname</label>
                                <input id="forename" type="text" class="form-control" v-model="forename" required>
                            </div>
                            <div class="form-group">
                                <label for="password" class="control-label required">Passwort</label>
                                <input id="password" type="password" class="form-control" v-model="password" required>
                            </div>
                            <div class="form-inline">
                                <b-button variant="primary" type="submit">Anlegen</b-button>
                                <div class="offset-md-1 form-inline" v-if="loadingCreateUser">
                                    <span class="fa fa-sync fa-spin"></span>
                                    <label class="control-label">Laden...</label>
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="form-horizontal col-md-4 offset-md-1">
                        <div class="form-inline">
                            <label class="btn btn-primary col-md-5 finger">
                                CSV Datei hochladen <input type="file" class="d-none" id="file" ref="file" accept=".csv" @change="submitFile()"/>
                            </label>
                            <div class="offset-md-1 form-inline" v-if="loadingFileUpload">
                                <span class="fa fa-sync fa-spin"></span>
                                <label class="control-label">Laden...</label>
                            </div>
                        </div>
                    </div>
                </div>
            </b-tab>
            <b-tab title="Kurseverwaltung">
                <b-tabs content-class="mt-3">
                    <b-tab title="Anlegen" active>
                        <div class="form-horizontal col-md-4">
                            <form @submit.prevent @submit="createCourse()">
                                <div class="form-group">
                                    <label for="courseSemester_create" class="control-label required">Semester</label>
                                    <select class="form-control" v-model="selectedSemester_create" id="courseSemester_create" required>
                                        <option v-for="semester in semesters" :value="semester.id" :key="semester.id">
                                            {{semester.year}} {{semester.type === 'w' ? 'WS' : 'SS'}}
                                        </option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label for="courseNumber_create" class="control-label required">Nummer</label>
                                    <input id="courseNumber_create" type="text" class="form-control"  pattern="[0-9]{3}\.[0-9]{3}" title="Format: 000.000" v-model="courseNumber_create" required>
                                </div>
                                <div class="form-group">
                                    <label for="courseName_create" class="control-label required">Name</label>
                                    <input id="courseName_create" type="text" class="form-control" v-model="courseName_create" required>
                                </div>
                                <div class="form-group col-md-6" style="padding-left:0px">
                                    <label for="minKreuzel_create" class="control-label">Mindestanforderung Kreuzel (in %)</label>
                                    <i-input id="minKreuzel_create" class="form-control" min="0" max="100" v-model="minKreuzel_create"> </i-input>
                                </div>
                                <div class="form-group col-md-6" style="padding-left:0px">
                                    <label for="minPoints_create" class="control-label">Mindestanforderung Punkte</label>
                                    <i-input id="minPoints_create" class="form-control" min="0" v-model="minPoints_create"> </i-input>
                                </div>
                                <div class="form-inline">
                                    <b-button variant="primary" type="submit">Anlegen</b-button>
                                    <div class="offset-md-1 form-inline" v-if="loadingCourse_create">
                                        <span class="fa fa-sync fa-spin"></span>
                                        <label class="control-label">Laden...</label>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </b-tab>
                    <b-tab title="Bearbeiten">
                        <div class="row col-md-12">
                            <div class="col-md-4">
                                <div class="form-group">
                                    <label for="courseSemester_edit" class="control-label">Semester</label>
                                    <select class="form-control" v-model="selectedSemester_edit" id="courseSemester_edit" @change="getCourses(selectedSemester_edit)">
                                        <option v-for="semester in semesters" :value="semester.id" :key="semester.id">
                                            {{semester.year}} {{semester.type === 'w' ? 'WS' : 'SS'}}
                                        </option>
                                    </select>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <div class="form-group">
                                    <label for="selectedCourse" class="control-label">Kurs</label>
                                    <select class="form-control" v-model="selectedCourse.id" id="selectedCourse" @change="getCourse(selectedSemester_edit, selectedCourse.id); getUsers(selectedCourse.id)">
                                        <option v-for="course in courses" :value="course.id" :key="course.id">
                                            {{course.number}} {{course.name}}
                                        </option>
                                    </select>
                                </div>
                            </div>
                            <div class="col-md-1" style="margin-top: 30px">
                                <b-button variant="danger" v-b-modal="'modal-delete-course'">Löschen</b-button>
                                <b-modal id="modal-delete-course" title="Löschbestätigung" :ok-title="'Ja'" :cancel-title="'Nein'" @ok="deleteCourse(selectedCourse.id)">
                                    Wollen Sie den Kurs wirklich löschen?
                                </b-modal>
                            </div>
                        </div>
                        <div class="row col-md-12" v-if="selectedCourse.id">
                            <div class="form-horizontal col-md-4" style="margin-left: 0px">
                                <form @submit.prevent @submit="updateCourse()">
                                    <div class="form-group">
                                        <label for="courseNumber_edit" class="control-label required">Nummer</label>
                                        <input id="courseNumber_edit" type="text" class="form-control"  pattern="[0-9]{3}\.[0-9]{3}" title="Format: 000.000" v-model="selectedCourse.number" required>
                                    </div>
                                    <div class="form-group">
                                        <label for="courseName_edit" class="control-label required">Name</label>
                                        <input id="courseName_edit" type="text" class="form-control" v-model="selectedCourse.name" required>
                                    </div>
                                    <div class="form-group col-md-6" style="padding-left:0px">
                                        <label for="minKreuzel_edit" class="control-label">Mindestanforderung Kreuzel (in %)</label>
                                        <i-input id="minKreuzel_edit" class="form-control" min="0" max="100" v-model="selectedCourse.minKreuzel"> </i-input>
                                    </div>
                                    <div class="form-group col-md-6" style="padding-left:0px">
                                        <label for="minPoints_edit" class="control-label">Mindestanforderung Punkte</label>
                                        <i-input id="minPoints_edit" class="form-control" min="0" v-model="selectedCourse.minPoints"> </i-input>
                                    </div>
                                    <div class="form-inline">
                                        <b-button variant="primary" type="submit">Aktualisieren</b-button>
                                        <div class="offset-md-1 form-inline" v-if="loadingCourse_edit">
                                            <span class="fa fa-sync fa-spin"></span>
                                            <label class="control-label">Laden...</label>
                                        </div>
                                    </div>
                                </form>
                            </div>
                            <div class="form-horizontal col-md-7 offset-md-1">
                                <div class="form-group">
                                    <label for="searchUserText" class="control-label">Suche</label>
                                    <input id="searchUserText" type="text" class="form-control" v-model="searchUserText">
                                </div>
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input" type="checkbox" id="showCheckedUsers" :value="checkedUsersView" @click="checkedUsersView = !checkedUsersView">
                                    <label class="form-check-label" for="showCheckedUsers">Nur zugeteilte Benutzer anzeigen</label>
                                </div>
                                <table class="table">
                                    <thead>
                                        <th></th>
                                        <th>Martikelnummer</th>
                                        <th>Nachname</th>
                                        <th>Vorname</th>
                                        <th>Rolle</th>
                                    </thead>
                                    <tbody>
                                        <tr v-for="user in filteredUsers" :key="user.martikelnummer">
                                            <td>
                                                <!-- <input type="checkbox" class="form-check-input" id="showCheckedUsers" :value="!!user.role[selectedCourse]" @click="user.role[selectedCourse] = undefined"> -->
                                            </td>
                                            <td>
                                                {{user.martikelnummer}}
                                            </td>
                                            <td>
                                                {{user.surname}}
                                            </td>
                                            <td>
                                                {{user.forename}}
                                            </td>
                                            <td>
                                                <!-- {{user.role[selectedCourse]}} -->
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                            
                        </div>
                    </b-tab>
                </b-tabs>
            </b-tab>
            <b-tab title="Semester anlegen">
                <div class="form-horizontal col-md-4">
                    <form @submit.prevent @submit="createSemester()">
                        <div class="form-group">
                            <label for="semesterYear" class="control-label required">Jahr</label>
                            <i-input id="semesterYear" class="form-control"  :max="maxYear" :min="maxYear-100" v-model="semesterYear" required></i-input>
                        </div>
                        <div class="form-check">
                            <input id="semesterTypeSummer" type="radio" value="s" class="form-check-input"  v-model="semesterType">
                            <label for="semesterTypeSummer" class="form-check-label">
                                Sommersemester
                            </label>
                        </div>
                        <div class="form-check">
                            <input id="semesterTypeWinter" type="radio" value="w" class="form-check-input"  v-model="semesterType">
                            <label for="semesterTypeWinter"   class="form-check-label" style="margin-right:15px">
                                Wintersemester
                            </label>
                        </div>
                        <div class="form-inline">
                            <b-button variant="primary" type="submit">Anlegen</b-button>
                            <div class="offset-md-1 form-inline" v-if="loadingCreateSemester">
                                <span class="fa fa-sync fa-spin"></span>
                                <label class="control-label">Laden...</label>
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
            password: undefined,
            martikelnummer: undefined,
            forename: undefined,
            surname: undefined,
            checkedUsersView: false,
            loadingFileUpload: false,
            loadingCreateUser: false,
            loadingCreateSemester: false,
            loadingCourse_create: false,
            loadingCourse_edit: false,
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
            selectedCourse: {},
            courses: [],
            searchUserText: undefined,
            users: []
        }
    },
    computed:{
        filteredUsers(){
            let result = [];
            if(this.checkedUsersView){
                if(this.searchUserText){
                    result = this.users.filter(user => user.role[this.selectedCourse.id] &&
                                                            (user.martikelnummer.indexOf(this.searchUserText) > -1
                                                            || user.surname.indexOf(this.searchUserText) > -1
                                                            || user.forename.indexOf(this.searchUserText) > -1));
                }
                else{
                    result = this.users.filter(user => user.role[this.selectedCourse.id]);
                }
            }
            else {
                if(this.searchUserText){
                    result = this.users.filter(user => user.martikelnummer.indexOf(this.searchUserText) > -1
                                                    || user.surname.indexOf(this.searchUserText) > -1
                                                    || user.forename.indexOf(this.searchUserText) > -1);
                }
                else{
                    result = this.users;
                }
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
            this.$store.dispatch("createUser", {username: this.username, password: this.password, martikelnummer: this.martikelnummer, forename: this.forename, surname: this.surname}).then(response=>{
                this.loadingCreateUser = false;
                this.$bvToast.toast(`Benutzer wurde angelegt`, {
                    title: 'Erfolg',
                    variant: 'success',
                    appendToast: true
                });
                this.username = this.password = this.martikelnummer = this.forename = this.surname = undefined;
            }).catch(()=>{
                this.loadingCreateUser = false;
                this.$bvToast.toast(`Benutzer konnte nicht angelegt werden`, {
                    title: 'Fehler',
                    variant: 'danger',
                    appendToast: true
                });
            });
        },
        createSemester(){
            this.loadingCreateSemester = true;
            this.$store.dispatch("createSemester", {year: this.semesterYear, type: this.semesterType}).then(response=>{
                this.loadingCreateSemester = false;
                this.$bvToast.toast(`Semester wurde angelegt`, {
                    title: 'Erfolg',
                    variant: 'success',
                    appendToast: true
                });
                this.semesterYear = this.maxYear;
                this.semesterType = this.getSemesterType();
                this.getSemesters();
            }).catch(()=>{
                this.loadingCreateSemester = false;
                this.$bvToast.toast(`Semester konnte nicht angelegt werden`, {
                    title: 'Fehler',
                    variant: 'danger',
                    appendToast: true
                });
            });
        },
        createCourse(){
            this.loadingCourse_create = true;
            this.$store.dispatch("createCourse", 
            {
                semesterId: this.selectedSemester_create,
                number: this.courseNumber_create,
                name: this.courseName_create,
                minKreuzel: this.minKreuzel_create,
                minPoints: this.minPoints_create
            }).then(response=>{
                this.loadingCourse_create = false;
                this.$bvToast.toast(`Kurs wurde angelegt`, {
                    title: 'Erfolg',
                    variant: 'success',
                    appendToast: true
                });
                this.courseNumber = this.courseName = this.minKreuzel = this.minPoints = undefined;
                this.getCourses(this.selectedSemester_edit);
            }).catch(()=>{
                this.loadingCourse_create = false;
                this.$bvToast.toast(`Kurs konnte nicht angelegt werden`, {
                    title: 'Fehler',
                    variant: 'danger',
                    appendToast: true
                });
            });
        },
        updateCourse(){
            this.loadingCourse_edit = true;
            this.$store.dispatch("updateCourse", this.selectedCourse).then(response=>{
                this.loadingCourse_edit = false;
                this.$bvToast.toast(`Kurs wurde aktualisiert`, {
                    title: 'Erfolg',
                    variant: 'success',
                    appendToast: true
                });
            }).catch(()=>{
                this.loadingCourse_edit = false;
                this.$bvToast.toast(`Kurs konnte nicht aktualisiert werden`, {
                    title: 'Fehler',
                    variant: 'danger',
                    appendToast: true
                });
            });
        },
        submitFile(){
            this.loadingFileUpload = true;
            const formData = new FormData();
            formData.append('file',this.$refs.file.files[0]);
            this.$refs.file.value = '';
            this.$store.dispatch("createUsers", formData).then(response =>{
                this.loadingFileUpload = false;
                this.$bvToast.toast(`Benutzer wurden angelegt`, {
                    title: 'Erfolg',
                    variant: 'success',
                    appendToast: true
                });
            }).catch(()=>{
                this.loadingFileUpload = false;
                this.$bvToast.toast(`Benutzer konnten nicht angelegt werden`, {
                    title: 'Fehler',
                    variant: 'danger',
                    appendToast: true
                });
            })
        },
        getUsers(courseId){
            this.$store.dispatch("getUsers",{courseId}).then(response=>{
                this.users = response.data;
            }).catch(()=>{
                this.$bvToast.toast(`Benutzer konnten nicht geladen werden`, {
                    title: 'Fehler',
                    variant: 'danger',
                    appendToast: true
                });
            });
        },
        getSemesterType(){
            return new Date().getMonth() > 1 && new Date().getMonth() < 9 ? 's' : 'w';
        },
        getSemesters(){
            this.$store.dispatch("getSemesters").then(response =>{
                this.semesters = response.data;
                this.selectedSemester_create = this.selectedSemester_edit = this.semesters[0].id;
                this.getCourses(this.selectedSemester_edit);
            }).catch(()=>{
                this.$bvToast.toast(`Semester konnten nicht geladen werden`, {
                    title: 'Fehler',
                    variant: 'danger',
                    appendToast: true
                });
            });
        },
        getCourse(semesterId, courseId){
            this.$store.dispatch("getCourse",{semesterId, courseId}).then(response =>{
                this.selectedCourse = response.data;
            }).catch(()=>{
                this.$bvToast.toast(`Kurs konnte nicht geladen werden`, {
                    title: 'Fehler',
                    variant: 'danger',
                    appendToast: true
                });
            });
        },
        getCourses(id){
            this.$store.dispatch("getCourses",{id}).then(response =>{
                this.courses = response.data;
                this.selectedCourse = this.courses[0];
            }).catch(()=>{
                this.$bvToast.toast(`Kurse konnten nicht geladen werden`, {
                    title: 'Fehler',
                    variant: 'danger',
                    appendToast: true
                });
            });
        },
        deleteCourse(id){
            this.$store.dispatch("deleteCourse",{id}).then(response =>{
                
            }).catch(()=>{
                this.$bvToast.toast(`Kurse konnte nicht gelöscht werden`, {
                    title: 'Fehler',
                    variant: 'danger',
                    appendToast: true
                });
            });
        }
    }
}
</script>
