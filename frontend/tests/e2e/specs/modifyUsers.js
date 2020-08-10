const Path = require('path');
module.exports = {
    before: browser => {
        browser
        .loginAsAdmin()
        .page.userManagement().navigate();
    },
    'Create: invalid input': browser => {
        const page = browser.page.userManagement();
        page.showModalNew();
        const modalNew = page.section.modal_new;
        const userData = [
            {
                username: {
                    value: 'TestStudent',
                    valid: true
                },
                surname: {
                    value: 'mySurname',
                    valid: true
                },
                forename: {
                    value: 'myForename',
                    valid: true
                },
                isAdmin: {
                    value: false,
                    valid: true
                },
                email: {
                    value: 'testEmail@com',
                    valid: true
                },
                matriculationNumber: {
                    value: 'abc',
                    valid: false
                },
            }
        ]

        for(const user of userData){
            const {isAdmin, ...keys} = user;
            for(const data in keys){
                modalNew
                .clearValue2(`@${data}`)
                .setValue(`@${data}`, user[data].value)
                .assert.isValidInput(`@${data}`, 'valid', user[data].valid)
            }
            modalNew
            .setCheckbox(`@isAdmin`, isAdmin)
            .submit()
            .assert.not.errorPresent();
            page.isUserModalPresent()
        }
        // browser.end();
    },
    'Create: reopen': browser => {
        const page = browser.page.userManagement();
        page.showModalNew();
        const modalNew = page.section.modal_new;
        const user = {
            username: 'TestStudent',
            surname: 'mySurname',
            forename: 'myForename',
            isAdmin: false,
            email: 'testEmail@com',
            matriculationNumber: '98765432'
        }
        

        const {isAdmin, ...keys} = user;
        for(const data in keys){
            modalNew
            .clearValue2(`@${data}`)
            .setValue(`@${data}`, user[data])
            .assert.isValidInput(`@${data}`, 'valid', true)
        }
        modalNew
        .setCheckbox(`@isAdmin`, isAdmin)
        .cancel();

        page.showModalNew();
        for(const data in keys){
            modalNew.assert.value(`@${data}`,'');
        }
        modalNew.expect.element(`@isAdmin`).to.not.be.selected
    },
    'Create: Valid input': browser =>{
        const page = browser.page.userManagement();
        page.showModalNew();
        const modalNew = page.section.modal_new;
        const userData = [
            {
                username: 'TestStudent',
                surname: 'mySurname',
                forename: 'myForename',
                isAdmin: false,
                email: 'testEmail@com',
                matriculationNumber: '98765432'
            }
        ]

        for(const user of userData){
            const {isAdmin, ...keys} = user;
            for(const data in keys){
                modalNew
                .clearValue2(`@${data}`)
                .setValue(`@${data}`, user[data])
                .assert.isValidInput(`@${data}`, 'valid', true)
            }
            modalNew
            .setCheckbox(`@isAdmin`, isAdmin)
            .submit()
            .assert.successPresent()
            .closeToast()
            page.userModalNotPresent()
        }
    },
    'modal_delete close test': browser =>{
        const page = browser.page.userManagement();
        browser.pause(2000) //wait till table is loaded
        
        browser.execute(function(selector){
            return document.querySelectorAll(selector).length;
        },[page.elements.tableEntries.selector],function(result){
            runCloseTest(result.value)
        })
        function runCloseTest(count){
            const deleteModal = page.section.modal_delete;
            const modalCloseVariants = ['cancel', 'cancelX', 'cancelClick'];
            for(const variant of modalCloseVariants){
                page.showModalDelete();
                deleteModal.pause(1000)[variant]();
                page.deleteModalNotPresent();
                page.assert.not.toastPresent()
            }
            page.assert.elementCount(page.elements.tableEntries.selector, count)
        }
    },
    'modal_edit close test': browser =>{
        const page = browser.page.userManagement();
        const modalNew = page.section.modal_new;
        browser.pause(2000) //wait till table is loaded
        
        browser.execute(function(selector){
            return document.querySelectorAll(selector).length;
        },[page.elements.tableEntries.selector],function(result){
            runCloseTest(result.value)
        })
        
        function runCloseTest(count){
            page.pause(2000);
            const modalCloseVariants = ['cancel', 'cancelX', 'cancelClick'];
            for(const variant of modalCloseVariants){
                page.showModalNew();
                modalNew.pause(1000)[variant]();
                page.deleteModalNotPresent();
                page
                .assert.not.toastPresent()
                .assert.elementPresent('@tableEntries')
            }
            page.assert.elementCount(page.elements.tableEntries.selector, count)
        }
    },
    'Delete user': browser =>{
        const page = browser.page.userManagement();
        const deleteModal = page.section.modal_delete;
        const matriculationNumber = '98765432';
        page
        .setValue('@searchBar',matriculationNumber)
        .assert.elementCount('@tableEntries',1)
        .assert.containsText('@firstTableCell', matriculationNumber)
        .showModalDelete()

        deleteModal.pause(1000).submit() //without pause, button click is not triggered

        page
        .deleteModalNotPresent();
        page
        .assert.successPresent()
        .closeToast()
        .assert.not.elementPresent('@tableEntries')
    },
    'upload CSV file wrong': browser => {
        const creation = browser.page.userManagement().section.creation;
        const files = ['usersWrong', 'usersWrong2'];
        for(const fileName of files){
            creation
            .setValue('@uploadButton', Path.resolve(`${__dirname}/testFiles/${fileName}.csv`))
            .assert.errorPresent()
            .closeToast();
        }
    },
    'upload CSV file right': browser => {
        const page = browser.page.userManagement();
        const creation = page.section.creation;
        const files = ['usersRight'];
        for(const fileName of files){
            creation
            .setValue('@uploadButton', Path.resolve(`${__dirname}/testFiles/${fileName}.csv`))
            .assert.successPresent();
        }
        // .assert.containsText('@tableEntries', 'Klaus')
    },
    'upload CSV file right admin': browser => {
        const page = browser.page.userManagement();
        const creation = page.section.creation;
        const files = ['usersRight'];
        creation.setCheckbox('@isAdmin', true);
        for(const fileName of files){
            creation
            .setValue('@uploadButton', Path.resolve(`${__dirname}/testFiles/${fileName}.csv`))
            .assert.successPresent()
            .closeToast()
        }
        // .assert.containsText('@tableEntries', 'Klaus')
    },
    'Edit: invalid input': browser => {
        const page = browser.page.userManagement();
        page
        .setValue('@searchBar','Klaus')
        .click('@rowEditButton')
        .isUserModalPresent()

        const modalUser = page.section.modal_new;
        const notEditable = ['@matriculationNumber', '@username'];
        for(const item of notEditable){
            modalUser.expect.element(item).to.not.be.enabled;
        }

        const userData = [
            {
                surname: {
                    value: '',
                    valid: false
                },
                forename: {
                    value: 'myForename',
                    valid: true
                },
                isAdmin: {
                    value: false,
                    valid: true
                },
                email: {
                    value: 'testEmail@com',
                    valid: true
                },
            }
        ]

        for(const user of userData){
            const {isAdmin, ...keys} = user;
            for(const data in keys){
                modalUser
                .clearValue2(`@${data}`)
                .setValue(`@${data}`, user[data].value)
                .assert.isValidInput(`@${data}`, 'valid', user[data].valid)
            }
            modalUser
            .setCheckbox(`@isAdmin`, isAdmin)
            .submit()
            .assert.not.errorPresent();
            page.isUserModalPresent()
        }
    }
};