<template>
  <div class="account">
      <b-tabs content-class="mt-3">
            <b-tab title="Benutzer anlegen" active v-if="$store.getters.userInfo.isAdmin">
                <div class="row">
                    <div class="form-horizontal col-md-4">
                        <div class="requiredField">
                            <label class="control-label">Pflichtfeld</label>
                        </div>
                        <form @submit.prevent @submit="submit()">
                            <div class="form-group required">
                                <label for="username" class="control-label required">Benutzername</label> 
                                <input id="username" type="text" class="form-control" v-model="username" required>
                            </div>
                            <div class="form-group required">
                                <label for="martikelnummer" class="control-label">Martikelnummer</label>
                                <input id="martikelnummer" type="text" class="form-control"  pattern="[0-9]{8}" v-model="martikelnummer" title="Achtstellige Nummer" required>
                            </div>
                            <div class="form-group required">
                                <label for="surname" class="control-label">Nachname</label>
                                <input id="surname" type="text" class="form-control" v-model="surname" required>
                            </div>
                            <div class="form-group required">
                                <label for="forename" class="control-label">Vorname</label>
                                <input id="forename" type="text" class="form-control" v-model="forename" required>
                            </div>
                            <div class="form-group required">
                                <label for="password" class="control-label">Passwort</label>
                                <input id="password" type="password" class="form-control" v-model="password" required>
                            </div>
                            <div class="form-group">
                                <button class="btn btn-primary" type="submit">Anlegen</button>
                                <div class="loader" v-if="loadingCreateUser"></div>
                            </div>
                        </form>
                    </div>
                    <div class="form-horizontal col-md-4 offset-md-1">
                        <label class="btn btn-primary col-md-5 finger">
                            CSV Datei hochladen <input type="file" style="display:none" id="file" ref="file" accept=".csv" @change="submitFile()"/>
                        </label>
                        <div class="loader" v-if="loadingFileUpload"></div>
                    </div>
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
        loadingFileUpload: undefined,
        loadingCreateUser: undefined
    }
  },
  methods:{
    submit(){
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
