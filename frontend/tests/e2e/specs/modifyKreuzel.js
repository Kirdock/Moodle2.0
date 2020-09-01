const Path = require('path');
const courseTest = require('./modifyCourse.js');
const testCourses = require('./testFiles/testCourses.js');
const exerciseSheetTest = require('./modifyExerciseSheet.js');
const testExerciseSheets = require('./testFiles/testExerciseSheets.js');
const testExamplesRight = require('./testFiles/testExamplesRight.js');
const visibileCourses = [testCourses[0]];
const hiddenCourses = [testCourses[1]];

module.exports = {
    before: browser => {
        // courseTest.before(browser, function (){
        //     courseTest['create course'](browser); //create two courses, one where the user is in and one where he isn't
        //     courseTest['assign users'](browser);
        //     for(const sheet of testExerciseSheets){
        //         browser.perform(done=>{
        //             exerciseSheetTest.before(browser, function (){
        //                 exerciseSheetTest['create example'](browser);
        //                 done();
        //             }, sheet.name);
        //         })
        //     }
        //     browser.perform(done =>{
        //         browser.loginAsStudent();
        //         done();
        //     })
        // });
        browser.loginAsStudent();
    },
    'check courses': function(browser){
        const page = browser.page.studentCourses();
        page.navigate();
        const dateToday = new Date();
        const sSemesterSelected = dateToday.getMonth() > 1 && dateToday.getMonth() < 9;
        page.selectSemester(dateToday.getFullYear(), sSemesterSelected);

        for(const visibleCourse of visibileCourses){
            page.coursePresent(visibleCourse);
        }
        for(const hiddenCourse of hiddenCourses){
            page.courseNotPresent(hiddenCourse);
        }
    },
    'select course': function(browser, course){
        const page = browser.page.studentCourses();
        page.navigate().pause(1000);
        const coursePage = browser.page.studentCourse();
        course = course || visibileCourses[0];
        page.selectCourse(course);
        
        browser.expect.url().to.match(/\/Course\/([0-9]+)$/);
        coursePage.validateCourse(course);
        for(const exerciseSheet of testExerciseSheets){
            coursePage.validateExerciseSheet(exerciseSheet);
        }
    },
    'check exerciseSheets': function(browser, course){
        course = course || visibileCourses[0];
        const self = this;
        
        for(const exerciseSheet of testExerciseSheets){
            browser.perform(function(done){
                self['select course'](browser, course)
                self['select exerciseSheet'](browser, course, exerciseSheet)
                done();
            })
        }
    },
    'select exerciseSheet': function(browser, course, exerciseSheet){
        const page = browser.page.studentCourse();
        const exerciseSheetPage = browser.page.studentExerciseSheet();
        exerciseSheet = exerciseSheet || testExerciseSheets[0];
        this['select course'](browser, course);
        page.selectExerciseSheet(exerciseSheet)
        browser.log(`testing ExerciseSheet ${exerciseSheet.name}`)
        browser.expect.url().to.match(/\/Course\/([0-9]+)\/ExerciseSheet\/([0-9]+)$/);
        exerciseSheetPage.validateExerciseSheet(exerciseSheet);
        
        for(const example of testExamplesRight){
            exerciseSheetPage.validateExample(example, exerciseSheet);
        }
    }
}