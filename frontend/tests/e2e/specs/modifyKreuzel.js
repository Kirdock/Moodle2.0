const Path = require('path');
const courseTest = require('./modifyCourse.js');
const testCourses = require('./testFiles/testCourses.js');
const exerciseSheetTest = require('./modifyExerciseSheet.js');
const testExerciseSheets = require('./testFiles/testExerciseSheets.js');
const testExamplesRight = require('./testFiles/testExamplesRight.js');
const visibileCourses = [testCourses[0]];
const hiddenCourses = [testCourses[1]];
const kreuzelDescription = 'My kreuzel description';

module.exports = {
    before: browser => {
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
    'modify exerciseSheet type2': function(browser, exerciseSheet){
        const page = browser.page.studentExerciseSheet();
        exerciseSheet = exerciseSheet || testExerciseSheets[1];
        this['select exerciseSheet'](browser, undefined, exerciseSheet);
        const example = testExamplesRight[0];
        const resultModal = page.section.resultModal;

        page.checkMandatory(0,1);
        page.checkKreuzel(0,3,exerciseSheet.minKreuzel)
        page.checkPoints(0,18,exerciseSheet.minPoints)


        page.setKreuzel2(example.name, false, 1);
        page.assert.not.elementPresent({
            selector: page.description(example.name, false),
            locateStrategy: 'xpath'
        })

        page.setKreuzel2(example.name, false, 2);
        page.assert.not.elementPresent({
            selector: page.description(example.name, false),
            locateStrategy: 'xpath'
        })

        page.setKreuzel2(example.name, false, 3);
        page.assert.elementPresent({
            selector: page.description(example.name, false),
            locateStrategy: 'xpath'
        })
        page.clearValue2(page.description(example.name, false), true);
        page.assert.isValidInput(page.description(example.name, false), 'valid', false, true)
        page.submit();
        page.assert.not.toastPresent();



        page.clearValue2(page.description(example.name, false), true)
            .setValue('xpath',page.description(example.name, false), kreuzelDescription);

        page.setKreuzel2(testExamplesRight[1].subExamples[0].name, true, 1);

        page.checkMandatory(1,1);
        page.checkKreuzel(2,3,exerciseSheet.minKreuzel)
        page.checkPoints(18,18,exerciseSheet.minPoints)

        page.setKreuzel2(testExamplesRight[2].name, false, 1);
        page.setValue('xpath',page.uploadButton(testExamplesRight[2].name, false), Path.resolve(`${__dirname}/testFiles/${'testSubmission.zip'}`));
        page.assert.successPresent();
        page.closeToast();
        page.checkUploadCount(testExamplesRight[2].name, false, 0);
        page.expect.element({
                selector: page.uploadButton(testExamplesRight[2].name, false),
                locateStrategy: 'xpath'
            }).to.not.be.enabled;
        page.click('xpath',page.resultButton(testExamplesRight[2].name, false)).pause(1000);
        
        resultModal.containsResult();
        resultModal.cancelX();
        resultModal.pause(1000);
        page.expect.section('@resultModal').to.not.be.present;

        page.checkKreuzel(3,3,exerciseSheet.minKreuzel)

        page.submit();
        page.assert.successPresent();
        page.closeToast();
        
        browser.refresh().pause(1000);

        page.checkKreuzelInfo({
            name: example.name,
            isSubExample: false,
            type: 3,
            description: kreuzelDescription
        });

        page.checkKreuzelInfo({
            name: testExamplesRight[1].subExamples[0].name,
            isSubExample: true,
            type: 1
        });
        page.checkMandatory(1,1);
        page.checkKreuzel(3,3,exerciseSheet.minKreuzel)
        page.checkPoints(18,18,exerciseSheet.minPoints)
        page.checkUploadCount(testExamplesRight[2].name, false, 0);
        page.expect.element({
                selector: page.uploadButton(testExamplesRight[2].name, false),
                locateStrategy: 'xpath'
            }).to.not.be.enabled;
        page.click('xpath',page.resultButton(testExamplesRight[2].name, false)).pause(1000);
        
        resultModal.containsResult();
        resultModal.cancelX();
        resultModal.pause(1000);
        page.expect.section('@resultModal').to.not.be.present;
    },
    'modify exerciseSheet type1': function(browser, exerciseSheet){
        const page = browser.page.studentExerciseSheet();
        exerciseSheet = exerciseSheet || testExerciseSheets[0];
        this['select exerciseSheet'](browser, undefined, exerciseSheet);
        
        page.checkMandatory(0,1);
        page.checkKreuzel(0,3,exerciseSheet.minKreuzel)
        page.checkPoints(0,18,exerciseSheet.minPoints)

        page.setKreuzel(testExamplesRight[0].name, false, true);
        page.setKreuzel(testExamplesRight[1].subExamples[0].name, true, true);
        page.submit();
        page.assert.successPresent();
        page.closeToast();
        browser.refresh().pause(1000);
        page.checkKreuzelInfo({
            name: testExamplesRight[0].name,
            isSubExample: false,
            type: true,
        })
        page.checkKreuzelInfo({
            name: testExamplesRight[1].subExamples[0].name,
            isSubExample: true,
            type: true,
        })
        page.checkMandatory(1,1);
        page.checkKreuzel(2,3,exerciseSheet.minKreuzel)
        page.checkPoints(18,18,exerciseSheet.minPoints)
    }
}