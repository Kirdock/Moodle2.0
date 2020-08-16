module.exports = {
    command: function () {
        const self = this;
        this.perform(function (done){
            const loginPage = self.launchUrl + 'Login';
            self.url(loginPage);
            self.pause(1000);
            self.url(function(result){
                if(result.value === loginPage){
                    login();
                    done();
                }
                else{ //logged in
                    const page = self.page.courseManagement();
                    const url = new URL(page.url, self.launchUrl).href;
                    page.navigate().pause(200);
                    self.url(function(result){
                        if(result.value === url){ //logged in as owner or admin
                            self.logout();
                            self.loginAsStudent();
                            done();
                        }
                        else{
                            self.log('Already logged in as student')
                            done();
                        }
                    });
                }
                
            })
        })

        function login(){
            self.setValue('input#user','ppipp')
            .setValue('input#password','password')
            .click('button[type=submit]')
            .assert.urlEquals(self.launchUrl + 'Courses')
        }
    }
}