
module.exports = {
    url: '/Courses',
    commands: [
        {
            coursePresent(course, status = true){
                return (status ? browser.assert : browser.assert.not).elementPresent({
                    selector: `//div[@id="courseList"]/div/a[text()="${course.number} ${course.name}"]`,
                    locateStrategy: 'xpath'
                });
            },
            courseNotPresent(course){
                return this.coursePresent(course, false);
            },
            selectCourse(course){
                return this.api.click({
                    selector: `//div[@id="courseList"]/div/a[text()=" ${course.number} ${course.name} "]`,
                    locateStrategy: 'xpath'
                }).pause(1000);
            },
            selectSemester(year, isSummer){
                return this.api.click('xpath',`//select/option[contains(text(),"${year} ${isSummer ? 'SS' : 'WS'}")]`).pause(1000);
            }
        }
    ],
    elements: {
    }
}