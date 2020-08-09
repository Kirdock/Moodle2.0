module.exports = {
    command: function () {
        this
        .url(this.launchUrl + 'Login')
        .waitForElementVisible('body')
        .setValue('input#user','admin')
        .setValue('input#password','admin')
        .click('button[type=submit]')
        .pause(1000)
        .waitForElementVisible('body')
        .assert.urlEquals(this.launchUrl + 'Courses')
    }
}
  