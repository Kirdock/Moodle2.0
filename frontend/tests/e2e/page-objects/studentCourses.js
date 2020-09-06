
module.exports = {
    url: '/Courses',
    commands: [
        {
            courseOption(course){
                return `//div[@id="courseList"]/div/a[${typeof course === 'string' ? `contains(text(),"${course}")` : `text()=" ${course.number} ${course.name} "`}]`;
            },
            coursePresent(course, status = true){
                return (status ? this.api.assert : this.api.assert.not).elementPresent({
                    selector: this.courseOption(course),
                    locateStrategy: 'xpath'
                });
            },
            courseNotPresent(course){
                return this.coursePresent(course, false);
            },
            selectCourse(course){
                return this.api.click({
                    selector: this.courseOption(course),
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