module.exports = {
    command: function () {
        this
        .url(this.launchUrl + 'Login')
        .waitForElementVisible('body')
        .setValue('input#user','ppipp')
        .setValue('input#password','password')
        .click('button[type=submit]')
        .assert.urlEquals(this.launchUrl + 'Courses')
    }
}
  