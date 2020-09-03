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
            selector: '.courseManagement>div:nth-of-type(1) button.btn-primary',
            index: 0
        },
        copyButton: {
            selector: '.courseManagement>div:nth-of-type(1) button.btn-primary',
            index: 1
        },
        deleteButton: '.courseManagement>div:nth-of-type(1) button.btn-danger',
        selectSemester: '#courseSemester_edit',
        selectSemesterOption: '#courseSemester_edit option',
        selectCourse: '#selectedCourse',
        selectCourseX: '//select[@id="selectedCourse"]',
        container: '.tabs',
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
                    selector: '#courseInfo input[type="text"].form-control', //.form-control to differentiate between ownerInput and to be more persistent when ownerInput is not present
                    index: 0
                },
                name: {
                    selector: '#courseInfo input[type="text"].form-control',
                    index: 1
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
            commands: [assignedUsersCommands, {
                showKreuzelModal: function(){
                    return this.click('@kreuzelButton')
                },
                showPresentationModal: function(){
                    return this.click('@presentationButton').pause(1000)
                },
                checkPresentationCount(user, count){
                    return this.assert.elementPresent({
                        selector: `//div[@id="assignedUsers"]//tr[td[2][text() =" ${user.matriculationNumber} "] and td[5][text() = " ${count} "]]`,
                        locateStrategy: 'xpath'
                    })
                }
            }],
            elements: {
                roleSelect: '#showRoles',
                table: '#assignedUsers table',
                tableEntries: '#assignedUsers table tr',
                tableEntriesCheck: '#assignedUsers table tr input[type="checkbox"]',
                uploadCsv: '#assignedUsers input[type=file]',
                submitButton: {
                    selector: '#assignedUsers button',
                    index: 3
                },
                kreuzelButton: '#assignedUsers>div>div.form-inline>div:nth-of-type(1)',
                presentationButton: '#assignedUsers>div>div.form-inline>div:nth-of-type(2)'
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
                kreuzelType0: {
                    selector: '#modal-new-exerciseSheet input[type=radio]',
                    index: 0
                },
                kreuzelType1: {
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
        },
        kreuzelModal: {
            selector: '#modal-kreuzelList',
            commands: [modalCommands, {
                selectExerciseSheet(exerciseSheetName){
                    return this.api.click('xpath',`//div[@id="modal-kreuzelList"]//div[@class="form-group"][1]/select/option[text()=" ${exerciseSheetName} "]`).pause(2000);
                },
                validateUser(user, examples, withoutDescription = false){
                    let validation = `//div[@id="modal-kreuzelList"]//tr[td[1][text() = " ${user.matriculationNumber} "] and td[2][text() = " ${user.surname} "] and td[3][text() = " ${user.forename} "]`;
                    const offset = 4;
                    const self = this;
                    for(let i = 0; i < examples.length; i++){
                        validation += ` and td[${i+offset}][text() =" ${examples[i].type === 1 || examples[i].type === true ? 'X' : examples[i].type === 3 ? 'O' : ''} " ${examples[i].type === 3 && !withoutDescription ? `and textarea[@readonly="readonly"]` : ''}]`;
                        if(examples[i].type === 3 && !withoutDescription){
                            this.api.perform(done => {
                                self.api.assert.value({
                                    selector: `//div[@id="modal-kreuzelList"]//tr[td[1][text() = " ${user.matriculationNumber} "]]/td[${i+offset}]/textarea`,
                                    locateStrategy: 'xpath'
                                }, examples[i].description)
                                done();
                            })
                        }
                    }
                    return this.api.assert.elementPresent({
                        selector: validation + ']',
                        locateStrategy: 'xpath'
                    })
                },
                setKreuzelInfo(user, examples){
                    const offset = 4;
                    const self = this;
                    for(let i = 0; i < examples.length; i++){
                        const option = examples[i].type === 1 || examples[i].type === true ? 'X' : examples[i].type === 3 ? 'O' : false;
                        self.api.perform(done =>{
                            self.api.click('xpath', `//div[@id="modal-kreuzelList"]//tr[td[1][text() = " ${user.matriculationNumber} "]]/td[${i+offset}]/select/option[${option === false ? 'last()' : `text()=" ${option} "`}]`)
                            done();
                        })
                    }
                },
                enterEditMode(){
                    return this.click('@editButton');
                },
                submit(){
                    return this.click('@saveButton');
                }
            }],
            elements: {
                editButton: '#modal-kreuzelList .custom-control.custom-switch label',
                saveButton: '.kreuzelList>button'
            }
        },
        presentationModal: {
            selector: '#modal-presented',
            commands: [modalCommands, {
                selectStudent(user){
                    return this.setMultiSelect('@studentSelect', undefined, `${user.matriculationNumber} ${user.surname} ${user.forename}`).pause(1000)
                },
                selectExerciseSheet(name){
                    return this.setMultiSelect('@exerciseSheetSelect', undefined, `${name}`).pause(1000)
                },
                selectExample(name){
                    return this.setMultiSelect('@exampleSelect', undefined, `${name}`).pause(2000)
                },
                checkExerciseSheetCount(count){
                    return this.assert.elementCount('#modal-presented .form-inline>div:nth-of-type(2) .multiselect .multiselect__element', count)
                },
                checkExampleCount(count){
                    return this.assert.elementCount('#modal-presented .form-inline>div:nth-of-type(3) .multiselect .multiselect__element', count)
                },
                checkPresentationCount(count){
                    return this.assert.elementCount('#modal-presented tr', count);
                },
                studentPresent(user){
                    return this.assert.elementPresent({
                        selector: `//div[@id="modal-presented"]//div[@class="form-inline"]/div[1]//li[@class="multiselect__element"]/span/span[text()="${user.matriculationNumber} ${user.surname} ${user.forename}"]`,
                        locateStrategy: 'xpath'
                    })
                },
                exerciseSheetPresent(name){
                    return this.assert.elementPresent({
                        selector: `//div[@id="modal-presented"]//div[@class="form-inline"]/div[2]//li[@class="multiselect__element"]/span/span[text()="${name}"]`,
                        locateStrategy: 'xpath'
                    })
                },
                examplePresent(name){
                    return this.assert.elementPresent({
                        selector: `//div[@id="modal-presented"]//div[@class="form-inline"]/div[3]//li[@class="multiselect__element"]/span/span[text()="${name}"]`,
                        locateStrategy: 'xpath'
                    })
                },
                presentationPresent(user, info, negate = false){
                    return (negate ? this.assert.not : this.assert).elementPresent({
                        selector: `//div[@id="modal-presented"]//tr[td[1][text()=" ${user.matriculationNumber} "] and td[2][text()=" ${user.surname} "] and td[3][text()=" ${user.forename} "] and td[4][text()=" ${info.exerciseSheet} "] and td[5][text()=" ${info.example} "]]`,
                        locateStrategy: 'xpath'
                    })
                },
                presentationNotPresent(user, info){
                    return this.presentationPresent(user, info, true)
                },
                deletePresentation(user, info){
                    return this.api.click('xpath',`//div[@id="modal-presented"]//tr[td[1][text()=" ${user.matriculationNumber} "] and td[2][text()=" ${user.surname} "] and td[3][text()=" ${user.forename} "] and td[4][text()=" ${info.exerciseSheet} "] and td[5][text()=" ${info.example} "]]/td[6]/a`).pause(1000)
                },
                submit(){
                    return this.click('@submitButton').pause(1000)
                },
                selectFilterExerciseSheet(name = 'Alle'){
                    return this.click({
                        selector: `//div[@id="modal-presented"]//select/option[text()=" ${name} "]`,
                        locateStrategy: 'xpath'
                    })
                }
            }],
            elements: {
                studentSelect: '#modal-presented .form-inline>div:nth-of-type(1) .multiselect',
                exerciseSheetSelect: '#modal-presented .form-inline>div:nth-of-type(2) .multiselect',
                exampleSelect: '#modal-presented .form-inline>div:nth-of-type(3) .multiselect',
                submitButton: '#modal-presented .form-inline>div button'
            }
        }
    }
}