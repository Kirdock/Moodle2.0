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
                                <button class="btn btn-primary" type="submit">Anlegen</button>
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
            <b-tab title="Kurse anlegen">
                <div class="form-horizontal col-md-4">
                    <form @submit.prevent @submit="createCourse()">
                        <div class="form-group">
                            <label for="courseSemester" class="control-label required">Semester</label>
                            <select class="form-control" v-model="selectedSemester" id="courseSemester">
                                <option v-for="semester in semesters" :value="semester.id" :key="semester.id">
                                    {{semester.year}} {{semester.type === 'w' ? 'WS' : 'SS'}}
                                </option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="courseNumber" class="control-label required">Nummer</label>
                            <input id="courseNumber" type="text" class="form-control"  pattern="[0-9]{3}\.[0-9]{3}" title="Format: 000.000" v-model="courseNumber" required>
                        </div>
                        <div class="form-group">
                            <label for="courseName" class="control-label required">Name</label>
                            <input id="courseName" type="text" class="form-control" v-model="courseName" required>
                        </div>
                        <div class="form-group col-md-6" style="padding-left:0px">
                            <label for="minKreuzel" class="control-label">Mindestanforderung Kreuzel (in %)</label>
                            <nc-input id="minKreuzel" class="form-control" min="0" max="100" v-model="minKreuzel"> </nc-input>
                        </div>
                        <div class="form-group col-md-6" style="padding-left:0px">
                            <label for="minPoints" class="control-label">Mindestanforderung Punkte</label>
                            <nc-input id="minPoints" class="form-control" min="0" v-model="minPoints"> </nc-input>
                        </div>
                        <div class="form-inline">
                            <button class="btn btn-primary" type="submit">Anlegen</button>
                            <div class="offset-md-1 form-inline" v-if="loadingCreateCourse">
                                <span class="fa fa-sync fa-spin"></span>
                                <label class="control-label">Laden...</label>
                            </div>
                        </div>
                    </form>
                </div>
            </b-tab>
            <b-tab title="Semester anlegen">
                <div class="form-horizontal col-md-4">
                    <form @submit.prevent @submit="createSemester()">
                        <div class="form-group">
                            <label for="semesterYear" class="control-label required">Jahr</label>
                            <nc-input id="semesterYear" class="form-control"  :max="maxYear" :min="maxYear-100" v-model="semesterYear" required></nc-input>
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
                            <button class="btn btn-primary" type="submit">Anlegen</button>
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
        loadingFileUpload: false,
        loadingCreateUser: false,
        loadingCreateSemester: false,
        loadingCreateCourse: false,
        semesterYear: new Date().getFullYear(),
        maxYear: new Date().getFullYear(),
        semesterType: undefined,
        semesters: [],
        selectedSemester: undefined,
        courseNumber: undefined,
        courseName: undefined,
        minKreuzel: undefined,
        minPoints: undefined
    }
  },
  created(){
    this.semesterType = this.getSemesterType();
    this.getSemesters();
  },
  methods:{
    filterKey(e){ //onInput
    //TO-DO: No-Comma-Input-Component
        if (e.key === ',' || e.key === '.'){
            return e.preventDefault();
        }
    },
    preventComma(e){ //onPaste
        e.target.value = e.target.value.split(/\.|\,/)[0];
    },
    createUser(){
        this.loadingCreateUser = true;
        this.$store.dispatch("createUser", {username: this.username, password: this.password, martikelnummer: this.martikelnummer, forename: this.forename, surname: this.surname}).then(response=>{
            this.loadingCreateUser = false;
            this.$bvToast.toast(`Benutzer wurde angelegt`, {
                title: 'Erfolg',
                autoHideDelay: this.$store.getters.toastDelay,
                variant: 'success',
                appendToast: true
            });
            this.username = this.password = this.martikelnummer = this.forename = this.surname = '';
        }).catch(()=>{
            this.loadingCreateUser = false;
            this.$bvToast.toast(`Benutzer konnte nicht angelegt werden`, {
                title: 'Fehler',
                autoHideDelay: this.$store.getters.toastDelay,
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
                autoHideDelay: this.$store.getters.toastDelay,
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
                autoHideDelay: this.$store.getters.toastDelay,
                variant: 'danger',
                appendToast: true
            });
        });
    },
    createCourse(){
        this.loadingCreateCourse = true;
        this.$store.dispatch("createCourse", 
        {
            semesterId: this.selectedSemester,
            number: this.courseNumber,
            name: this.courseName,
            minKreuzel: this.minKreuzel,
            minPoints: this.minPoints
        }).then(response=>{
            this.loadingCreateCourse = false;
            this.$bvToast.toast(`Kurs wurde angelegt`, {
                title: 'Erfolg',
                autoHideDelay: this.$store.getters.toastDelay,
                variant: 'success',
                appendToast: true
            });
            this.courseNumber = this.courseName = this.minKreuzel = this.minPoints = undefined;
        }).catch(()=>{
            this.loadingCreateCourse = false;
            this.$bvToast.toast(`Kurs konnte nicht angelegt werden`, {
                title: 'Fehler',
                autoHideDelay: this.$store.getters.toastDelay,
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
                autoHideDelay: this.$store.getters.toastDelay,
                variant: 'success',
                appendToast: true
            });
        }).catch(()=>{
            this.loadingFileUpload = false;
            this.$bvToast.toast(`Benutzer konnten nicht angelegt werden`, {
                title: 'Fehler',
                autoHideDelay: this.$store.getters.toastDelay,
                variant: 'danger',
                appendToast: true
            });
        })
    },
    getSemesterType(){
        return new Date().getMonth() > 1 && new Date().getMonth() < 9 ? 's' : 'w';
    },
    getSemesters(){
        this.$store.dispatch("getSemesters").then(response =>{
            this.semesters = response.data;
            this.selectedSemester = this.semesters[0].id;
        }).catch(()=>{
            this.$bvToast.toast(`Semester konnten nicht geladen werden`, {
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
