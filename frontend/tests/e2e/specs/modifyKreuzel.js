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
    },
    'check courses': function(browser){
        for(const visibleCourse of visibileCourses){
            browser.assert.elementPresent('xpath', `//div[@id="courseList"]/div/a[text()="${visibleCourse.number} ${visibleCourse.name}"]`);
        }
        for(const hiddenCourse of hiddenCourses){
            browser.assert.not.elementPresent('xpath', `//div[@id="courseList"]/div/a[text()="${hiddenCourse.number} ${hiddenCourse.name}"]`);
        }
    },
    'select course': function(browser, course){
        course = course || visibileCourses[0];
        browser.click({
            selector: `//div[@id="courseList"]/div/a[text()=" ${course.number} ${course.name} "]`,
            locateStrategy: 'xpath'
        }).pause(1000)
          .expect.url().to.match(/\/Course\/([0-9]+)$/);
        browser.assert.elementPresent('xpath', `//div[@class="course"]/div[1][text()="${course.description}"]`);
    },
    'check exerciseSheet': function(browser, course){
        this['select course'](browser, course);

        for(const exercisSheet of testExerciseSheets){
            browser.assert.elementPresent('xpath', `//div[@class="course"]/table/tbody/tr[td[1][text() = " ${exercisSheet.name} "] and td[2][contains(text(), "/${2}")] and td[3][text(), " ${exercisSheet.submissionDateFormat} ")] and td[4][text() = " ${exercisSheet.minKreuzel} "] and td[5][contains(text(), "/${15}")] and td[6][text(), " ${exercisSheet.minPoints} "]]`);
        }
    },
    'select exerciseSheet': function(browser, course, exerciseSheet){
        exerciseSheet = exerciseSheet || testExerciseSheets[0];
        this['select course'](browser, course);
        browser.click({
            selector: `//div[@class="course"]/table/tbody/tr[td[1][text() = " ${exerciseSheet.name} "]]/td[7]/a[2]`,
            locateStrategy: 'xpath'
        }).pause(1000)
          .expect.url().to.match(/\/Course\/([0-9]+)\/ExerciseSheet\/([0-9]+)$/);
        browser.assert.elementPresent('xpath', `//div[@class="exerciseSheet"]/h2[contains(text(),"${exerciseSheet.submissionDateFormat}")]`)
            .assert.elementPresent('xpath', `//div[@class="exerciseSheet"]/div[1]/label/strong[text()="${exerciseSheet.minKreuzel}%"]`)
            .assert.elementPresent('xpath', `//div[@class="exerciseSheet"]/div[2]/label/strong[text()="${exerciseSheet.minPoints}%")]`)
        
        for(const example of testExamplesRight){
            if(example.subExamples.length === 0){
                browser.assert.elementPresent('xpath', `//div[@class="exerciseSheet"]//table/tbody/tr[td[1][text()=" ${example.name} "] and td[2][boolean(text()) = true] and td[3][text()=" ${example.mandatory ? 'Ja' : 'Nein'} "] and td[4][text()=" ${example.weighting} "] and td[5][text()=" ${example.points} "] and td[6][${example.includeThird ? `count(.//input[@type="radio"${exerciseSheet.deadlineReached ? 'and @disabled="disabled"' : ''}]) = 3` : `input[@type="checkbox" ${exerciseSheet.deadlineReached ? 'and @disabled="disabled"' : ''}]`}]`)
            }
            else{
                browser.assert.elementPresent('xpath', `//div[@class="exerciseSheet"]//table/tbody/tr[td[1][text()=" ${example.name} "] and td[2][boolean(text()) = true] and td[3][boolean(text()) = true] and td[4][boolean(text()) = true] and td[5][boolean(text()) = true]]`)
            }
            for(const subExample of example.subExamples){
                browser.assert.elementPresent('xpath', `//div[@class="exerciseSheet"]//table/tbody/tr[td[1][boolean(text()) = true] and td[2][text()=" ${subExample.name} "] and td[3][text()=" ${subExample.mandatory ? 'Ja' : 'Nein'} "] and td[4][text()=" ${subExample.weighting} "] and td[5][text()=" ${subExample.points} "] and td[6][${subExample.includeThird ? `count(.//input[@type="radio" ${exerciseSheet.deadlineReached ? 'and @disabled="disabled"' : ''}]) = 3` : `input[@type="checkbox" ${exerciseSheet.deadlineReached ? 'and @disabled="disabled"' : ''}]`}]`)
            }
        }
    }
}