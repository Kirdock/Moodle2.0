const permitPages = [
    '',
];

const authNotAllowedPages = [
    'Login'
]

const userPages = [
    'Courses',
    'Course/1',
    'Course/1/ExerciseSheet/1',
    'Account',
]

const adminPages = [
    'Admin/UserManagement',
]

const ownerPages = [
    'Admin',
    'Admin/SemesterManagement',
    'Admin/CourseManagement',
    'Admin/Course/1/SheetManagement/1',
]

function checkAllowed(browser, allowed, notAllowed){
    for(const page of allowed){
        const url = browser.launchUrl + page;
        browser.url(url)
        .assert.urlEquals(url);
    }

    for(const page of notAllowed){
        const url = browser.launchUrl + page;
        browser.url(url)
        .assert.not.urlEquals(url);
    }
}


module.exports = {
    'not logged in': browser => {
        const allowed = permitPages.concat(authNotAllowedPages);
        const notAllowed = userPages.concat(adminPages).concat(ownerPages);
        checkAllowed(browser, allowed, notAllowed);
    },
    'student': browser => {
        browser.loginAsStudent();
        const allowed = permitPages.concat(userPages);
        const notAllowed = authNotAllowedPages.concat(adminPages).concat(ownerPages);
        checkAllowed(browser, allowed, notAllowed);
    },
    'owner': browser => {
        browser.loginAsOwner();
        const allowed = permitPages.concat(userPages).concat(ownerPages);
        const notAllowed = authNotAllowedPages.concat(adminPages);
        checkAllowed(browser, allowed, notAllowed);
    },
    'admin': browser => {
        browser.loginAsAdmin();
        const allowed = permitPages.concat(userPages).concat(ownerPages).concat(adminPages);
        checkAllowed(browser, allowed, authNotAllowedPages);
    }

}