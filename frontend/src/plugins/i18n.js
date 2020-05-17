import Vue from 'vue';
import VueI18n from 'vue-i18n';
Vue.use(VueI18n);

const messages = {
    'de': {
        account: 'Konto',
        actions: 'Aktionen',
        admin: 'Admin',
        all: 'Alle',
        assigned: 'Zugeteilt',
        cancel: 'Abbrechen',
        change: 'Änderung',
        changeLanguageError: 'Die Sprache konnte nicht geändert werden',
        confirm: 'Bestätigen',
        copy: 'Kopieren',
        course: {
            management: 'Lehrveranstaltungen verwalten',
            name: 'Lehrveranstaltung',
            copied: 'Die Lehrveranstaltung wurde erfolgreich kopiert',
            created: 'Lehrveranstaltung wurde angelegt',
            deleted: 'Lehrveranstaltung gelöscht',
            usersUpdated: 'Die Rollen wurden aktualisiert',
            courses: 'Lehrveranstaltungen',
            updated: 'Lehrveranstaltung wurde aktualisiert',
            
            error: {
                copy: 'Die Lehrveranstaltung konnte nicht kopiert werden',
                create: 'Lehrveranstaltung konnte nicht angelegt werden',
                delete: 'Lehrveranstaltung konnte nicht gelöscht werden',
                get: 'Lehrveranstaltung konnte nicht geladen werden',
                update: 'Lehrveranstaltung konnte nicht aktualisiert werden',
                usersUpdated: 'Die Rollen konnten nicht aktualisiert werden'
            },
            errors: {
                get: 'Lehrveranstaltungen konnten nicht geladen werden',
            },
            question: {
                copy: 'In welches Semester soll die Lehrveranstaltung kopiert werden?',
                delete: 'Wollen Sie die Lehrveranstaltung wirklich löschen?'
            },
            title: {
                create: 'Neue Lehrveranstaltung'
            }
        },
        create: 'Anlegen',
        delete: 'Löschen',
        edit: 'Bearbeiten',
        eightDigitNumber: 'Achtstellige Nummer',
        error: 'Fehler',
        exerciseSheet: {
            name: 'Übungsblatt',
            created: 'Übungsblatt wurde angelegt',
            updated: 'Übungsblatt wurde aktualisiert',
            deleted: 'Übungsblatt wurde gelöscht',
            error: {
                create: 'Übungsblatt konnte nicht angelegt werden',
                get: 'Übungsblatt konnte nicht geladen werden',
                update: 'Übungsblatt konnte nicht aktualisiert werden',
                delete: 'Übungsblatt konnte nicht gelöscht werden'
            },
            title: {
                create: 'Neues Übungsblatt'
            },
            question: {
                delete: 'Wollen Sie das Übungsblatt wirklich löschen?'
            }
        },
        exerciseSheets: {
            name: 'Übungsblätter',
            error: {
                get: 'Übungsblätter konnten nicht geladen werden',
            }
        },
        forename: 'Vorname',
        format: 'Format',
        home: 'Home',
        information: 'Information',
        lecturer: 'Lehrende/r',
        loading: 'Laden',
        login: 'Anmelden',
        loggedInAs: 'Angemeldet als',
        logout: 'Abmelden',
        matrikelnummer: 'Matrikelnummer',
        minRequireKreuzel: 'Mindestanforderung Kreuzel (in %)',
        minRequirePoints: 'Mindestanforderung Punkte (in %)',
        name: 'Name',
        new: 'Neu',
        no: 'Nein',
        number: 'Nummer',
        password: 'Passwort',
        passwordDontMatch: 'Passwörter stimmen nicht überein',
        passwordNew: 'Neues Passwort',
        passwordNewConfirm: 'Neues Passwort bestätigen',
        passwordOld: 'Altes Passwort',
        passwordUpdated: 'Passwort wurde aktualisiert',
        passwordUpdatedError: 'Passwort konnte nicht aktualisiert werden',
        requiredField: 'Pflichtfeld',
        role: 'Rolle',
        search: 'Suche',
        semester: {
            create: 'Semester anlegen',
            semester: 'Semester',
            created: 'Semester wurde angelegt',
            management: 'Semesterverwaltung',
            summer: 'Sommersemester',
            summerShortcut: 'SS',
            winter: 'Wintersemester',
            winterShortcut: 'WS',
            error: {
                created: 'Semester konnte nicht angelegt werden',
                get: 'Semester konnten nicht geladen werden',
            }
        },
        sessionExpired: 'Sitzung ist abgelaufen',
        settings: 'Einstellungen',
        show: 'Anzeigen',
        student: 'Student',
        submissionDate: 'Abgabedatum',
        success: 'Erfolg',
        surname: 'Nachname',
        tutor: 'Tutor',
        uploadCSV: 'CSV Datei hochladen',
        update: 'Aktualisieren',
        user: {
            assigned: 'Zugeteilte Benutzer',
            user: 'Benutzer',
            created: 'Benutzer wurde angelegt',
            users: 'Benutzer',
            deleted: 'Benutzer wurde gelöscht',
            management: 'Benutzerverwaltung',
            error: {
                create: 'Benutzer konnte nicht angelegt werden',
                delete: 'Benutzer konnte nicht gelöscht werden',
                get: 'Benutzer konnten nicht geladen werden'
            },
            question: {
                delete: 'Wollen Sie den Benutzer wirklich löschen?'
            }
        },
        userPwdInvalid: 'Benutzername oder Passwort stimmt nicht überein',
        username: 'Benutzername',
        warning: 'Warnung',
        welcome: 'Willkommen',
        year: 'Jahr',
        yes: 'Ja',
        title: {
            delete: 'Löschbestätigung'
        }
    },
    'en': {
        account: 'Account',
        actions: 'Actions',
        admin: 'Admin',
        all: 'All',
        assigned: 'Assigned',
        cancel: 'cancel',
        change: 'Change',
        changeLanguageError: 'Language could not be changed',
        confirm: 'Confirm',
        copy: 'Copy',
        course:{
            copied: 'Course copied',
            name: 'Course',
            courses: 'Courses',
            management: 'Course Management',
            created: 'Course created',
            deleted: 'Course deleted',
            updated: 'Course updated',
            question:{
                delete: 'Do you really want to delete this course?',
                copy: 'In which semester should the course be copied?'
            },
            error:{
                copy: 'Course could not be copied',
                create: 'Course could not be created',
                delete: 'Course could not be deleted',
                get: 'Course could not be loaded',
                usersUpdated: 'Roles could not be updated',
                update: 'Course could not be updated'
            },
            errors: {
                get: 'Courses could not be loaded',
            },
            title: {
                create: 'New course'
            }
        },
        create: 'Create',
        delete: 'Delete',
        edit: 'Edit',
        eightDigitNumber: 'Eight-digit number',
        error: 'Error',
        exerciseSheet: {
            name: 'Exercise sheet',
            create: 'Exercise sheet created',
            updated: 'Exercise sheet updated',
            delete: 'Exercise sheet deleted',
            error: {
                create: 'Exercise sheet could not be created',
                get: 'Exercise sheet could not be loaded',
                update: 'Exercise sheet could lnto be updated',
                delete: 'Exercise sheet could not be deleted'
            },
            title: {
                create: 'New exercise sheet'
            },
            question: {
                delete: 'Do you really want to delete this exercise sheet?'
            }
        },
        exerciseSheets: {
            name: 'Exercise sheets',
            error: {
                get: 'Exercise sheets could not be loaded',
            }
        },
        forename: 'Forename',
        format: 'Format',
        home: 'Home',
        information: 'Information',
        lecturer: 'Lecturer',
        loading: 'Loading',
        login: 'Login',
        loggedInAs: 'Logged in as',
        logout: 'Logout',
        matrikelnummer: 'Matrikelnummer',
        minRequireKreuzel: 'Minimum requirement of kreuzel (in %)',
        minRequirePoints: 'Minimum requirement of points (in %)',
        name: 'Name',
        new: 'New',
        no: 'No',
        number: 'Number',
        password: 'Password',
        passwordDontMatch: 'Passwords do not match',
        passwordNew: 'New password',
        passwordNewConfirm: 'Confirm new password',
        passwordOld: 'Old Password',
        passwordUpdated: 'Password updated',
        passwordUpdatedError: 'Password could not be updated',
        requiredField: 'Required field',
        role: 'Role',
        search: 'Search',
        semester: {
            create: 'Create Semester',
            semester: 'Semester',
            created: 'Semester created',
            management: 'Semester management',
            summer: 'Summer semester',
            summerShortcut: 'SS',
            winter: 'Winter semester',
            winterShortcut: 'WS',
            error:{
                get: 'Semester could not be loaded',
                create: 'Semester could not be created',
            }
        },
        sessionExpired: 'Session expired',
        settings: 'Settings',
        show: 'Show',
        student: 'Student',
        submissionDate: 'Submission date',
        success: 'Success',
        surname: 'Surname',
        tutor: 'Tutor',
        uploadCSV: 'Upload CSV file',
        update: 'Update',
        user: {
            assigned: 'Assigned users',
            user: 'User',
            users: 'Users',
            created: 'User created',
            deleted: 'User deleted',
            management: 'User Management',
            question: {
                delete: 'Do you really want to delete this user?',
            },
            error: {
                create: 'User could not be created',
                delete: 'User could not be deleted',
                get: 'User could not be loaded'
            }
        },
        userPwdInvalid: 'Username or password do not match',
        username: 'Username',
        warning: 'Warning',
        welcome: "Welcome",
        year: 'Year',
        yes: 'Yes',
        title: {
            delete: 'Confirmation of deletion'
        }
    }
};

const i18n = new VueI18n({
    fallbackLocale: 'en', // set fallback locale
    messages, // set locale messages
});

export default i18n;