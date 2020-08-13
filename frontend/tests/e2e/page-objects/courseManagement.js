const modalCommands = require('./../modalCommands.js');
const createCommands = {
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
            selector: '#assignedUsers'
        }
    }
}