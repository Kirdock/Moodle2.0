const modalCommands = require('./../modalCommands.js');
const createCommands = {
    selectTab: function(index){
        const element = `.tabs .nav-item:nth-child(${index+1})`;
        this.click(element).pause(1000).assert.cssClassPresent(`${element} a`,'active');
    },
    selectCourse: function(value){
        this.click('xpath',`//select[@id="selectedCourse"]/option[contains(text(),"${value}")]`).pause(2000);
    },
    showNewModal: function(){
        this.click('@newButton');
        return this.section.modal_new.waitForElementVisible('@submitButton', 1000);
    },
    showDeleteModal: function(){
        this.click('@deleteButton');
        return this.section.modal_delete.waitForElementVisible('@submitButton', 1000);
    },
    modalNewPresent: function(){
        return this.expect.section('@modal_new').to.be.visible;
    },
    modalNewNotPresent: function(){
        return this.expect.section('@modal_new').to.not.be.present;
    },
    modalDeleteNotPresent: function(){
        return this.expect.section('@modal_delete').to.not.be.present;
    },
    ownerExists: function(browser, owner, callback){
        browser.element('xpath', `//li[@class="multiselect__element"]/span/span[contains(text(),"${owner}")]`, callback)
    },
    modalNewExerciseSheetNotPresent: function(){
        return this.expect.section('@exerciseSheetsNewModal').to.not.be.present;
    },
    modalNewExerciseSheetPresent: function(){
        return this.expect.section('@exerciseSheetsNewModal').to.be.visible;
    },
    modalDeleteExerciseSheetNotPresent: function(){
        return this.expect.section('@exerciseSheetDeleteModal').to.not.be.present;
    }
}
const assignedUsersCommands = {
    selectRole: function(index){
        this.click(`#showRoles option:nth-child(${index+1})`).pause(2000)
    },
    userAssignedAsStudent: function(self, user){
        self.perform(function (done){
            self.element('xpath',`//div[@id="assignedUsers"]//table//tr[td[2][contains(text(),"${user.matriculationNumber}")] and td[3][contains(text(),"${user.surname}")] and td[4][contains(text(),"${user.forename}")]]/td/select`, function (result){
                if(result.value.ELEMENT){
                    self.elementIdAttribute(result.value.ELEMENT, 'value', function(selectedValue){
                        if(selectedValue.value !== 's'){
                            throw new Error('Wrong selected role');
                        }
                        else{
                            self.logSuccess('User \033[33m ['+JSON.stringify(user)+']\033[0m created right')
                            done();
                        }
                    })
                }
                else{
                    throw new Error('User not found');
                }
            })
        })
    },
    isUserAssigned(self, matriculationNumber, role){
        self.perform(function (done){
            self.element('xpath', `//div[@id="assignedUsers"]//table//tr[td[2][contains(text(),"${matriculationNumber}")]]`, function(row){
                if(row.value.ELEMENT){
                    self.perform(function(done2){
                        self.elementIdElement(row.value.ELEMENT, 'css selector', 'input[type=checkbox]', function(checkbox){
                            if(checkbox.value.ELEMENT){
                                self.elementIdAttribute(checkbox.value.ELEMENT, 'checked', function(selectedValue){
                                    if(selectedValue.value === true.toString()){
                                        done2();
                                    }
                                    else{
                                        throw new Error('Checkbox is not checked');
                                    }
                                });
                            }
                            else{
                                throw new Error('Checkbox not found')
                            }
                        })
                    }).perform(function (done2){
                        self.elementIdElement(row.value.ELEMENT, 'css selector', 'select', function(dropdown){
                            if(dropdown.value.ELEMENT){
                                self.elementIdAttribute(dropdown.value.ELEMENT, 'value', function(selectedValue){
                                    if(selectedValue.value && selectedValue.value === role){
                                        done2();
                                    }
                                    else{
                                        throw new Error(`User ${matriculationNumber} does not have a role but is selected`);
                                    }
                                })
                            }
                            else{
                                throw new Error('Dropdown not found')
                            }
                        })
                    }).perform(function (){
                        self.logSuccess('User \033[33m ['+matriculationNumber+']\033[0m assigned right')
                        done();
                    })
                }
                else{
                    throw new Error('User not found')
                }
            })
        })
    },
    setUserRoleByMatriculationNumber(self, matriculationNumber, index){
        self.click('xpath',`//div[@id="assignedUsers"]//table//tr[td[2][contains(text(),"${matriculationNumber}")]]/td/select/option[${index+1}]`);
    },
    selectUserByMatriculationNumber(self, matriculationNumber){
        self.click('xpath',`//div[@id="assignedUsers"]//table//tr[td[2][contains(text(),"${matriculationNumber}")]]/td[1]/input[@type="checkbox"]`);
    },
    submit: function(){
        this.click('@submitButton')
    }
}

