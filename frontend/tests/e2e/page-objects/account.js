module.exports = {
    url: '/Account',
    commands: [{
        selectTab(position){
            return this.api.click(`.tabs ul>li:nth-of-type(${position})>a`)
        }
    }],
    sections: {
        information: {
            selector: '.tab-content>div:nth-of-type(1)',
            commands: [{
                submit(){
                    return this.click('@submitButton');
                },
                setUser(user){
                    this.clearValue2('@surname')
                    this.clearValue2('@forename')
                    this.clearValue2('@email')

                    this.setValue('@surname', user.surname)
                    this.setValue('@forename', user.forename)
                    this.setValue('@email', user.email)
                }
            }],
            elements: {
                username: '.tab-content>div:nth-of-type(1) .form-horizontal>div.form-group:nth-of-type(1) input',
                matriculationNumber: '.tab-content>div:nth-of-type(1) .form-horizontal>div.form-group:nth-of-type(2) input',
                surname: '.tab-content>div:nth-of-type(1) .form-horizontal>div.form-group:nth-of-type(3) input',
                forename: '.tab-content>div:nth-of-type(1) .form-horizontal>div.form-group:nth-of-type(4) input',
                email: '.tab-content>div:nth-of-type(1) .form-horizontal>div.form-group:nth-of-type(5) input',
                submitButton: '.tab-content>div:nth-of-type(1) button.btn.btn-primary',
            }
        },
        security: {
            selector: '.tab-content>div:nth-of-type(2)',
            commands: [{
                submit(){
                    return this.click('@submitButton');
                }
            }],
            elements: {
                oldPassword: '.tab-content>div:nth-of-type(2) .form-horizontal>form>div.form-group:nth-of-type(1) input',
                newPassword: '.tab-content>div:nth-of-type(2) .form-horizontal>form>div.form-group:nth-of-type(2) input',
                newPasswordConfirm: '.tab-content>div:nth-of-type(2) .form-horizontal>form>div.form-group:nth-of-type(3) input',
                submitButton: '.tab-content>div:nth-of-type(2) button.btn.btn-primary',
            }
        }
    }
}