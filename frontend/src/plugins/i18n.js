import Vue from 'vue';
import VueI18n from 'vue-i18n';
Vue.use(VueI18n);

const messages = {
    'de': {
        account: 'Konto',
        actions: 'Aktionen',
        addTag: 'Neuen Dateityp hinzufügen',
        admin: 'Admin',
        all: 'Alle',
        assigned: 'Zugeteilt',
        attachment: {
            saved: 'Anhang wurde gespeichert',
            error: {
                get: 'Ahnang konnte nicht geladen werden',
                save: 'Anhang konnte nicht gespeichert werden'
            }
        },
        attempts: 'Versuche',
        attendance: {
            list: 'Anwesenheitsliste',
            error: {
                get: 'Die Anwesenheitsliste konnte nicht geladen werden'
            }
        },
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
            usersSaved: 'Die Rollen wurden gespeichert',
            saved: 'Lehrveranstaltung wurde gespeichert',
            presetsUpdated: 'Voreinstellungen wurden gespeichert',
            error: {
                copy: 'Die Lehrveranstaltung konnte nicht kopiert werden',
                create: 'Lehrveranstaltung konnte nicht angelegt werden',
                delete: 'Lehrveranstaltung konnte nicht gelöscht werden',
                duplicate: 'In diesem Semester existiert bereits eine Lehrveranstaltung mit dieser Nummer',
                get: 'Lehrveranstaltung konnte nicht geladen werden',
                save: 'Lehrveranstaltung konnte nicht gespeichert werden',
                presetsUpdate: 'Voreinstellungen konnten nicht gespeichert werden',
                usersSave: 'Die Rollen konnten nicht gespeichert werden',
            },
            question: {
                copy: 'In welches Semester soll die Lehrveranstaltung kopiert werden?',
                delete: 'Wollen Sie die Lehrveranstaltung wirklich löschen?'
            },
            title: {
                create: 'Neue Lehrveranstaltung'
            }
        },
        courses: {
            name: 'Lehrveranstaltungen',
            error: {
                get: 'Lehrveranstaltungen konnten nicht geladen werden'
            }
        },
        create: 'Anlegen',
        date: 'Datum',
        deadlineReached: 'Das Abgabeende wurde bereits überschritten',
        delete: 'Löschen',
        description: 'Beschreibung',
        descriptionExerciseSheets: 'Standardbeschreibung für Übungsblätter',
        download: 'Herunterladen',
        edit: 'Bearbeiten',
        eightDigitNumber: 'Achtstellige Nummer',
        email: 'Email',
        error: 'Fehler',
        example: {
            created: 'Beispiel wurde erstellt',
            deleted: 'Beispiel wurde gelöscht',
            name: 'Beispiel',
            new: 'Neues Beispiel',
            saved: 'Beispiel wurde gespeichert',
            error: {
                create: 'Beispiel konnte nicht angelegt werden',
                duplicate: 'Der Name des Beispiels ist bereits vergeben',
                delete: 'Beispiel konnte nicht gelöscht werden',
                save: 'Beispiel konnte nicht gespeichert werden'
            },
            question: {
                delete: 'Wollen Sie das Beispiel wirklich löschen?'
            }
        },
        examples: 'Beispiele',
        exampleUploadLimit: 'Hochladelimit bei Beispielen',
        reason: 'Begründung',
        subExample: {
            name: 'Unterbeispiel',
            new: 'Neues Unterbeispiel',
            error: {
                duplicate: 'Der Name des Unterbeispiels ist bereits vergeben',
            }
        },
        subExamples: {
            name: 'Unterbeispiele',
        },
        exerciseSheet: {
            name: 'Übungsblatt',
            created: 'Übungsblatt wurde angelegt',
            saved: 'Übungsblatt wurde gespeichert',
            deleted: 'Übungsblatt wurde gelöscht',
            error: {
                create: 'Übungsblatt konnte nicht angelegt werden',
                duplicate: 'Der Name des Übungsblattes ist bereits vergeben',
                get: 'Übungsblatt konnte nicht geladen werden',
                save: 'Übungsblatt konnte nicht gespeichert werden',
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
        fileTypes: {
            error: {
                get: 'Dateiformate konnten nicht geladen werden'
            }
        },
        forename: 'Vorname',
        format: 'Format',
        home: 'Home',
        information: 'Information',
        issueDate: 'Ausgabedatum',
        kreuzel: {
            name: 'Kreuzel',
            saved: 'Kreuzel wurden gespeichert',
            type: 'Kreuzeltyp',
            error: {
                get: 'Kreuzel konnten nicht geladen werden',
                save: 'Kreuzel konnten nicht gespeichert werden'
            },
            option:{
                1: 'Ja/Nein',
                2: 'Ja/Nein/Nicht sicher'
            }
        },
        lang: 'de-DE',
        lecturer: 'Lehrende/r',
        listEmpty: 'Liste ist leer',
        loading: 'Laden',
        login: 'Anmelden',
        loggedInAs: 'Angemeldet als',
        logout: 'Abmelden',
        mandatory: 'Verpflichtend',
        matriculationNumber: 'Matrikelnummer',
        maxAttemptsReached: 'Die maximale Anzahl der Versuche wurde bereits erreicht',
        minKreuzel: 'Mindestanforderung Kreuzel',
        minPoints: 'Mindestanforderung Punkte',
        minRequireKreuzel: 'Mindestanforderung Kreuzel',
        minRequirePoints: 'Mindestanforderung Punkte',
        name: 'Name',
        new: 'Neu',
        no: 'Nein',
        noFileUploaded: 'Keine Datei hochgeladen',
        notSure: 'Nicht sicher',
        notSureTooltip: 'Diese Option wird als "Ja" gewertet',
        number: 'Nummer',
        optional: 'optional',
        owner: 'Besitzer',
        password: 'Passwort',
        passwordDontMatch: 'Passwörter stimmen nicht überein',
        passwordExpired: 'Das Passwort ist abgelaufen\nEin neues Passwort wird soeben zugeschickt',
        passwordNew: 'Neues Passwort',
        passwordNewConfirm: 'Neues Passwort bestätigen',
        passwordOld: 'Altes Passwort',
        passwordUpdated: 'Passwort wurde aktualisiert',
        passwordUpdatedError: 'Passwort konnte nicht aktualisiert werden',
        pdf: 'PDF',
        points: 'Punkte',
        presentation: {
            deleted: 'Präsentation wurde gelöscht',
            exists: 'Dieser Eintrag existiert bereits',
            saved: 'Präsentation wurde gespeichert',
            error: {
                delete: 'Präsentation konnte nicht gelöscht werden',
                get: 'Präsentationen konnten nicht geladen werden',
                save: 'Präsentation konnte nicht gespeichert werden'
            }
        },
        presentations: 'Präsentationen',
        presets: 'Voreinstellungen',
        remove: 'Entfernen',
        requiredField: 'Pflichtfeld',
        requirements: 'Anforderungen',
        result: 'Ergebnis',
        role: 'Rolle',
        row: 'Zeile',
        save: 'Speichern',
        search: 'Suche',
        searchOrAddFileType: 'Suchen oder Dateityp hinzufügen',
        security: 'Sicherheit',
        selected: 'Ausgewählt',
        semester: {
            create: 'Semester anlegen',
            name: 'Semester',
            created: 'Semester wurde angelegt',
            management: 'Semesterverwaltung',
            summer: 'Sommersemester',
            summerShortcut: 'SS',
            winter: 'Wintersemester',
            winterShortcut: 'WS',
            error: {
                alreadyExists: 'Dieses Semester existiert bereits',
                create: 'Semester konnte nicht angelegt werden',
                get: 'Semester konnten nicht geladen werden'
            }
        },
        sessionExpired: 'Sitzung ist abgelaufen',
        settings: 'Einstellungen',
        show: 'Anzeigen',
        sortError: 'Die Reihenfolge konnte nicht gespeichert werden',
        student: 'Student',
        submissionDate: 'Abgabedatum',
        submitFile: 'Dateiabgabe',
        success: 'Erfolg',
        supportedFileTypes: 'Unterstützte Dateiformate',
        surname: 'Nachname',
        total: 'Insgesamt',
        tutor: 'Tutor',
        typeToSearch: 'Tippen um zu suchen',
        update: 'Aktualisieren',
        uploadLimit: 'Hochladelimit',
        uploadCSV: 'CSV Datei hochladen',
        user: {
            assigned: 'Zugeteilte Benutzer',
            user: 'Benutzer',
            created: 'Benutzer wurde angelegt',
            users: 'Benutzer',
            deleted: 'Benutzer wurde gelöscht',
            management: 'Benutzerverwaltung',
            saved: 'Benutzer wurde gespeichert',
            error: {
                create: 'Benutzer konnte nicht angelegt werden',
                delete: 'Benutzer konnte nicht gelöscht werden',
                get: 'Benutzer konnte nicht geladen werden',
                matriculationNumberExists: 'Die Matrikelnummer ist bereits vergeben',
                matriculationNumberExistsUsernameAvaliable: 'Benutzername ist frei, aber die Matrikelnummer ist bereits vergeben',
                matriculationNumberWrongFormat: 'Ungültiges Format der Matrikelnummer. Bitte vergewissern Sie sich, dass es sich um eine achtstellige Nummer handelt',
                save: 'Benutzer konnte nicht gespeichert werden',
                usernameExists: 'Der Benutzername ist bereits vergeben',
                usernameExistsMatAvailable: 'Die Matrikelnummer ist frei, aber der Benutzername ist bereits vergeben'
            },
            question: {
                delete: 'Wollen Sie den Benutzer wirklich löschen?'
            },
            title: {
                new: 'Neuen Benutzer anlegen',
                edit: 'Benutzer bearbeiten'
            }
        },
        users: {
            error: {
                get: 'Benutzer konnten nicht geladen werden',
            }
        },
        userPwdInvalid: 'Benutzername oder Passwort stimmt nicht überein',
        username: 'Benutzername',
        validator: {
            name: 'Validator',
            skeleton: 'Validator-Skelett',
            deleted: 'Validator wurde gelöscht',
            saved: 'Validator wurde gespeichert',
            error: {
                delete: 'Validator konnte nicht gelöscht werden',
                get: 'Validator konnte nicht geladen werden',
                save: 'Validator konnte nicht gespeichert werden',
                skeletonDownload: 'Validator-Skelett konnte nicht heruntergeladen werden'
            }
        },
        view: 'Ansehen',
        warning: 'Warnung',
        weighting: 'Gewichtung',
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
        addTag: 'Add this as new tag',
        admin: 'Admin',
        all: 'All',
        assigned: 'Assigned',
        attachment: {
            saved: 'Attachtment saved',
            error: {
                get: 'attachment could not be loaded',
                save: 'Attachtment could not be saved'
            }
        },
        attempts: 'Attempts',
        attendance: {
            list: 'Attendance list',
            error: {
                get: 'Could not get attendance list'
            }
        },
        cancel: 'Cancel',
        change: 'Change',
        changeLanguageError: 'Language could not be changed',
        confirm: 'Confirm',
        copy: 'Copy',
        course:{
            copied: 'Course copied',
            name: 'Course',
            management: 'Course Management',
            created: 'Course created',
            deleted: 'Course deleted',
            saved: 'Course saved',
            presetsUpdated: 'Presets saved',
            question:{
                delete: 'Do you really want to delete this course?',
                copy: 'In which semester should the course be copied?'
            },
            error:{
                copy: 'Course could not be copied',
                create: 'Course could not be created',
                delete: 'Course could not be deleted',
                duplicate: 'A course with this number already exists this semester',
                get: 'Course could not be loaded',
                save: 'Course could not be saved',
                presetsUpdate: 'Presets could not be saved',
                usersSave: 'Roles could not be saved',
            },
            title: {
                create: 'New course'
            }
        },
        courses: {
            name: 'Courses',
            error: {
                get: 'Courses could not be loaded'
            }
        },
        create: 'Create',
        date: 'Date',
        deadlineReached: 'Submission date is already reached',
        delete: 'Delete',
        description: 'Description',
        descriptionExerciseSheets: 'Default description for example sheets',
        download: 'Download',
        edit: 'Edit',
        eightDigitNumber: 'Eight-digit number',
        email: 'Email',
        error: 'Error',
        example: {
            created: 'Example created',
            deleted: 'Example deleted',
            name: 'example',
            new: 'New example',
            saved: 'Example saved',
            error: {
                create: 'Example could not be created',
                duplicate: 'Example name is already taken',
                delete: 'Example could not be deleted',
                save: 'Example could not be saved'
            },
            question: {
                delete: 'Do you really want to delete this example?'
            }
        },
        examples: 'Examples',
        exampleUploadLimit: 'Example upload limit',
        reason: 'Reason',
        subExample: {
            name: 'Sub example',
            new: 'New sub example',
            error: {
                duplicate: 'Sub example name is already taken',
            }
        },
        subExamples: {
            name: 'Sub examples',
        },
        exerciseSheet: {
            name: 'Exercise sheet',
            create: 'Exercise sheet created',
            saved: 'Exercise sheet saved',
            delete: 'Exercise sheet deleted',
            error: {
                create: 'Exercise sheet could not be created',
                duplicate: 'Exercise sheet name is already taken',
                get: 'Exercise sheet could not be loaded',
                save: 'Exercise sheet could lnto be saved',
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
        fileTypes: {
            error: {
                get: 'File types could not be loaded'
            }
        },
        forename: 'Forename',
        format: 'Format',
        home: 'Home',
        information: 'Information',
        issueDate: 'Date of issue',
        kreuzel: {
            name: 'Kreuzel',
            save: 'Kreuzel saved',
            type: 'Kreuzel type',
            error: {
                get: 'Kreuzel could not be loaded',
                save: 'Kreuzel could not be saved'
            },
            option:{
                1: 'Yes/No',
                2: 'Yes/No/Not sure'
            }
        },
        lang: 'en-US',
        lecturer: 'Lecturer',
        listEmpty: 'List is empty',
        loading: 'Loading',
        login: 'Login',
        loggedInAs: 'Logged in as',
        logout: 'Logout',
        mandatory: 'Mandatory',
        matriculationNumber: 'Matriculation number',
        maxAttemptsReached: 'The maximum number of attempts has already been reached',
        minKreuzel: 'Minimum requirement of kreuzel',
        minPoints: 'Minimum requirement of points',
        minRequireKreuzel: 'Minimum requirement of kreuzel',
        minRequirePoints: 'Minimum requirement of points',
        name: 'Name',
        new: 'New',
        no: 'No',
        noFileUploaded: 'No file uploaded',
        notSure: 'Not sure',
        notSureTooltip: 'This option is considered "yes"',
        number: 'Number',
        optional: 'optional',
        owner: 'Owner',
        password: 'Password',
        passwordDontMatch: 'Passwords do not match',
        passwordExpired: 'The password has expired.\nA new password has just been sent',
        passwordNew: 'New password',
        passwordNewConfirm: 'Confirm new password',
        passwordOld: 'Old Password',
        passwordUpdated: 'Password updated',
        passwordUpdatedError: 'Password could not be updated',
        pdf: 'PDF',
        points: 'Points',
        presentation: {
            deleted: 'Presentation deleted',
            exists: 'This entry already exists',
            saved: 'Presentation saved',
            error: {
                delete: 'Presentation could not be deleted',
                get: 'Presentation could not be loaded',
                save: 'Presentation could not be saved'
            }
        },
        presentations: 'Presentations',
        presets: 'Presets',
        remove: 'Remove',
        requiredField: 'Required field',
        requirements: 'Requirements',
        result: 'Result',
        role: 'Role',
        row: 'Row',
        save: 'Save',
        search: 'Search',
        searchOrAddFileType: 'Search or add a file type',
        security: 'Security',
        selected: 'Selected',
        semester: {
            create: 'Create Semester',
            name: 'Semester',
            created: 'Semester created',
            management: 'Semester management',
            summer: 'Summer semester',
            summerShortcut: 'SS',
            winter: 'Winter semester',
            winterShortcut: 'WS',
            error:{
                alreadyExists: 'Semester already exists',
                get: 'Semester could not be loaded',
                create: 'Semester could not be created'
            }
        },
        sessionExpired: 'Session expired',
        settings: 'Settings',
        show: 'Show',
        sortError: 'Order could not be updated',
        student: 'Student',
        submissionDate: 'Submission date',
        submitFile: 'File submission',
        success: 'Success',
        supportedFileTypes: 'Supported file types',
        surname: 'Surname',
        total: 'Total',
        tutor: 'Tutor',
        typeToSearch: 'Type to search',
        update: 'Update',
        uploadCSV: 'Upload CSV file',
        uploadLimit: 'Upload limit',
        user: {
            assigned: 'Assigned users',
            user: 'User',
            users: 'Users',
            created: 'User created',
            deleted: 'User deleted',
            management: 'User Management',
            saved: 'User saved',
            error: {
                create: 'User could not be created',
                delete: 'User could not be deleted',
                get: 'User could not be loaded',
                matriculationNumberExists: 'Matriculation number is already taken',
                matriculationNumberExistsUsernameAvaliable: 'Username is available but matriculation number is already taken',
                matriculationNumberWrongFormat: 'Invalid matriculation number format. Please  Please make sure it is an eight-digit number',
                save: 'User could not be saved',
                usernameExists: 'Username is already taken',
                usernameExistsMatAvailable: 'Matriculation number is available but username is already taken'
            },
            question: {
                delete: 'Do you really want to delete this user?',
            },
            title: {
                new: 'Create new user',
                edit: 'Edit user'
            }
        },
        userPwdInvalid: 'Username or password do not match',
        username: 'Username',
        validator: {
            name: 'Validator',
            skeleton: 'Validator skeleton',
            deleted: 'Validator deleted',
            saved: 'Validator saved',
            error: {
                delete: 'Validator could not be deleted',
                get: 'Validator could not be loaded',
                save: 'Validator could not be saved',
                skeletonDownload: 'Validator skeleton could not be downloaded'
            }
        },
        view: 'View',
        warning: 'Warning',
        weighting: 'Weighting',
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