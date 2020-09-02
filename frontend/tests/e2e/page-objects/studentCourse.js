
module.exports = {
    url: '/Course/%s',
    commands: [
        {
            validateCourse(course){
                return this.api.assert.elementPresent({
                    selector: `//div[@class="course"]/div[1][.//text()="${course.description}"]`,
                    locateStrategy: 'xpath'
                });
            },
            validateExerciseSheet(exerciseSheet){
                return this.api.assert.elementPresent({
                    selector: `//div[@class="course"]/table/tbody/tr[td[1][text() = " ${exerciseSheet.name} "] and td[2][text() = " ${exerciseSheet.submissionDateFormat} "] and td[3][contains(text(), "/${3}")] and td[4][text() = " ${exerciseSheet.minKreuzel} "] and td[5][contains(text(), "/${18}")] and td[6][text() = " ${exerciseSheet.minPoints} "]]`,
                    locateStrategy: 'xpath'
                });
            },
            selectExerciseSheet(exerciseSheet){
                return this.api.click({
                    selector: `//div[@class="course"]/table/tbody/tr[td[1][text() = " ${exerciseSheet.name} "]]/td[7]/a[2]`,
                    locateStrategy: 'xpath'
                }).pause(1000);
            }
        }
    ],
    elements: {
    }
}