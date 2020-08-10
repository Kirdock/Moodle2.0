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
    'Delete user': browser =>{
        const page = browser.page.userManagement();
        const matriculationNumber = '98765432';
        page
        .setValue('@searchBar',matriculationNumber)
        .assert.containsText('@firstTableCell', matriculationNumber)
        .showModalDelete()

        const deleteModal = page.section.modal_delete;
        deleteModal.pause(1000).submit() //without pause, button click is not triggered

        page
        .deleteModalNotPresent();
        page
        .assert.successPresent()
        .closeToast()
        .assert.not.elementPresent('@firstTableEntry')
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