const Path = require('path');
const testCourses = require('./testFiles/testCourses.js');
const testCourse = testCourses[0];
const testExerciseSheet = require('./testFiles/testExerciseSheets.js')[0];
const testExerciseSheetsInvalid = require('./testFiles/testExerciseSheetsInvalid.js');
const testUsers = require('./testFiles/testUsers.js');
const userTest = require('./modifyUsers.js');
const exerciseSheetsTab = 2;
const assignUsersTab = 1;
const informationTab = 0;

function deSelectAssignedUser(browser){
    const assignedUsers = browser.page.courseManagement().section.assignedUsers;
    assignedUsers.selectRole(4);
    browser.perform(function (done){
        browser.elements('css selector', assignedUsers.elements.tableEntriesCheck.selector, function(result){
            if (result.value && result.value.length !== 0) {
                // Element is present
                for(const element of result.value){
                    browser.elementIdClick(element.ELEMENT) //reset
                }
                assignedUsers.assert.not.elementPresent('@tableEntries');
            }
            assignedUsers.submit();
            assignedUsers.assert.successPresent();
            assignedUsers.closeToast();
            done();
        });
    })
}

function ownerExists(browser, name){
    const page = browser.page.courseManagement();
    page.showNewModal()
    page.pause(1000);
    const defaultTimeoutBefore = browser.options.globals.asyncHookTimeout;
    browser.options.globals.asyncHookTimeout = 60000; //adjust default timeout; creation of users takes more time than 10s
    browser.perform(function(done){
        page.ownerExists(browser, name, function(result){
            if(result.value && result.value.ELEMENT){
                browser.log('Owner is present');
                page.section.modal_new.cancel().pause(1000, done)
            }
            else{
                browser.log('Owner is not present. Owner will now be created');
                browser.page.userManagement().navigate().pause(2000);

                userTest['upload CSV file'](browser);
                
                page.navigate().pause(1000, ()=>{
                    browser.options.globals.asyncHookTimeout = defaultTimeoutBefore;
                    done();
                });
            }
        })
    })
}

function courseExists(self, browser, courseText, create){
    //create === true: create it
    //create === false: delete it
    const page = browser.page.courseManagement();
    browser.perform(function (done){
        browser.element('xpath', `${page.elements.selectCourseX.selector}/option[contains(text(),"${courseText}")]`, function(result){
            if (result.value && result.value.ELEMENT) {
                // Element is present
                if(!create){
                    browser.log('Course already available. Course will now be deleted')
                    self['delete course'](browser, courseText);
                }
                done();
            } else {
                if(create){
                    browser.log('Course does not exist. Course will now be created')
                    self['create course'](browser);
                }
                done();
            }
        });
    })
}

function exerciseSheetExists(self, browser, courseText, name, create){
    const page = browser.page.courseManagement();
    courseExists(self, browser, courseText, true)

    browser.perform(function (done){
        page.selectCourse(courseText);
        page.selectTab(exerciseSheetsTab);
        const exerciseSheet = page.section.exerciseSheets;
        exerciseSheet.exerciseSheetPresent(browser, name, function(result){
            if (result.value && result.value.length !== 0) {
                // Elements are present
                if(!create){
                    browser.log('ExerciseSheet already available. ExerciseSheet will now be deleted')
                    self['delete exerciseSheet'](browser, name);
                }
                done();
            } else {
                if(create){
                    browser.log('ExerciseSheet does not exist. ExerciseSheet will now be created')
                    self['create exerciseSheet'](browser);
                }
                done();
            }
        });
    })
}

