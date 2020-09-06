const Path = require('path');
const testCourses = require('./testFiles/testCourses.js');
const testCourse = testCourses[0];
const testExerciseSheets = require('./testFiles/testExerciseSheets.js');
const testExerciseSheet = testExerciseSheets[0];
const testExerciseSheetsInvalid = require('./testFiles/testExerciseSheetsInvalid.js');
const testUsers = require('./testFiles/testUsers.js');
const testKreuzel = require('./testFiles/testKreuzel.js');
const testKreuzel2 = require('./testFiles/testKreuzel2.js');
const exerciseSheetsTab = 2;
const assignUsersTab = 1;
const informationTab = 0;

function beforeKreuzel(self, browser){
    const coursePage = browser.page.courseManagement();
    const modifyExerciseSheet = require('./modifyExerciseSheet.js');
    self['create course'](browser);
    self['assign users'](browser);
    
    for(const sheet of testExerciseSheets){
        browser.perform(done=>{
            self['select exerciseSheet'](browser, sheet.name, false)
            modifyExerciseSheet['create example'](browser);
            coursePage.navigate().pause(1000)
            done();
        })
    }
}

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

                require('./modifyUsers.js')['upload CSV file'](browser);
                
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
                    self['create exerciseSheet'](browser, name);
                }
                done();
            }
        });
    })
}

