module.exports = {
    'Invalid input': function (browser) {
        browser.loginAsAdmin();
        const userPage = browser.page.userManagement();
        userPage.navigate().showModalNew();
        const modalNew = userPage.section.modal_new;

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
            .assert.not.errorPresent()
        }
        browser.end();
    }
};