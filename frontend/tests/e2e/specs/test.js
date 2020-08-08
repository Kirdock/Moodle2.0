module.exports = {
  'login with empty input': browser => {
    const data = [
      {
        username:{
          value: 'admin',
          valid: true
        },
        password:{
          value: '',
          valid: false
        }
      },
      {
        username:{
          value: '',
          valid: false
        },
        password:{
          value: 'admin',
          valid: true
        }
      },
      {
        username:{
          value: '',
          valid: false
        },
        password:{
          value: '',
          valid: false
        }
      }
    ];
    browser.init()
      .assert.elementPresent('a[href="/Login"]')
      .click('a[href="/Login"]')
      .assert.urlEquals('http://localhost:8080/Login')

    for (user of data){
      browser
      .clearValue2('input#user')
      .clearValue2('input#password')
      .setValue('input#user', user.username.value)
      .setValue('input#password',user.password.value)
      .assert.isValidInput('input#user', 'valid', user.username.valid)
      .assert.isValidInput('input#password', 'valid', user.password.valid)
      .click('button[type=submit]')
      .assert.not.elementPresent('.b-toast[role=alert]')
    }
  },
  'login wrong credentials': browser => {
    const credentials = [
      {
        username: 'admin',
        password: 'test'
      }
    ];

    browser
      .url('http://localhost:8080/Login')
      .waitForElementVisible('body');

    for(const credential of credentials){
      browser.clearValue2('input#user')
      .clearValue2('input#password')
      .setValue('input#user', credential.username)
      .setValue('input#password', credential.password)
      .assert.isValidInput('input#password', 'valid', true)
      .assert.isValidInput('input#user', 'valid', true)
      .click('button[type=submit]')
      .assert.elementPresent('.b-toast[role=alert]')
      .click('button.close.ml-auto.mb-1') //remove toast message
      .assert.not.elementPresent('.b-toast[role=alert]')
      .assert.urlEquals('http://localhost:8080/Login')
    }
  },
  'login': browser => {
    browser.loginAsAdmin()
  },
}
