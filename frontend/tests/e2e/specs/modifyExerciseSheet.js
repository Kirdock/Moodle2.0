const courseTest = require('./modifyCourse.js');

module.exports = {
    before: browser => {
        courseTest.before(browser, next);
        function next(){
            courseTest['select exerciseSheet'](browser);
        }
    },
    'modify information': browser => {
        
    }
}