import Vue from 'vue';
import VueI18n from 'vue-i18n';
Vue.use(VueI18n);

const messages = {
    'de': {
        test: 'deitsch',
        changeLanguageError: 'Die Sprache konnte nicht geändert werden',
        toastErrorTitle: 'Fehler'
    },
    'en': {
        test: 'englisch',
        changeLanguageError: 'Language could not be changed',
        toastErrorTitle: 'Error'
    }
};

const i18n = new VueI18n({
    fallbackLocale: 'en', // set fallback locale
    messages, // set locale messages
});

export default i18n;