const Path = require('path');
const testCourse = {
    owner: {
        index: undefined,
        value: 'Owner'
    },
    number: '000.001',
    name: 'Mein erster Kurs',
    minKreuzel: '20',
    minPoints: '30',
    description: 'Meine Kursbeschreibung'
}
const testExerciseSheet = {
    name: 'TestExerciseSheet',
    issueDate: '31-08-2021T23:55',
    submissionDate: '30-09-2021T23:50',
    submissionDateFormat: '30.9.2021, 23:50:00',
    description: 'My first exercise sheet',
    minKreuzel: '50',
    minPoints: '40',
    kreuzelType: 0
}
const testUsers = require('./../testUsers.js');
const userTest = require('./modifyUsers.js');

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

function ownerExists(browser, name, done){
    const page = browser.page.courseManagement();
    page.showNewModal()
    page.pause(1000);
    browser.perform(function(){ //done not possible because timeout reaches 10s
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
        page.selectTab(2);
        const exerciseSheet = page.section.exerciseSheets;
        exerciseSheet.exerciseSheetPresent(browser, name, function(result){
            // if (result.value && result.value.length !== 0) {
            //     // Elements are present
            //     if(!create){
            //         browser.log('Course already available. Course will now be deleted')
            //         self['delete course'](browser, courseText);
            //     }
            //     done();
            // } else {
            //     if(create){
            //         browser.log('Course does not exist. Course will now be created')
            //         self['create course'](browser);
            //     }
            //     done();
            // }
            done();
        });
        // done();
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
            const {owner, ...keys} = course;
            for(const data in keys){
                modal_new
                .setValue(`@${data}`, course[data].value);
                if(course[data].expected !== undefined){
                    modal_new.assert.value(`@${data}`,course[data].expected)
                }
                if(course[data].valid !== undefined){
                    modal_new.assert.isValidInput(`@${data}`, 'valid', course[data].valid)
                }
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

        courseExists(this,browser, testCourse.number, false)
        ownerExists(browser,testCourse.owner.value, next)
        
        function next(){
            const modal_new = page.section.modal_new;
            const courseInfo = page.section.courseInfo;
            page.showNewModal();
            page.pause(1000);
            const courses = [testCourse]

            for(const course of courses){
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
            }
        }
    },
    'modify course invalid': function(browser, number){
        number = number || testCourse.number;
        courseExists(this,browser, number, true);

        const page = browser.page.courseManagement();
        const courseInfo = page.section.courseInfo;
        page.selectCourse(number);

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

            courseInfo.setMultiSelect('@owner',course.owner.index, course.owner.value);
            courseInfo.assert.isValidInput(`@ownerInput`, 'valid', owner.valid);
            courseInfo.clearValue('@description')
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
        courseExists(self, browser, `${courseData[0].number.value} ${courseData[0].name.value}`, false);
        
        const page = browser.page.courseManagement();
        const courseInfo = page.section.courseInfo;
        page.selectCourse(courseText);
        
        
        for(const course of courseData){
            const {owner, description, ...data} = course;

            courseInfo.setMultiSelect('@owner',course.owner.index, course.owner.value);
            courseInfo.assert.isValidInput(`@ownerInput`, 'valid', owner.valid);
            courseInfo.clearValue('@description')
            courseInfo.setValue('@description', description.value)
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
        page.selectTab(1);
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
        page.selectTab(1);
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
        page.selectTab(2);
        const newModal = page.section.exerciseSheetsNewModal;

        const exerciseSheets = [testExerciseSheet];
        for(const exerciseSheet of exerciseSheets){
            exerciseSheetSection.showNewModal();
            newModal.setValue('@name', exerciseSheet.name)
                .setValue('@issueDate', exerciseSheet.issueDate.replace('T', browser.Keys.RIGHT_ARROW))
                .setValue('@submissionDate', exerciseSheet.submissionDate.replace('T', browser.Keys.RIGHT_ARROW))
                .setValue('@description', exerciseSheet.description)
                .setValue('@minKreuzel', exerciseSheet.minKreuzel)
                .setValue('@minPoints', exerciseSheet.minPoints)
            if(exerciseSheet.kreuzelType === 0){
                newModal.click('@kreuzelType1')
            }
            else{
                newModal.click('@kreuzelType2')
            }
            newModal.submit();
            page.assert.successPresent()
            page.closeToast();
            page.modalNewExerciseSheetNotPresent();
            exerciseSheetSection.exerciseSheetPresentStrict(exerciseSheet);
        }

    },
    after: browser =>{
        browser.end();
    }
}