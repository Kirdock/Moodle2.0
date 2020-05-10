import Vue from 'vue';
import VueI18n from 'vue-i18n';
Vue.use(VueI18n);

const messages = {
    'de': {
        account: 'Konto',
        admin: 'Admin',
        changeLanguageError: 'Die Sprache konnte nicht geändert werden',
        confirmDeletion: 'Löschbestätigung',
        courseDeleteQuestion: 'Wollen Sie die Lehrveranstaltung wirklich löschen?',
        courseManagement: 'Lehrveranstaltungen verwalten',
        course: 'Lehrveranstaltung',
        courseCreated: 'Lehrveranstaltung wurde angelegt',
        courseCreatedError: 'Lehrveranstaltung konnte nicht angelegt werden',
        courseDeleted: 'Lehrveranstaltung gelöscht',
        courseDeletedError: 'Lehrveranstaltung konnte nicht gelöscht werden',
        courseGetError: 'Lehrveranstaltungen konnte nicht geladen werden',
        courses: 'Lehrveranstaltungen',
        coursesGetError: 'Lehrveranstaltungen konnten nicht geladen werden',
        courseUpdated: 'Lehrveranstaltung wurde aktualisiert',
        courseUpdatedError: 'Lehrveranstaltung konnte nicht aktualisiert werden',
        create: 'Anlegen',
        createSemester: 'Semester anlegen',
        createUser: 'Benutzer anlegen',
        delete: 'Löschen',
        edit: 'Bearbeiten',
        eightDigitNumber: 'Achtstellige Nummer',
        error: 'Fehler',
        forename: 'Vorname',
        format: 'Format',
        home: 'Home',
        loading: 'Laden',
        login: 'Anmelden',
        loggedInAs: 'Angemeldet als',
        logout: 'Abmelden',
        matrikelnummer: 'Matrikelnummer',
        minRequireKreuzel: 'Mindestanforderung Kreuzel (in %)',
        minRequirePoints: 'Mindestanforderung Punkte',
        name: 'Name',
        no: 'Nein',
        number: 'Nummer',
        onlyShowAssignedUsers: 'Nur zugeteilte Benutzer anzeigen',
        password: 'Passwort',
        requiredField: 'Pflichtfeld',
        role: 'Rolle',
        search: 'Suche',
        semester: 'Semester',
        semesterCreated: 'Semester wurde angelegt',
        semesterCreatedError: 'Semester konnte nicht angelegt werden',
        semesterGetError: 'Semester konnten nicht geladen werden',
        success: 'Erfolg',
        summerSemester: 'Sommersemester',
        summerSemesterShortcut: 'SS',
        surname: 'Nachname',
        uploadCSV: 'CSV Datei hochladen',
        update: 'Aktualisieren',
        userCreated: 'Benutzer wurde angelegt',
        userCreatedError: 'Benutzer konnte nicht angelegt werden',
        userGetError: 'Benutzer konnten nicht geladen werden',
        username: 'Benutzername',
        userPwdInvalid: 'Benutzername oder Passwort stimmt nicht überein',
        welcome: 'Willkommen',
        winterSemester: 'Wintersemester',
        winterSemesterShortcut: 'WS',
        year: 'Jahr',
        yes: 'Ja'
    },
    'en': {
        account: 'Account',
        admin: 'Admin',
        changeLanguageError: 'Language could not be changed',
        confirmDeletion: 'Confirmation of deletion',
        courseDeleteQuestion: 'Do you really want to delete this course?',
        courseManagement: 'Course management',
        course: 'Course',
        courseCreated: 'Course created',
        courseCreatedError: 'Course could not be created',
        courseDeleted: 'Course deleted',
        courseDeletedError: 'Course could not be deleted',
        courseGetError: 'Course could not be loaded',
        courses: 'Courses',
        coursesGetError: 'Courses could not be loaded',
        courseUpdated: 'Course updated',
        courseUpdatedError: 'Course could not be updated',
        create: 'Create',
        createSemester: 'Create Semester',
        createUser: 'Create user',
        delete: 'delete',
        edit: 'Edit',
        eightDigitNumber: 'Eight-digit number',
        error: 'Error',
        forename: 'Forename',
        format: 'Format',
        home: 'Home',
        loading: 'Loading',
        login: 'Login',
        loggedInAs: 'Logged in as',
        logout: 'Logout',
        matrikelnummer: 'Matrikelnummer',
        minRequireKreuzel: 'Minimum requirement of kreuzel (in %)',
        minRequirePoints: 'Minimum requirement of points',
        name: 'Name',
        no: 'No',
        number: 'Number',
        onlyShowAssignedUsers: 'Only show assigned users',
        password: 'Password',
        requiredField: 'Required field',
        role: 'Role',
        search: 'Search',
        semester: 'Semester',
        semesterCreated: 'Semester created',
        semesterCreatedError: 'Semester could not be created',
        semesterGetError: 'Semester could not be loaded',
        success: 'Success',
        summerSemester: 'Summer semester',
        summerSemesterShortcut: 'SS',
        surname: 'Surname',
        uploadCSV: 'Upload CSV file',
        update: 'Update',
        userCreated: 'User created',
        userCreatedError: 'User could not be created',
        userGetError: 'User could not be loaded',
        username: 'Username',
        userPwdInvalid: 'Username or password do not match',
        welcome: "Welcome",
        winterSemester: 'Winter semester',
        winterSemesterShortcut: 'WS',
        year: 'Year',
        yes: 'Yes'
    }
};

const i18n = new VueI18n({
    fallbackLocale: 'en', // set fallback locale
    messages, // set locale messages
});

export default i18n;