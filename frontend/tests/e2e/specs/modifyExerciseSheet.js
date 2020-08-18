const Path = require('path');
const courseTest = require('./modifyCourse.js');
const testExerciseSheetsInvalid = require('./testFiles/testExerciseSheetsInvalid.js');
const testExamplesRight = require('./testFiles/testExamplesRight.js');


function exampleExists(self, browser, name, create){
    const page = browser.page.exerciseSheetManagement();

    browser.perform(function(done){
        page.getExample(browser, name, function(result){
            if(result.value && result.value.ELEMENT){
                if(!create){
                    browser.log('Example exist. Example will now be deleted')
                    self['delete example'](browser, name);
                }
                done();
            }
            else{
                if(create){
                    browser.log('Example does not exist. Example will now be created')
                    self['create example'](browser, name)
                }
                done();
            }
        })
    });
}

function getExample(name){
    return testExamplesRight.find(example => example.name === name);
}

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
        // information.clearDate('@issueDate');
        // information.setValue('@name', 'asdlfkj');
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
    },
    'create example': function(browser, name){
        const page = browser.page.exerciseSheetManagement();
        const self = this;
        const examples = name ? [getExample(name)] : testExamplesRight;
        const exampleSection = page.section.example;

        for(const example of examples){
            exampleExists(self, browser, example.name, false);

            browser.perform(function(done){
                page.newExample(browser, next)

                function next(index){
                    page
                        .clearValue2(exampleSection.name(index))
                        .clearValue(exampleSection.description(index)) // clear part 1
                        .setValue(exampleSection.description(index), ` ${browser.Keys.BACK_SPACE}`) //clear part 2
                        .clearValue2(exampleSection.weighting(index))
                        .clearValue2(exampleSection.points(index))

                        .setValue(exampleSection.name(index), example.name)
                        .setValue(exampleSection.description(index), example.description)
                    if(example.subExamples.length === 0){
                        page.setValue(exampleSection.weighting(index), example.weighting)
                            .setValue(exampleSection.points(index), example.points)
                            .setCheckbox(exampleSection.submitFile(index), example.submitFile)
                            .setCheckbox(exampleSection.mandatory(index), example.mandatory)
                        if(example.validator){
                            exampleSection.validatorUpload(index, Path.resolve(`${__dirname}/testFiles/${'validatorRight'}.jar`));
                            page.assert.successPresent();
                            page.closeToast();
                            page.assert.containsText(exampleSection.validatorName(index),'validatorRight.jar')
                            for(const fileType of example.fileTypes){
                                page.setMultiSelect(exampleSection.fileTypes(index), undefined, fileType)
                            }
                        }
                    }
                    else{
                        exampleSection.newSubExample(index);
                    }
                    
                    exampleSection.save(index);
                    page.assert.successPresent();
                    page.closeToast();
                    for(let i = 0; i < example.subExamples.length; i++){
                        if(i === 0){
                            exampleSection.selectLastSubExample(index);
                        }
                        else {
                            exampleSection.newSubExampleAndSelect(index);
                        }
                        page
                            .clearValue2(exampleSection.name(index))
                            .clearValue(exampleSection.description(index)) // clear part 1
                            .setValue(exampleSection.description(index), ` ${browser.Keys.BACK_SPACE}`) //clear part 2
                            .clearValue2(exampleSection.weighting(index))
                            .clearValue2(exampleSection.points(index))

                            .setValue(exampleSection.name(index), example.subExamples[i].name)
                            .setValue(exampleSection.description(index), example.subExamples[i].description)
                            .setValue(exampleSection.weighting(index), example.subExamples[i].weighting)
                            .setValue(exampleSection.points(index), example.subExamples[i].points)
                            .setCheckbox(exampleSection.submitFile(index), example.subExamples[i].submitFile)
                            .setCheckbox(exampleSection.mandatory(index), example.subExamples[i].mandatory)
                        exampleSection.save(index);
                        page.assert.successPresent();
                        page.closeToast();
                        exampleSection.selectParent(index);
                    }
                    done();
                }
            })
        }
    },
    'delete example': function(browser, name){
        const page = browser.page.exerciseSheetManagement();
        const exampleSection = page.section.example;
        const deleteModal = page.section.deleteModal;
        const self = this;
        name = name || testExamplesRight[0].name;
        
        exampleExists(self, browser, name, true);
        page.selectExample(browser, name, next);

        function next(index){
            exampleSection.showDeleteModal(index);
            deleteModal.submit();
            page.assert.successPresent();
            page.closeToast();
        }
    }
}