module.exports = {
    before: (browser) => {
        const page = browser
            .loginAsAdmin()
            .page.courseManagement()
        page.navigate().pause(1000);
        browser.perform(done =>{
            //check if semester exists
            browser.element('css selector', page.elements.selectSemesterOption.selector,function(result){
                if(result.value && result.value.ELEMENT){
                    page.pause(1000, done);
                }
                else{
                    const semester = require('./modifySemester.js')
                    browser.page.semesterManagement().navigate();
                    browser.perform(() =>{
                        semester['create semester'](browser, true);
                    }).perform(()=>{
                        page.navigate();
                        page.pause(1000, done);
                    })
                }
            });
        })
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
                page.showNewModal()

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
    'select course': function (browser) {
        const page = browser.page.courseManagement();
        page.navigate().pause(1000); //with refresh courseId is still set
        courseExists(this, browser, testCourse.number, true)

        page.expect.element('@deleteButton').to.not.be.present
        page.expect.element('@copyButton').to.not.be.present
        page.selectCourse(testCourse.number);
        page.expect.element('@deleteButton').to.be.present
        page.expect.element('@copyButton').to.be.present
        page.assert.urlContains('?courseId=');
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
    'modify course': function(browser, courseText, isOwner = false){
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
        
        const page = browser.page.courseManagement();
        const courseInfo = page.section.courseInfo;
        page.selectCourse(courseText);
        page.selectTab(informationTab);
        
        for(const course of courseData){
            const {owner, description, name, number, ...data} = course;
            if(!isOwner){
                data.name = name;
                data.number = number;
                courseInfo.setMultiSelect('@owner',course.owner.index, course.owner.value)
                    .assert.isValidInput(`@ownerInput`, 'valid', owner.valid)
            }
            courseInfo
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
    'assign users csv': function (browser, courseText){
        courseText = courseText || testCourse.number;
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
    'create exerciseSheet':  function(browser, name){
        const courseText = testCourse.number;
        const page = browser.page.courseManagement();
        const exerciseSheetSection = page.section.exerciseSheets;
        const self = this;
        const newModal = page.section.exerciseSheetsNewModal;
        const exerciseSheetPage = browser.page.exerciseSheetManagement().section.information;
        const exerciseSheets = [];
        if(name === undefined){
            exerciseSheets.push(testExerciseSheets[0]);
        }
        else if(name instanceof Array){
            for(const sheetName of name){
                exerciseSheets.push(testExerciseSheets.find(sheet => sheet.name === sheetName))
            }
        }
        else{
            exerciseSheets.push(testExerciseSheets.find(sheet => sheet.name === name));
        }

        for(const exerciseSheet of exerciseSheets){
            browser.perform(done =>{
                exerciseSheetExists(self, browser, courseText, exerciseSheet.name, false)
                page.selectCourse(courseText);
                page.selectTab(exerciseSheetsTab);

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
                self['select exerciseSheet'](browser, exerciseSheet.name, false);
                
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
                done();
            })
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
            browser.perform(done => {
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
                done();
            })
        }
        newModal.cancelX();
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
    'select exerciseSheet': function(browser, name, goBack = true, courseText){
        courseText = courseText || testCourse.number;
        const exerciseSheetName = name || testExerciseSheet.name;
        const page = browser.page.courseManagement();
        exerciseSheetExists(this, browser, courseText, exerciseSheetName, true);
        page.selectCourse(courseText);
        page.selectTab(exerciseSheetsTab);

        const exerciseSheetSection = page.section.exerciseSheets;
        exerciseSheetSection.edit(exerciseSheetName);
        page.pause(1000)
        page.expect.url().to.match(/\/Admin\/Course\/([0-9]+)\/SheetManagement\/([0-9]+)$/)
        if(goBack){
            page.navigate().pause(1000)
        }
    },
    'kreuzel test user': function(browser, isOwner = false, courseText){
        const coursePage = browser.page.courseManagement();
        const page = coursePage.section.assignedUsers;
        const modifyKreuzel = require('./modifyKreuzel.js');
        
        if(!isOwner){
            beforeKreuzel(this, browser)
        }
        
        browser.logout();
        browser.loginAsStudent();
        modifyKreuzel['modify exerciseSheet type1'](browser)
        modifyKreuzel['modify exerciseSheet type2'](browser)

        browser.logout();
        if(isOwner){
            browser.loginAsOwner();
        }
        else{
            browser.loginAsAdmin()
        }
        browser.page.courseManagement().navigate();

        this['select course'](browser, courseText);
        coursePage.selectTab(assignUsersTab);
        const kreuzelModal = coursePage.section.kreuzelModal;
        page.showKreuzelModal();

        for(const kreuzelInfo of testKreuzel.concat(testKreuzel2)){
            browser.perform(done => {
                kreuzelModal.selectExerciseSheet(kreuzelInfo.exerciseSheet.name)
                kreuzelModal.validateUser(testUsers[3], kreuzelInfo.examples)
                done();
            })
        }
        kreuzelModal.cancelX();
    },
    'kreuzel test': function(browser, skipCreation = false, courseText){
        const coursePage = browser.page.courseManagement();
        const page = coursePage.section.assignedUsers;
        let kreuzelInfo = JSON.parse(JSON.stringify(testKreuzel));
        kreuzelInfo = kreuzelInfo[0];
        kreuzelInfo.exerciseSheet = testExerciseSheets[3];
        
        let kreuzelInfo2 = JSON.parse(JSON.stringify(testKreuzel2));
        kreuzelInfo2 = kreuzelInfo2[0];
        kreuzelInfo2.exerciseSheet = testExerciseSheets[2];
        
        if(!skipCreation){
            beforeKreuzel(this, browser)
            browser.logout();
            browser.loginAsAdmin()
                .page.courseManagement().navigate()
        }
        this['select course'](browser, courseText);
        coursePage.selectTab(assignUsersTab);
        const kreuzelModal = coursePage.section.kreuzelModal;
        page.showKreuzelModal();

        kreuzelModal.selectExerciseSheet(kreuzelInfo.exerciseSheet.name)
        kreuzelModal.enterEditMode().pause(1000);

        for(const kreuzel of [kreuzelInfo, kreuzelInfo2]){
            browser.perform(done =>{
                kreuzelModal.selectExerciseSheet(kreuzel.exerciseSheet.name);
                kreuzelModal.setKreuzelInfo(testUsers[3], kreuzel.examples);
                kreuzelModal.submit();
                kreuzelModal.assert.successPresent();
                kreuzelModal.closeToast();
                done();
            })
        }
        
        this['select course'](browser, courseText);
        coursePage.selectTab(assignUsersTab);
        page.showKreuzelModal();
        for(const kreuzel of [kreuzelInfo, kreuzelInfo2]){
            browser.perform(done =>{
                kreuzelModal.selectExerciseSheet(kreuzel.exerciseSheet.name);
                kreuzelModal.validateUser(testUsers[3], kreuzel.examples, true);
                done();
            })
        }
        kreuzelModal.cancelX();
    },
    'modify presentation': function(browser, isOwner = false, courseText){
        const coursePage = browser.page.courseManagement();
        const page = coursePage.section.assignedUsers;
        const presentationModal = coursePage.section.presentationModal;
        const allKreuzel = testKreuzel.concat(testKreuzel2);
        this['kreuzel test user'](browser, isOwner, courseText) //to make sure, exercise sheets and course have correct data, create everything new
        this['select course'](browser, courseText);
        coursePage.selectTab(assignUsersTab);

        page.showPresentationModal();
        presentationModal.studentPresent(testUsers[3]);
        presentationModal.studentPresent(testUsers[2]);

        presentationModal.selectStudent(testUsers[2])
        presentationModal.checkExerciseSheetCount(0)
        presentationModal.checkExampleCount(0)

        presentationModal.selectStudent(testUsers[3])
        presentationModal.checkExerciseSheetCount(2)
        presentationModal.checkExampleCount(5)
        
        for(const kreuzel of allKreuzel){ //test if every exercise sheet and example that was kreuzelt is present
            browser.perform(done =>{
                presentationModal.exerciseSheetPresent(kreuzel.exerciseSheet.name);
                for(const example of kreuzel.examples){
                    browser.perform(done2 => {
                        if(example.type === 3 || example.type === true || example.type === 1){
                            presentationModal.examplePresent(example.name);
                        }
                        done2();
                    })
                }
                done();
            })
        }

        for(const kreuzel of allKreuzel){ //test if only kreuzelt examples of specific exercise sheet are present
            browser.perform(done =>{
                presentationModal.selectExerciseSheet(kreuzel.exerciseSheet.name);
                for(const example of kreuzel.examples){
                    browser.perform(done2 => {
                        if(example.type === 3 || example.type === true || example.type === 1){
                            presentationModal.examplePresent(example.name);
                        }
                        done2();
                    })
                }
                done();
            })
        }
            //invalid submit check
            presentationModal.selectStudent(testUsers[2])
            presentationModal.submit();
            presentationModal.assert.not.toastPresent();
            presentationModal.checkPresentationCount(0);

        presentationModal.selectStudent(testUsers[3])

        //enter by example name
        let presentationInfo1 = {exerciseSheet: testKreuzel[0].exerciseSheet.name, example: testKreuzel[0].examples[0].name};

        presentationModal.selectExample(presentationInfo1.example)
        presentationModal.submit();
        presentationModal.assert.successPresent();
        presentationModal.closeToast();
            //duplicate check
            presentationModal.submit();
            presentationModal.assert.warningPresent();
            presentationModal.closeToast();
        
        presentationModal.presentationPresent(testUsers[3], presentationInfo1)

        //enter by exerciseSheet name and example name
        let presentationInfo2 = {exerciseSheet: testKreuzel2[0].exerciseSheet.name, example: testKreuzel2[0].examples[0].name};
        presentationModal.selectExerciseSheet(presentationInfo2.exerciseSheet)
        presentationModal.selectExample(presentationInfo2.example)
        presentationModal.submit();
        presentationModal.assert.successPresent();
        presentationModal.closeToast();
        presentationModal.presentationPresent(testUsers[3], presentationInfo2)

        page.checkPresentationCount(testUsers[3], 2);

        //delete example
        presentationModal.deletePresentation(testUsers[3], presentationInfo2)
        presentationModal.assert.successPresent();
        presentationModal.closeToast();
        presentationModal.presentationNotPresent(testUsers[3], presentationInfo2)
        page.checkPresentationCount(testUsers[3], 1);

        //refresh and check
        this['select course'](browser, courseText);
        coursePage.selectTab(assignUsersTab);
        page.showPresentationModal();
        presentationModal.presentationPresent(testUsers[3], presentationInfo1)
        page.checkPresentationCount(testUsers[3], 1);
        presentationModal.cancelX();
    },
    after: browser =>{
        browser.end();
    }
}