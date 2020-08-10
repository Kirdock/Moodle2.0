module.exports = {
    command: function () {
        this
        .url(this.launchUrl + 'Login')
        .waitForElementVisible('body')
        .setValue('input#user','admin')
        .setValue('input#password','admin')
        .click('button[type=submit]')
        .assert.urlEquals(this.launchUrl + 'Courses')
    }
}
  