module.exports = {
    url: '/Admin/CourseManagement',
    commands: [createCommands],
    elements: {
        newButton: {
            selector: 'button.btn-primary',
            index: 0
        },
        copyButton: {
            selector: 'button.btn-primary',
            index: 1
        },
        deleteButton: 'button.btn-danger',
        selectSemester: '#courseSemester_edit',
        selectSemesterOption: '#courseSemester_edit option',
        selectCourse: '#selectedCourse',
        selectCourseX: '//select[@id="selectedCourse"]',
        container: '.tabs'
    },
    sections: {
        modal_new: {
            selector: '#modal-new-course',
            commands: [modalCommands],
            elements: {
                owner: '#modal-new-course .course_info .multiselect',
                ownerInput: {
                    selector: '#modal-new-course .course_info input[type="text"]',
                    index: 0
                },
                number: {
                    selector: '#modal-new-course .course_info input[type="text"]',
                    index: 1
                },
                name: {
                    selector: '#modal-new-course .course_info input[type="text"]',
                    index: 2
                },
                minKreuzel: {
                    selector: '#modal-new-course .course_info input[type="number"]',
                    index: 0
                },
                minPoints: {
                    selector: '#modal-new-course .course_info input[type="number"]',
                    index: 1
                },
                description: '#modal-new-course .course_info .note-editable',
                submitButton: '.modal-footer button.btn.btn-primary',
            }
        },
        modal_delete: {
            selector: '#modal-delete-course',
            commands: [modalCommands],
            elements: {
                submitButton: '.modal-footer button.btn.btn-primary'
            }
        },
        courseInfo: {
            selector: '#courseInfo',
            commands: [
                {
                    submit: function() {
                        return this.click('@submitButton');
                    }
                }
            ],
            elements: {
                owner: '#courseInfo .multiselect',
                ownerText: '#courseInfo .multiselect .multiselect__single',
                ownerInput: {
                    selector: '#courseInfo input[type="text"]',
                    index: 0
                },
                number: {
                    selector: '#courseInfo input[type="text"]',
                    index: 1
                },
                name: {
                    selector: '#courseInfo input[type="text"]',
                    index: 2
                },
                minKreuzel: {
                    selector: '#courseInfo input[type="number"]',
                    index: 0
                },
                minPoints: {
                    selector: '#courseInfo input[type="number"]',
                    index: 1
                },
                description: '#courseInfo .note-editable',
                submitButton: '#courseInfo button.btn.btn-primary',
            }
        },
        assignedUsers: {
            selector: '#assignedUsers',
            commands: [assignedUsersCommands],
            elements: {
                roleSelect: '#showRoles',
                table: '#assignedUsers table',
                tableEntries: '#assignedUsers table tr',
                tableEntriesCheck: '#assignedUsers table tr input[type="checkbox"]',
                uploadCsv: '#assignedUsers input[type=file]',
                submitButton: {
                    selector: '#assignedUsers button',
                    index: 3
                }
            }
        },
        exerciseSheets: {
            selector: '#exerciseSheets',
            commands: [{
                showNewModal: function(){
                    this.click('@newButton');
                    return this.api.waitForElementVisible(this.elements.submitButton.selector).pause(1000);
                },
                showDeleteModal: function(name){
                    this.api.click('xpath',`//table//tr[td[1][contains(text(),"${name}")]]/td[3]/a[3]`);
                    return this.api.waitForElementVisible(this.elements.submitButton.selector).pause(1000);
                },
                edit: function(name){
                    return this.api.click('xpath',`//table//tr[td[1][contains(text(),"${name}")]]/td[3]/a[1]`);
                },
                exerciseSheetPresent: function(browser, name, callback){
                    browser.elements('xpath', `//div[@id="exerciseSheets"]//table//tr[td[1][contains(text(),"${name}")]]`, callback)
                },
                exerciseSheetPresentStrict: function(exerciseSheet){
                    return this.assert.elementPresent({
                        selector: `//table//tr[td[1][contains(text(),"${exerciseSheet.name}")] and td[2][contains(text(),"${exerciseSheet.submissionDateFormat}")]]`,
                        locateStrategy: 'xpath'
                    })
                },
                count: function(callback){
                    this.api.execute(function(selector){
                        return document.querySelectorAll(selector).length;
                    }, [this.elements.tableEntries.selector], function(result){
                        callback(result.value);
                    })
                }
            }],
            elements: {
                tableEntries: '#exerciseSheets table tr',
                newButton: '#exerciseSheets button',
                submitButton: '.modal-footer button.btn.btn-primary'
            }
        },
        exerciseSheetsNewModal: {
            selector: '#modal-new-exerciseSheet',
            commands: [modalCommands],
            elements: {
                name: {
                    selector: '#modal-new-exerciseSheet input',
                    index: 0
                },
                issueDate: {
                    selector: '#modal-new-exerciseSheet input[type=datetime-local]',
                    index: 0
                },
                submissionDate: {
                    selector: '#modal-new-exerciseSheet input[type=datetime-local]',
                    index: 1
                },
                description: '#modal-new-exerciseSheet .note-editable',
                minKreuzel: {
                    selector: '#modal-new-exerciseSheet input[type=number]',
                    index: 0
                },
                minPoints: {
                    selector: '#modal-new-exerciseSheet input[type=number]',
                    index: 1
                },
                kreuzelType1: {
                    selector: '#modal-new-exerciseSheet input[type=radio]',
                    index: 0
                },
                kreuzelType2: {
                    selector: '#modal-new-exerciseSheet input[type=radio]',
                    index: 1
                },
                submitButton: '#modal-new-exerciseSheet .modal-footer button.btn.btn-primary'
            }
        },
        exerciseSheetDeleteModal: {
            selector: '#modal-delete-exerciseSheet',
            commands: [modalCommands],
            elements: {
                submitButton: '.modal-footer button.btn.btn-primary'
            }
        }
    }
}