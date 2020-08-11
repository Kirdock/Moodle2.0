

module.exports = {
    before: browser => {
        browser
            .loginAsAdmin()
            .page.semesterManagement().navigate();
    },
    'auto select': browser =>{
        const page = browser.page.semesterManagement();
        const dateToday = new Date();
        const sSemesterSelected = dateToday.getMonth() > 1 && dateToday.getMonth() < 9;
        
        page.assert.value('@year', dateToday.getFullYear().toString());
        if(sSemesterSelected){
            page.expect.element('@sSemester').to.be.selected;
            page.expect.element('@wSemester').to.not.be.selected;
        }
        else{
            page.expect.element('@wSemester').to.be.selected;
            page.expect.element('@sSemester').to.not.be.selected;
        }
    },
    'create semester': (browser) => {
        const page = browser.page.semesterManagement();
            // .clearValue('@year') //auto set at start
            // .setValue('@year', '2020')
            // .setValue('@wSemester', true)
        page.submit()
        page.assert.toastPresent();
        page.closeToast();
    }
}