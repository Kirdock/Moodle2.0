const Path = require('path');
const admin = {
    matriculationNumber: '00000000'
}
const testUser = { //will be used for creating, deleting, editing
    username: 'TestStudent',
    surname: 'mySurname',
    forename: 'myForename',
    isAdmin: false,
    email: 'testEmail@com',
    matriculationNumber: '98765432'
}

function userExists(self, browser, matriculationNumber, create, performAfter){
    //create === true: create it
    //create === false: delete it
    const page = browser.page.userManagement();
    page
        .clearValue2('@searchBar')
        .setValue('@searchBar',matriculationNumber)
        .pause(1000) //wait for search
    browser.element('css selector', page.elements.firstTableCell.selector, function(result){
        page.clearValue2('@searchBar')

        if (result.value && result.value.ELEMENT) {
            // Element is present
            if(!create){
                browser.log('User already available. User will now be deleted')
                self['delete user'](browser, matriculationNumber);
            }
            performAfter();
        } else {
            if(create){
                browser.log('User does not exist. User will now be created')
                self['create user'](browser);
            }
            performAfter();
        }
    });
}

function userRightCreated(browser, user){
    const page = browser.page.userManagement();
    const modalNew = page.section.modal_new;
    browser.log('Checking if user was created right')
    page
        .clearValue2('@searchBar')
        .setValue('@searchBar',user.matriculationNumber)
        .assert.elementCount('@tableEntries',1);
    const {email, username, ...tableColumns} = user;
    for(const data in tableColumns){ //table check
        page.assert.containsText(`@tableCell${data}`, user[data] === true ? 'Ja' : user[data] === false ? 'Nein' : user[data])
    }

    page.click('@rowEditButton')
        .isUserModalPresent()

    for(const data in user){ //edit modal check
        if(user[data] === true){
            modalNew.expect.element(`@${data}`).to.be.selected;
        }
        else if(user[data] === false){
            modalNew.expect.element(`@${data}`).to.not.be.selected;
        }
        else{
            modalNew.assert.value(`@${data}`,user[data]);
        }
    }
    
    modalNew.cancel();
    page.clearValue2('@searchBar');
}


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
                    value: '',
                    valid: false
                },
                forename: {
                    value: '',
                    valid: false
                },
                isAdmin: {
                    value: false,
                    valid: true
                },
                email: {
                    value: 'testEmail',
                    valid: false
                },
                matriculationNumber: {
                    value: 'abc',
                    valid: false
                }
            },
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
                    value: true,
                    valid: true
                },
                email: {
                    value: 'testEmail@com',
                    valid: true
                },
                matriculationNumber: {
                    value: '123456',
                    valid: false
                },
            },
            {
                username: {
                    value: 'Test Student who has a very long username, longer than everyone else',
                    valid: true
                },
                surname: {
                    value: 'Test Student who has a very long surname, longer than everyone else',
                    valid: true
                },
                forename: {
                    value: 'Test Student who has a very long forename, longer than everyone else',
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
                    value: '123456789',
                    valid: false
                }
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
            modalNew.setValue(`@${data}`, user[data])
        }
        modalNew
        .setCheckbox(`@isAdmin`, isAdmin)
        .cancel();

        page.showModalNew();
        for(const data in keys){
            modalNew.assert.value(`@${data}`,'');
        }
        modalNew.expect.element(`@isAdmin`).to.not.be.selected;
    },
    'create user': function(browser) {
        const page = browser.page.userManagement();
        const modalNew = page.section.modal_new;
        const userData = [testUser]
        

        for(const user of userData){
            userExists(this,browser,user.matriculationNumber, false, createUser);

            function createUser(){
                page.showModalNew();
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
                page.userModalNotPresent();

                userRightCreated(browser,user)
            }
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
    'delete user': function(browser, matr) {
        const page = browser.page.userManagement();
        const deleteModal = page.section.modal_delete;
        const matriculationNumber = matr || testUser.matriculationNumber;

        userExists(this, browser, matriculationNumber, true, deleteUser)

        function deleteUser(){
            page
                .clearValue2('@searchBar')
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
                .clearValue2('@searchBar')
        }
    },
    'delete admin': function(browser, matr) {
        const page = browser.page.userManagement();
        const deleteModal = page.section.modal_delete;
        const matriculationNumber = admin.matriculationNumber;
        page
            .clearValue2('@searchBar')
            .setValue('@searchBar',matriculationNumber)
            .assert.elementCount('@tableEntries',1)
            .assert.containsText('@firstTableCell', matriculationNumber)
            .showModalDelete()

        deleteModal.pause(1000).submit() //without pause, button click is not triggered

        page
            .deleteModalNotPresent();
        page
            .assert.errorPresent()
            .closeToast()
            .assert.elementCount('@tableEntries',1)
            .clearValue2('@searchBar')
    },
    'upload CSV file wrong': browser => {
        const creation = browser.page.userManagement().section.creation;
        const files = ['usersWrong', 'usersWrong2'];
        for(const fileName of files){
            creation
            .setValue('@uploadButton', Path.resolve(`${__dirname}/testFiles/${fileName}.csv`))
            .assert.toastPresent()
            .assert.not.successPresent()
            .closeToast();
        }
    },
    'upload CSV file right': function(browser, isAdmin = false) {
        const page = browser.page.userManagement();
        const creation = page.section.creation;
        const files = ['usersRight'];
        const self = this;
        const users = [
            {
                username: 'klstriessnig',
                matriculationNumber: '88888888',
                surname: 'Strießnig',
                forename: 'Klaus',
                isAdmin
            },
            {
                username: 'ppipp',
                matriculationNumber: '87596328',
                surname: 'Pipp',
                forename: 'Peter',
                isAdmin
            },
            {
                username: 'TestOwner',
                matriculationNumber: '00000001',
                surname: 'TestOwnerN',
                forename: 'TestOwnerV',
                isAdmin
            }
        ]
        creation.setCheckbox('@isAdmin', isAdmin);
        let i = -1;
        iterate();
        function iterate(){
            i++;
            userExists(self, browser, users[i].matriculationNumber, false, i === users.length-1 ? upload : iterate)
        }
        
        function upload(){
            for(const fileName of files){
                creation
                .setValue('@uploadButton', Path.resolve(`${__dirname}/testFiles/${fileName}.csv`))
                .assert.successPresent();
            }
            
            for(const user of users){
                userRightCreated(browser, user)
            }
        }
    },
    'upload CSV file right admin': function(browser) {
        this["upload CSV file right"](browser, true);
    },
    'Edit: invalid input': function(browser) {
        const page = browser.page.userManagement();
        userExists(this, browser, testUser.matriculationNumber, true, editUser)
        
        function editUser(){
            page.setValue('@searchBar',testUser.matriculationNumber)
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
    }
};