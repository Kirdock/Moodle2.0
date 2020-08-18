
module.exports = {
    commands: [],
    elements: {
        newExampleButton: '#exerciseSheetTab .nav.nav-tabs li:last-child'
    },
    sections: {
        information: {
            selector: '#exerciseSheetTab .tab-content div:first-child',
            commands: [{
                submit: function(){
                    return this.click('@submitButton')
                }
            }],
            elements: {
                submitButton: '#exerciseSheetTab .tab-content div:first-child button.btn.btn-primary',
                name: '#exerciseSheetTab .tab-content div:first-child input[type=text]',
                issueDate: {
                    selector: '#exerciseSheetTab .tab-content div:first-child input[type=datetime-local]',
                    index: 0
                },
                submissionDate: {
                    selector: '#exerciseSheetTab .tab-content div:first-child input[type=datetime-local]',
                    index: 1
                },
                description: '#exerciseSheetTab .tab-content div:first-child .note-editable',
                minKreuzel: {
                    selector: '#exerciseSheetTab .tab-content div:first-child input[type=number]',
                    index: 0
                },
                minPoints: {
                    selector: '#exerciseSheetTab .tab-content div:first-child input[type=number]',
                    index: 1
                },
                kreuzelType0: {
                    selector: '#exerciseSheetTab .tab-content div:first-child input[type=radio]',
                    index: 0
                },
                kreuzelType1: {
                    selector: '#exerciseSheetTab .tab-content div:first-child input[type=radio]',
                    index: 1
                }
            }
        },
        example: {
            selector: '#exerciseSheetTab .tab-content>div',
            commands: [{
                tabContent: function(sortIndex){
                    return `#exerciseSheetTab .tab-content>div:nth-child(${sortIndex + 2})`;
                },
                newSubExample: function(sortIndex){
                    return this.click(`${this.tabContent(sortIndex)} .subExamples button`)
                },
                save: function(sortIndex){
                    return this.click(`${this.tabContent(sortIndex)} button[type=submit]`)
                },
                delete: function(sortIndex){
                    return this.click(`${this.tabContent(sortIndex)} button.btn.btn-danger`)
                },
                validatorUpload: function(sortIndex, path){
                    return this.setValue(`${this.tabContent(sortIndex)} .validatorGroup input[type=file]`, path)
                }
            }],
            elements: {
            }
        }
    }
}