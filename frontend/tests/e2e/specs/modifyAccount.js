const modifyCourse = require("./modifyCourse");
const testUsers = require("./testFiles/testUsers");
const modifyUsers = require("./modifyUsers");
const testUser = testUsers[3];
testUser.email = `${testUser.username}@edu.aau.at`

module.exports = {
    before: function(browser){//make sure user exists
        modifyUsers.before(browser)
        modifyUsers['create user'](browser, testUser);
        browser.logout();
        browser.loginAsStudent();
        browser.page.account().navigate().pause(1000)
    },
    'check information': function(browser, usr){
        const user = usr || testUser;
        const page = browser.page.account();
        const information = page.section.information;
        page.selectTab(1);

        information.assert.value('@username', user.username)
        information.assert.value('@matriculationNumber', user.matriculationNumber)
        information.assert.value('@surname', user.surname)
        information.assert.value('@forename', user.forename)
        information.assert.value('@email', user.email)
        information.expect.element('@username').to.not.be.enabled;
        information.expect.element('@matriculationNumber').to.not.be.enabled;
    },
    'modify information invalid': function(browser){
        const page = browser.page.account();
        const information = page.section.information;
        page.selectTab(1);
        information.clearValue2('@surname')
        information.assert.isValidInput('@surname', 'valid', false)
        information.clearValue2('@forename')
        information.assert.isValidInput('@forename', 'valid', false)
        information.clearValue2('@email')
        information.assert.isValidInput('@email', 'valid', false)
        information.setValue('@email', 'myEmail')
        information.assert.isValidInput('@email', 'valid', false)
        information.submit();
        information.assert.not.toastPresent();
    },
    'modify information': function(browser, usr){
        const user = usr || testUser;
        const change = {
            matriculationNumber: user.matriculationNumber,
            username: user.username,
            forename: 'My new forename',
            surname: 'My new surname',
            email: 'mynew@mail.com'
        }
        const page = browser.page.account();
        const information = page.section.information;
        page.selectTab(1);

        information.setUser(change);
        information.submit();
        information.assert.successPresent();
        information.closeToast();

        browser.refresh().pause(1000);
        this['check information'](browser, change)

        //revert the change
        information.setUser(user);
        information.submit();
        information.assert.successPresent();
        information.closeToast();
    },
    'modify password': function(browser){
        const passwordData = {
            oldPassword: 'password',
            newPassword: 'newPassword',
            newPasswordConfirm: 'newPassword'
        }
        const page = browser.page.account();
        const security = page.section.security;
        page.selectTab(2);
        security.clearValue2('@oldPassword')
        security.clearValue2('@newPassword')
        security.clearValue2('@newPasswordConfirm')

        security.setValue('@oldPassword', passwordData.oldPassword)
        security.setValue('@newPassword', passwordData.newPassword)
        security.setValue('@newPasswordConfirm', passwordData.newPasswordConfirm)
        security.submit();
        security.assert.successPresent();
        security.closeToast();
        security.logout();
        security.loginAsStudent(passwordData.newPassword);
        page.navigate().pause(1000);
        page.expect.url().to.match(/\/Account$/);


        //revert password change
        page.selectTab(2);
        security.setValue('@oldPassword', passwordData.newPassword)
        security.setValue('@newPassword', passwordData.oldPassword)
        security.setValue('@newPasswordConfirm', passwordData.oldPassword)
        security.submit();
        security.assert.successPresent();
        security.closeToast();
    },
    'modify password invalid': function(browser){
        const page = browser.page.account();
        const security = page.section.security;
        page.selectTab(2);
        security.clearValue2('@oldPassword')
        security.clearValue2('@newPassword')
        security.clearValue2('@newPasswordConfirm')

        security.assert.isValidInput('@oldPassword', 'valid', false)
        security.assert.isValidInput('@newPassword', 'valid', false)
        security.assert.isValidInput('@newPasswordConfirm', 'valid', false)

            //new password and confirm of new password have to match
            security.setValue('@oldPassword', 'wrongPassword')
            security.setValue('@newPassword', 'something')
            security.setValue('@newPasswordConfirm', 'somethingOther')
            security.submit();
            security.assert.not.toastPresent();


        security.clearValue2('@oldPassword')
        security.clearValue2('@newPassword')
        security.clearValue2('@newPasswordConfirm')


        security.setValue('@oldPassword', 'wrongPassword')
        security.setValue('@newPassword', 'something')
        security.setValue('@newPasswordConfirm', 'something')
        security.submit();
        security.assert.errorPresent();
        security.closeToast();
    }
}