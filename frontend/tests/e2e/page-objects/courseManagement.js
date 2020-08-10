const modalCommands = require('./../modalCommands.js');
modalCommands.selectOwner = function(index){
    return this.click(`@owner`)
    // return this.click(`.course_info .multiselect__element:nth-child(${index+1})>span`)
}
const createCommands = {
    selectCourse: function(){
        this.click('#selectedCourse option[value="1"]');
    },
    showNewModal: function(){
        this.click('@newButton');
        return this.section.modal_new.waitForElementVisible('@submitButton', 1000);
    },
    modalNewPresent: function(){
        return this.expect.section('@modal_new').to.be.visible;
    },
    modalNewNotPresent: function(){
        return this.expect.section('@modal_new').to.not.be.present;
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
        selectCourse: '#selectedCourse',
        container: '.tabs'
    },
    sections: {
        modal_new: {
            selector: '.modal',
            commands: [modalCommands],
            elements: {
                owner: '.course_info .multiselect',
                ownerInput: {
                    selector: '.course_info input[type="text"]',
                    index: 0
                },
                number: {
                    selector: '.course_info input[type="text"]',
                    index: 1
                },
                name: {
                    selector: '.course_info input[type="text"]',
                    index: 2
                },
                minKreuzel: {
                    selector: '.course_info input[type="number"]',
                    index: 0
                },
                minPoints: {
                    selector: '.course_info input[type="number"]',
                    index: 1
                },
                description: '.course_info .note-editable',
                submitButton: '.modal-footer button.btn.btn-primary',
            }
        }
    }
}