<template>
  <div class="account">
      <b-tabs content-class="mt-3">
            <b-tab title="Benutzer anlegen" active v-if="$store.getters.userInfo.isAdmin">
                <div class="row" style="margin-left: 0px">
                    <div class="form-horizontal col-md-4">
                        <div class="requiredField">
                            <label class="control-label">Pflichtfeld</label>
                        </div>
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
            <b-tab title="Kurse anlegen" active v-if="$store.getters.userInfo.isAdmin">
            </b-tab>
            <b-tab title="Semester anlegen" active v-if="$store.getters.userInfo.isAdmin">
                <div class="form-horizontal col-md-4">
                    <form @submit.prevent @submit="createSemester()">
                        <div class="form-group">
                            <label for="semesterYear" class="control-label required">Jahr</label>
                            <input id="semesterYear" type="number" class="form-control"  :max="maxYear" :min="maxYear-100" v-model="semesterYear" required>
                        </div>
                        <div class="form-group">
                            <label for="semesterTypeWinter"   class="control-label" style="margin-right:15px">
                                <input id="semesterTypeWinter" type="radio" value="w" class="form-control"  v-model="semesterType">
                                Wintersemester
                            </label>
                            <label for="semesterTypeSummer" class="control-label">
                                <input id="semesterTypeSummer" type="radio" value="s" class="form-control"  v-model="semesterType">
                                Sommersemester
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
        semesterYear: new Date().getFullYear(),
        maxYear: new Date().getFullYear(),
        currentMonth: new Date().getMonth(),
        semesterType: new Date().getMonth() > 1 && new Date().getMonth() < 9 ? 's' : 'w'
    }
  },
  methods:{
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
    }
  }
}
</script>
