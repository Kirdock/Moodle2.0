module.exports = {
    command: function () {
        this
        .click('button.close.ml-auto.mb-1')
        .assert.not.errorPresent()
    }
}
  