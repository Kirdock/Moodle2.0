const courseTest = require('./modifyCourse.js');
const testExerciseSheetsInvalid = require('./testFiles/testExerciseSheetsInvalid.js');

module.exports = {
    before: browser => {
        courseTest.before(browser, next);
        function next(){
            courseTest['select exerciseSheet'](browser);
        }
    },
    'modify information': browser => {
        const page = browser.page.exerciseSheetManagement();
        const information = page.section.information;
        // information.clearValue('@issueDate');
        information.clearDate('@issueDate');
        information.setValue('@name', 'asdlfkj');
        page.pause(10000)
    },
    'modify information invalid': browser =>{
        const page = browser.page.exerciseSheetManagement();
        const information = page.section.information;

        for(const exerciseSheet of testExerciseSheetsInvalid){
            information
                .clearValue2('@name')
                .clearDate('@issueDate')
                .clearDate('@submissionDate')
                .clearValue('@description') // clear part 1
                .setValue('@description', ` ${browser.Keys.BACK_SPACE}`) //clear part 2 (custom clear for editor)
                .clearValue2('@minKreuzel')
                .clearValue2('@minPoints')
                .setValue('@name', exerciseSheet.name.value)
                .setValue('@issueDate', exerciseSheet.issueDate.value.replace('T', browser.Keys.RIGHT_ARROW))
                .setValue('@submissionDate', exerciseSheet.submissionDate.value.replace('T', browser.Keys.RIGHT_ARROW))
                .setValue('@description', exerciseSheet.description.value)
                .setValue('@minKreuzel', exerciseSheet.minKreuzel.value)
                .setValue('@minPoints', exerciseSheet.minPoints.value)
                .click(`@kreuzelType${exerciseSheet.kreuzelType}`)

            const {description, kreuzelType, ...keys} = exerciseSheet;
            for(const key in keys){
                information.assert.isValidInput(`@${key}`, 'valid', exerciseSheet[key].valid)
            }
            if(exerciseSheet.minKreuzel.expected !== undefined){
                information.assert.value('@minKreuzel', exerciseSheet.minKreuzel.expected)
            }
            if(exerciseSheet.minPoints.expected !== undefined){
                information.assert.value('@minPoints', exerciseSheet.minPoints.expected)
            }
            information
                .submit()
                .assert.not.toastPresent();
        }
    }
}