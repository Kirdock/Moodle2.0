const Path = require('path');
const { pathToFileURL } = require('url');
module.exports = {
    before: browser => {
        browser
        .loginAsAdmin()
        .page.courseManagement().navigate();
    },
    'create course invalid': browser => {
        const page = browser.page.courseManagement();
        const modal_new = page.section.modal_new;
        const courses = [
            {
                owner: {
                    value: 1,
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
                    value: 120,
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
                modal_new.setMultiSelect('@owner',owner.value)
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
    'create course valid': browser => {
        const page = browser.page.courseManagement();
        const modal_new = page.section.modal_new;
        page.showNewModal();
        modal_new.setMultiSelect('@owner',1);
        modal_new
            .setValue('@number', '000.001')
            .setValue('@name','TestCourse')
            .setValue('@minKreuzel',20)
            .setValue('@minPoints',30)
            .setValue('@description','Meine Kursbeschreibung')
            .submit();
        page.assert.successPresent();
        page.closeToast();
        page.waitForElementVisible('@container');
        page.assert.urlContains('?courseId=');
    },
    'modal_new close test': browser =>{
        const page = browser.page.courseManagement();
        
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
    'Select course': browser => {
        const page = browser.page.courseManagement();
        page.pause(2000)
            .expect.element('@deleteButton').to.not.be.present
        page.expect.element('@copyButton').to.not.be.present
        page.selectCourse(1);
        page.waitForElementVisible('@container')
            .expect.element('@deleteButton').to.be.present
        page.expect.element('@copyButton').to.be.present
        page.assert.urlContains('?courseId=');
    }
}