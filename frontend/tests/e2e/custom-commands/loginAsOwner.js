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
                    const page = self.page.userManagement();
                    const url = new URL(page.url, self.launchUrl).href;
                    page.navigate().pause(200);
                    self.url(function(result){
                        if(result.value === url){
                            self.logout(); //logged in as admin instead of owner
                            self.loginAsOwner();
                            done();
                        }
                        else{
                            const page = self.page.courseManagement();
                            const url = new URL(page.url, self.launchUrl).href;
                            page.navigate().pause(200);
                            self.url(function(result){
                                if(result.value === url){ //logged in as owner
                                    self.log('Already logged in as owner')
                                    done();
                                }
                                else{
                                    //logged in as student
                                    self.logout();
                                    self.loginAsOwner();
                                    done();
                                }
                            });
                        }
                    })
                }
                
            })
        })

        function login(){
            self.setValue('input#user','TestOwner')
            .setValue('input#password','password')
            .click('button[type=submit]')
            .assert.urlEquals(self.launchUrl + 'Courses')
        }
    }
}