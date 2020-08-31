const modifyCourse = require("./modifyCourse");
const testCourses = require("./testFiles/testCourses");
const visibleCourses = [testCourses[0]];
const hiddenCourses = [testCourses[1]];

module.exports = {
    before: (browser) => {
        modifyCourse.before(browser, function(){
            modifyCourse['create course'](browser);
            browser.logout();
            browser.loginAsOwner();
        })
        browser.loginAsOwner();
    },
    'check courses': function(browser){
        browser.assert.elementPresent({
            selector: '//nav/div/a[text() = "Admin"]',
            locateStrategy: 'xpath'
        });
        const coursePage = browser.page.courseManagement();
        coursePage.navigate();
        for(const course of visibleCourses){
            coursePage.assert.elementPresent({
                selector: `${coursePage.elements.selectCourseX.selector}/option[text()=" ${course.number} ${course.name} "]`,
                locateStrategy: 'xpath'
            })
        }

        for(const course of hiddenCourses){
            coursePage.assert.not.elementPresent({
                selector: `${coursePage.elements.selectCourseX.selector}/option[text()=" ${course.number} ${course.name} "]`,
                locateStrategy: 'xpath'
            })
        }

        coursePage.expect.element('@newButton').to.not.be.present;
    },
    'select course': function(browser){
        const coursePage = browser.page.courseManagement();
        const courseInfo = coursePage.section.courseInfo;
        coursePage.selectCourse(visibleCourses[0].number);
        coursePage.expect.element('@newButton').to.not.be.present;
        coursePage.expect.element('@deleteButton').to.not.be.present;
        coursePage.expect.element('@copyButton').to.not.be.present;

        courseInfo.expect.element('@owner').to.not.be.present;
        courseInfo.expect.element('@number').to.not.be.enabled;
        courseInfo.expect.element('@name').to.not.be.enabled;
    },
    'check admin page': function(browser){
        browser.url(browser.launchUrl + 'Admin');
        browser.assert.elementCount('.admin a', 1);
    }
}