const Path = require('path');
const courseTest = require('./modifyCourse.js');
const testCourses = require('./testFiles/testCourses.js');
const visibileCourses = [testCourses[0]];
const hiddenCourses = [testCourses[1]];

module.exports = {
    before: browser => {
        courseTest.before(browser, function (){
            courseTest['create course'](browser); //create two courses, one where the user is in and one where he isn't
            courseTest['assign users csv'](browser);
            courseTest['create exerciseSheet'](browser);
            browser.loginAsStudent();
        });
    },
    'check courses': function(browser){

    }
}