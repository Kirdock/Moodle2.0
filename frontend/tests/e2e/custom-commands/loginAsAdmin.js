module.exports = {
    command: function () {
        this
        .url('http://localhost:8080/Login')
        .waitForElementVisible('body')
        .setValue('input#user','admin')
        .setValue('input#password','admin')
        .click('button[type=submit]')
        .pause(1000)
        .waitForElementVisible('body')
        .assert.urlEquals('http://localhost:8080/Courses')
    }
  }
  