module.exports = {
    before: (browser, done) => {
        const page = browser
            .loginAsAdmin()
            .page.courseManagement()
        page.navigate().pause(1000);
        //check if semester exists
        browser.element('css selector', page.elements.selectSemesterOption.selector,function(result){
            if(result.value && result.value.ELEMENT){
                page.pause(1000, done);
            }
            else{
                const semester = require('./modifySemester.js')
                browser.page.semesterManagement().navigate();
                browser.perform(() =>{
                    semester['create semester'](browser);
                }).perform(()=>{
                    page.navigate();
                    page.pause(1000, done);
                })
            }
        });
    },
    'select course': function (browser) {
        const page = browser.page.courseManagement();
        page.navigate().pause(1000);
        courseExists(this, browser, testCourse.number, true)

        page.expect.element('@deleteButton').to.not.be.present
        page.expect.element('@copyButton').to.not.be.present
        page.selectCourse(testCourse.number);
        page.expect.element('@deleteButton').to.be.present
        page.expect.element('@copyButton').to.be.present
        page.assert.urlContains('?courseId=');
    },
    'create course invalid': browser => {
        const page = browser.page.courseManagement();
        page.navigate().pause(1000)
        const modal_new = page.section.modal_new;
        const courses = [
            {
                owner: {
                    value: 'owner',
                    valid: true
                },
                number: {
                    value: '123.985',
                    valid: true
                },
                name: {
                    value: 'TestCourse1',
                    valid: true
                },
                minKreuzel:{
                    value: 'abc',
                    expected: '',
                    valid: true
                },
                minPoints: {
                    value: '120',
                    valid: false
                },
                description: {
                    value: 'My course description'
                }
            }
        ]
        for(const course of courses){
            page.showNewModal(); //have to open the modal again, because owner is not resetable (it can't be empty)
            const {owner, description, ...keys} = course;

            modal_new.clearValue('@description')
                .setValue('@description', ` ${browser.Keys.BACK_SPACE}`)
                .setValue('@description', description.value);
            for(const data in keys){
                modal_new
                .setValue(`@${data}`, course[data].value);
                if(course[data].expected !== undefined){
                    modal_new.assert.value(`@${data}`,course[data].expected)
                }
                
                modal_new.assert.isValidInput(`@${data}`, 'valid', course[data].valid)
            }
            if(owner.value !== undefined){
                modal_new.setMultiSelect('@owner',0,owner.value)
            }
            modal_new.assert.isValidInput(`@ownerInput`, 'valid', owner.valid);
            modal_new.submit();
            modal_new.assert.not.toastPresent();
            page.modalNewPresent();
            modal_new.cancel();
        }
        
        page.expect.element('@container').to.not.be.present;
        page.assert.not.urlContains('?courseId=');
    },
    'create course': function (browser) {
        const page = browser.page.courseManagement();
        const modal_new = page.section.modal_new;
        const courseInfo = page.section.courseInfo;
        const defaultTimeoutBefore = browser.options.globals.asyncHookTimeout;
        const self = this;
        browser.options.globals.asyncHookTimeout = 60000;
        for(const course of testCourses){
            browser.perform(function(done){
                courseExists(self,browser, course.number, false)
                ownerExists(browser, course.owner.value)
                page.showNewModal().pause(1000);

                modal_new.setMultiSelect('@owner',course.owner.index, course.owner.value);
                modal_new
                    .setValue('@number', course.number)
                    .setValue('@name', course.name)
                    .setValue('@minKreuzel', course.minKreuzel)
                    .setValue('@minPoints', course.minPoints)
                    .setValue('@description', course.description)
                    .submit();
                page.assert.successPresent();
                page.closeToast();
                page.pause(1000);
                page.assert.urlContains('?courseId=');
                courseInfo.assert.containsText('@ownerText',`${course.owner.value}`)
                    .assert.value('@number', course.number)
                    .assert.value('@name', course.name)
                    .assert.containsText('@description', course.description)
                    .assert.value('@minKreuzel', course.minKreuzel)
                    .assert.value('@minPoints', course.minPoints)
                done();
            })
        }
        browser.options.globals.asyncHookTimeout = defaultTimeoutBefore;
    },
    'modify course invalid': function(browser, number){
        number = number || testCourse.number;
        courseExists(this,browser, number, true);

        const page = browser.page.courseManagement();
        const courseInfo = page.section.courseInfo;
        page.selectCourse(number);
        page.selectTab(informationTab);

        const courseData = [
            {
                owner: {
                    value: 'admin',
                    valid: true
                },
                number: {
                    value: '123.985',
                    valid: true
                },
                name: {
                    value: 'TestCourse1',
                    valid: true
                },
                minKreuzel:{
                    value: 'abc',
                    expected: '',
                    valid: true
                },
                minPoints: {
                    value: '120',
                    valid: false
                },
                description: {
                    value: 'My course description'
                }
            }
        ]
        
        for(const course of courseData){
            const {owner, description, ...data} = course;

            courseInfo.setMultiSelect('@owner',course.owner.index, course.owner.value)
                .assert.isValidInput(`@ownerInput`, 'valid', owner.valid)
                .clearValue('@description')
                .setValue('@description', ` ${browser.Keys.BACK_SPACE}`)
                .setValue('@description', description.value);
            for(const key in data){
                courseInfo.clearValue2(`@${key}`)
                    .setValue(`@${key}`, course[key].value)

                if(course[key].expected !== undefined){
                    courseInfo.assert.value(`@${key}`,course[key].expected)
                }
                courseInfo.assert.isValidInput(`@${key}`, 'valid', course[key].valid)
            }
            
            courseInfo.submit();
            courseInfo.assert.not.toastPresent();
        }
    },
    'modify course': function(browser, courseText){
        courseText = courseText || testCourse.number;
        const self = this;
        const courseData = [
            {
                owner: {
                    value: 'admin',
                    valid: true
                },
                number: {
                    value: '123.985',
                    valid: true
                },
                name: {
                    value: 'TestCourse1',
                    valid: true
                },
                minKreuzel:{
                    value: '50',
                    expected: '50',
                    valid: true
                },
                minPoints: {
                    value: '30',
                    expected: '30',
                    valid: true
                },
                description: {
                    value: 'My course description'
                }
            }
        ]
        courseExists(self, browser, courseText, true);
        for(const course of courseData){
            courseExists(self, browser, `${course.number.value} ${course.name.value}`, false);
        }
        
        const page = browser.page.courseManagement();
        const courseInfo = page.section.courseInfo;
        page.selectCourse(courseText);
        page.selectTab(informationTab);
        
        for(const course of courseData){
            const {owner, description, ...data} = course;

            courseInfo.setMultiSelect('@owner',course.owner.index, course.owner.value)
                .assert.isValidInput(`@ownerInput`, 'valid', owner.valid)
                .clearValue('@description')
                .setValue('@description', ` ${browser.Keys.BACK_SPACE}`)
                .setValue('@description', description.value);
            for(const key in data){
                courseInfo.clearValue2(`@${key}`)
                    .setValue(`@${key}`, course[key].value)

                if(course[key].expected !== undefined){
                    courseInfo.assert.value(`@${key}`,course[key].expected)
                }
                courseInfo.assert.isValidInput(`@${key}`, 'valid', course[key].valid)
            }
            courseInfo.submit();
            courseInfo.assert.successPresent();
            courseInfo.closeToast();

            page.assert.containsText('@selectCourse', `${course.number.value} ${course.name.value}`);
        }
    },
    'delete course': function(browser, courseText){
        courseText = courseText || testCourse.number;
        courseExists(this,browser, courseText, true);
        
        const page = browser.page.courseManagement();
        const deleteModal = page.section.modal_delete;
        page.selectCourse(courseText)
        page.showDeleteModal();
        deleteModal.pause(1000).submit() //without pause, button click is not triggered

        page
            .modalDeleteNotPresent();
        page
            .assert.successPresent()
            .closeToast()
            .assert.not.elementPresent('@container')
            .assert.not.elementPresent('@deleteButton')
            .expect.element('@copyButton').to.not.be.present
        page.assert.not.urlContains('?courseId=');
    },
    'modal_new close test': browser =>{
        const page = browser.page.courseManagement();
        page.navigate().pause(1000);
        
        const modal_new = page.section.modal_new;
        const modalCloseVariants = ['cancel', 'cancelX', 'cancelClick'];
        for(const variant of modalCloseVariants){
            page.showNewModal();
            modal_new.pause(1000)[variant]();
            page.modalNewNotPresent();
            page.assert.not.toastPresent()
        }
        page.expect.element('@container').to.not.be.present;
    },
    'assign users csv': function (browser){
        const courseText = testCourse.number;
        const page = browser.page.courseManagement();
        const assignedUsers = page.section.assignedUsers;
        const self = this;

        courseExists(self, browser, courseText, true);

        page.selectCourse(courseText);
        page.selectTab(assignUsersTab);
        deSelectAssignedUser(browser)

        page.expect.section('@assignedUsers').to.be.visible;
        assignedUsers.setValue('@uploadCsv', Path.resolve(`${__dirname}/testFiles/${'usersRight'}.csv`));
        assignedUsers.selectRole(4);
        assignedUsers.pause(1000)

        for(const user of testUsers){
            assignedUsers.userAssignedAsStudent(browser, user)
        }
    },
    'assign users': function(browser, courseText) {
        courseText = courseText || testCourse.number;
        const page = browser.page.courseManagement();
        const assignedUsers = page.section.assignedUsers;
        const self = this;

        courseExists(self, browser, courseText, true);
        page.selectCourse(courseText);
        page.selectTab(assignUsersTab);
        this['assign users csv'](browser);
        page.expect.section('@assignedUsers').to.be.visible;
        deSelectAssignedUser(browser);
            
        assignedUsers.selectRole(0);
        assignedUsers.setUserRoleByMatriculationNumber(browser, testUsers[0].matriculationNumber, 0);
        assignedUsers.isUserAssigned(browser, testUsers[0].matriculationNumber, 'l');
        assignedUsers.setUserRoleByMatriculationNumber(browser, testUsers[1].matriculationNumber, 1);
        assignedUsers.isUserAssigned(browser, testUsers[1].matriculationNumber, 't');
        assignedUsers.selectUserByMatriculationNumber(browser, testUsers[2].matriculationNumber);
        assignedUsers.isUserAssigned(browser, testUsers[2].matriculationNumber, 's');
        assignedUsers.setUserRoleByMatriculationNumber(browser, testUsers[3].matriculationNumber, 2);
        assignedUsers.isUserAssigned(browser, testUsers[3].matriculationNumber, 's');
        assignedUsers.submit();
    },
    'create exerciseSheet':  function(browser){
        const courseText = testCourse.number;
        const page = browser.page.courseManagement();
        const exerciseSheetSection = page.section.exerciseSheets;
        const self = this;

        exerciseSheetExists(self, browser, courseText, testExerciseSheet.name, false)

        page.selectCourse(courseText);
        page.selectTab(exerciseSheetsTab);
        const newModal = page.section.exerciseSheetsNewModal;
        const exerciseSheetPage = browser.page.exerciseSheetManagement().section.information;

        const exerciseSheets = [testExerciseSheet];
        for(const exerciseSheet of exerciseSheets){
            exerciseSheetSection.showNewModal();
            newModal.setValue('@name', exerciseSheet.name)
                .setValue('@issueDate', exerciseSheet.issueDate.replace('T', browser.Keys.RIGHT_ARROW))
                .setValue('@submissionDate', exerciseSheet.submissionDate.replace('T', browser.Keys.RIGHT_ARROW))
                .setValue('@description', exerciseSheet.description)
                .setValue('@minKreuzel', exerciseSheet.minKreuzel)
                .setValue('@minPoints', exerciseSheet.minPoints)
                .click(`@kreuzelType${exerciseSheet.kreuzelType}`)
                .submit();
            page.assert.successPresent()
            page.closeToast();
            page.modalNewExerciseSheetNotPresent();
            exerciseSheetSection.exerciseSheetPresentStrict(exerciseSheet);
            this['select exerciseSheet'](browser, exerciseSheet.name);
            
            exerciseSheetPage.assert.value('@name', exerciseSheet.name)
                .assert.value('@issueDate', exerciseSheet.issueDateValue)
                .assert.value('@submissionDate', exerciseSheet.submissionDateValue)
                .assert.containsText('@description', exerciseSheet.description)
                .assert.value('@minKreuzel', exerciseSheet.minKreuzel)
                .assert.value('@minPoints', exerciseSheet.minPoints)
                
            if(exerciseSheet.kreuzelType === 0){
                exerciseSheetPage.expect.element('@kreuzelType0').to.be.selected;
                exerciseSheetPage.expect.element('@kreuzelType1').to.not.be.selected;
            }
            else{
                exerciseSheetPage.expect.element('@kreuzelType1').to.be.selected;
                exerciseSheetPage.expect.element('@kreuzelType0').to.not.be.selected;
            }

            page.navigate().pause(1000);
            page.selectCourse(courseText);
            page.selectTab(exerciseSheetsTab);
        }

    },
    'create exerciseSheet invalid': function(browser){
        const courseText = testCourse.number;
        const page = browser.page.courseManagement();
        const exerciseSheetSection = page.section.exerciseSheets;
        const self = this;
        courseExists(self,browser, courseText, true);

        page.selectCourse(courseText);
        page.selectTab(exerciseSheetsTab);
        const newModal = page.section.exerciseSheetsNewModal;

        exerciseSheetSection.showNewModal();
        for(const exerciseSheet of testExerciseSheetsInvalid){
            newModal
                .clearValue2('@name')
                .clearDate('@issueDate')
                .clearDate('@submissionDate')
                .clearValue('@description') // clear part 1
                .setValue('@description', ` ${browser.Keys.BACK_SPACE}`) //clear part 2 (custom clear for editor)
                .clearValue2('@minKreuzel')
                .clearValue2('@minPoints')
                .setValue('@name', exerciseSheet.name.value)
                .setValue('@issueDate', exerciseSheet.issueDate.value.replace('T', browser.Keys.RIGHT_ARROW))
                .setValue('@submissionDate', exerciseSheet.submissionDate.value.replace('T', browser.Keys.RIGHT_ARROW))
                .setValue('@description', exerciseSheet.description.value)
                .setValue('@minKreuzel', exerciseSheet.minKreuzel.value)
                .setValue('@minPoints', exerciseSheet.minPoints.value)
                .click(`@kreuzelType${exerciseSheet.kreuzelType}`)

            const {description, kreuzelType, ...keys} = exerciseSheet;
            for(const key in keys){
                newModal.assert.isValidInput(`@${key}`, 'valid', exerciseSheet[key].valid)
            }
            if(exerciseSheet.minKreuzel.expected !== undefined){
                newModal.assert.value('@minKreuzel', exerciseSheet.minKreuzel.expected)
            }
            if(exerciseSheet.minPoints.expected !== undefined){
                newModal.assert.value('@minPoints', exerciseSheet.minPoints.expected)
            }
            newModal
                .submit()
                .assert.not.toastPresent();
        }
    },
    'delete exerciseSheet': function(browser, name){
        const courseText = testCourse.number;
        const page = browser.page.courseManagement();
        const exerciseSheetSection = page.section.exerciseSheets;
        const self = this;
        name = name || testExerciseSheet.name;
        const deleteModal = page.section.exerciseSheetDeleteModal;
        exerciseSheetExists(self, browser, courseText, testExerciseSheet.name, true);
        
        page.selectCourse(courseText);
        page.selectTab(exerciseSheetsTab);
        browser.perform(done =>{
            exerciseSheetSection.count(function(countBefore){
                exerciseSheetSection.showDeleteModal(name)
                deleteModal.submit();
                page.assert.successPresent();
                page.closeToast();
                page.modalDeleteExerciseSheetNotPresent();

                exerciseSheetSection.count(function(countAfter){
                    if(countAfter + 1 === countBefore){
                        done();
                    }
                    else{
                        throw new Error('Delete exerciseSheet did not work');
                    }
                });
            })
        })
    },
    'select exerciseSheet': function(browser, name){
        const courseText = testCourse.number;
        const exerciseSheetName = name || testExerciseSheet.name;
        const page = browser.page.courseManagement();
        exerciseSheetExists(this, browser, courseText, exerciseSheetName, true);
        page.selectCourse(courseText);
        page.selectTab(exerciseSheetsTab);

        const exerciseSheetSection = page.section.exerciseSheets;
        exerciseSheetSection.edit(exerciseSheetName);
        page.pause(1000)
        page.expect.url().to.match(/\/Admin\/Course\/([0-9]+)\/SheetManagement\/([0-9]+)$/)
    },
    after: browser =>{
        browser.end();
    }
}