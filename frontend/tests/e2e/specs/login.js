const loginPage = 'Login',
      coursesPage = 'Courses';

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
    browser.url(browser.launchUrl).pause(2000)
      .assert.elementPresent('a[href="/Login"]')
      .click('a[href="/Login"]')
      .assert.urlEquals(browser.launchUrl + loginPage)

    for (user of data){
      browser
      .clearValue2('input#user')
      .clearValue2('input#password')
      .setValue('input#user', user.username.value)
      .setValue('input#password',user.password.value)
      .assert.isValidInput('input#user', 'valid', user.username.valid)
      .assert.isValidInput('input#password', 'valid', user.password.valid)
      .click('button[type=submit]')
      .assert.not.errorPresent()
      .assert.urlEquals(browser.launchUrl + loginPage)
    }
  },
  'login wrong credentials': browser => {
    const credentials = [
      {
        username: 'admin',
        password: 'test'
      }
    ];

    for(const credential of credentials){
      browser.clearValue2('input#user')
      .clearValue2('input#password')
      .setValue('input#user', credential.username)
      .setValue('input#password', credential.password)
      .assert.isValidInput('input#password', 'valid', true)
      .assert.isValidInput('input#user', 'valid', true)
      .click('button[type=submit]')
      .assert.errorPresent()
      .closeToast()
      .assert.urlEquals(browser.launchUrl + loginPage)
    }
  },
  'login as admin': browser => {
    browser.loginAsAdmin()
    .url(browser.launchUrl + loginPage)
    .assert.urlEquals(browser.launchUrl + coursesPage)

  },
}
