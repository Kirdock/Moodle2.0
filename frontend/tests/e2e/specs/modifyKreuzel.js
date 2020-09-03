const Path = require('path');
const courseTest = require('./modifyCourse.js');
const testCourses = require('./testFiles/testCourses.js');
const exerciseSheetTest = require('./modifyExerciseSheet.js');
const testExerciseSheets = require('./testFiles/testExerciseSheets.js');
const testExamplesRight = require('./testFiles/testExamplesRight.js');
const testKreuzel2 = require('./testFiles/testKreuzel2.js');
const testKreuzel = require('./testFiles/testKreuzel.js');
const visibileCourses = [testCourses[0]];
const hiddenCourses = [testCourses[1]];

module.exports = {
    before: function(browser) {
        courseTest.before(browser, function (){
            courseTest['create course'](browser); //create two courses, one where the user is in and one where he isn't
            courseTest['assign users'](browser);
            for(const sheet of testExerciseSheets){
                browser.perform(done=>{
                    exerciseSheetTest.before(browser, function (){
                        exerciseSheetTest['create example'](browser);
                        done();
                    }, sheet.name);
                })
            }
            browser.perform(done =>{
                browser.loginAsStudent();
                done();
            })
        });
        // browser.loginAsStudent();
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
        const dateToday = new Date();
        const sSemesterSelected = dateToday.getMonth() > 1 && dateToday.getMonth() < 9;
        page.selectSemester(dateToday.getFullYear(), sSemesterSelected);

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
        if(exerciseSheet.deadlineReached){
            exerciseSheetPage.expect.element('@submitButton').to.not.be.enabled;
        }
        else{
            exerciseSheetPage.expect.element('@submitButton').to.be.enabled;
        }
    },
    'modify exerciseSheet type2 invalid': function(browser, exerciseSheet){
        const page = browser.page.studentExerciseSheet();
        exerciseSheet = exerciseSheet || testExerciseSheets[1];
        this['select exerciseSheet'](browser, undefined, exerciseSheet);
        const example = testExamplesRight[0];

        page.setKreuzel(example.name, false, 1);
        page.assert.not.elementPresent({
            selector: page.description(example.name, false),
            locateStrategy: 'xpath'
        })

        page.setKreuzel(example.name, false, 2);
        page.assert.not.elementPresent({
            selector: page.description(example.name, false),
            locateStrategy: 'xpath'
        })

        page.setKreuzel(example.name, false, 3);
        page.assert.elementPresent({
            selector: page.description(example.name, false),
            locateStrategy: 'xpath'
        })
        page.clearValue2(page.description(example.name, false), true);
        page.assert.isValidInput(page.description(example.name, false), 'valid', false, true)
        page.submit();
        page.assert.not.toastPresent();
    },
    'modify exerciseSheet type2': function(browser, kInfos){
        const page = browser.page.studentExerciseSheet();
        const kreuzelInfos = kInfos || testKreuzel2;
        const self = this;

        for(const kreuzel of kreuzelInfos){
            browser.perform(function(done){
                self['select exerciseSheet'](browser, undefined, kreuzel.exerciseSheet);
                page.checkMandatory(0, kreuzel.mandatory);
                page.checkKreuzel(0, kreuzel.kreuzel, kreuzel.exerciseSheet.minKreuzel)
                page.checkPoints(0, kreuzel.points, kreuzel.exerciseSheet.minPoints)
                for(const example of kreuzel.examples){
                    browser.perform(function(done2){
                        page.setKreuzel(example.name, example.isSubExample, example.type);
                        if(example.type === 3){
                            page.clearValue2(page.description(example.name, example.isSubExample), true)
                                .setValue('xpath',page.description(example.name, example.isSubExample), example.description);
                        }
                        if(example.uploadCount !== undefined){
                            page.checkUploadCount(example.name, example.isSubExample, example.uploadCount);
                        }
                        if(example.submitFile){
                            page.setValue('xpath',page.uploadButton(example.name, example.isSubExample), Path.resolve(`${__dirname}/testFiles/${'testSubmission.zip'}`));
                            page.assert.successPresent();
                            page.closeToast();
                            page.checkExampleAfterUpload(example);
                        }
                        done2();
                    })
                }
                page.checkMandatory(kreuzel.mandatoryAfter, kreuzel.mandatory);
                page.checkKreuzel(kreuzel.kreuzelAfter, kreuzel.kreuzel, kreuzel.exerciseSheet.minKreuzel)
                page.checkPoints(kreuzel.pointsAfter, kreuzel.points, kreuzel.exerciseSheet.minPoints)

                page.submit();
                page.assert.successPresent();
                page.closeToast();
                
                browser.refresh().pause(1000);
                page.validateKreuzelInfo(kreuzel)
                done();
            });
        }
        
    },
    'modify exerciseSheet type1': function(browser, kreuzelInfos){
        this['modify exerciseSheet type2'](browser, kreuzelInfos || testKreuzel)
    },
    'check edit kreuzel by admin or owner': function(browser){
        const page = browser.page.studentExerciseSheet();
        const self = this;
        let kreuzelInfo = JSON.parse(JSON.stringify(testKreuzel));
        kreuzelInfo = kreuzelInfo[0];
        kreuzelInfo.exerciseSheet = testExerciseSheets[3];
        
        let kreuzelInfo2 = JSON.parse(JSON.stringify(testKreuzel2));
        kreuzelInfo2 = kreuzelInfo2[0];
        kreuzelInfo2.exerciseSheet = testExerciseSheets[2];
        courseTest.before(browser, function(){
            courseTest['kreuzel test'](browser, true);
            browser.logout();
            browser.loginAsStudent();

            for(const kreuzel of [kreuzelInfo, kreuzelInfo2]){
                browser.perform(done => {
                    self['select exerciseSheet'](browser, undefined, kreuzel.exerciseSheet);
                    page.validateKreuzelInfo(kreuzel, true);
                    done();
                })
            }
        })
    }
}