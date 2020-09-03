module.exports = {
    command: function () {
        const self = this;
        this.perform(function (done){
            const loginPage = self.launchUrl + 'Login';
            self.url(loginPage);
            self.pause(1000);
            self.url(function(result){
                if(result.value === loginPage){
                    loginAsAdmin();
                    done();
                }
                else{ //logged in
                    const page = self.page.userManagement();
                    const url = new URL(page.url, self.launchUrl).href;
                    page.navigate().pause(1000);
                    self.url(function(result){
                        if(result.value === url){
                            self.log('Already logged in as admin')
                            done();
                        }
                        else{
                            self.logout();
                            self.loginAsAdmin();
                            done();
                        }
                    })
                }
                
            })
        })

        function loginAsAdmin(){
            self.setValue('input#user','admin')
            .setValue('input#password','admin')
            .click('button[type=submit]')
            .assert.urlEquals(self.launchUrl + 'Courses')
        }
    }
}
  