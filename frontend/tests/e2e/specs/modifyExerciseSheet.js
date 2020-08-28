const Path = require('path');
const courseTest = require('./modifyCourse.js');
const testExerciseSheetsInvalid = require('./testFiles/testExerciseSheetsInvalid.js');
const testExamplesRight = require('./testFiles/testExamplesRight.js');
const testExamplesInvalid = require('./testFiles/testExamplesInvalid.js');
const testExerciseSheet = require('./testFiles/testExerciseSheets.js')[0]

function exampleExists(self, browser, example, create){
    const page = browser.page.exerciseSheetManagement();

    browser.perform(function(done){
        page.getExample(browser, example.name, function(result){
            if(result.value && result.value.ELEMENT){
                if(!create){
                    browser.log('Example exist. Example will now be deleted')
                    self['delete example'](browser, example);
                }
                done();
            }
            else{
                if(create){
                    browser.log('Example does not exist. Example will now be created')
                    self['create example'](browser, example)
                }
                done();
            }
        })
    });
}

function enterInvalidExample(browser, example, index){
    const page = browser.page.exerciseSheetManagement();
    const exampleSection = page.section.example;
    page
        .clearValue2(exampleSection.name(index))
        .clearValue(exampleSection.description(index)) // clear part 1
        .setValue(exampleSection.description(index), ` ${browser.Keys.BACK_SPACE}`) //clear part 2
        .clearValue2(exampleSection.weighting(index))
        .clearValue2(exampleSection.points(index))
    browser.perform(done => {
        browser.element('css selector',exampleSection.deleteValidatorIcon(index), function(result){
            if(result.value && result.value.ELEMENT){
                exampleSection.deleteValidator(index);
                page.assert.successPresent();
                page.closeToast();
                page.assert.not.elementPresent(exampleSection.validatorActions(index));
                done();
            }
            else{
                page.assert.not.elementPresent(exampleSection.validatorActions(index));
                done();
            }
        })
    })
    browser.perform(done =>{
        browser.execute(function(selector){
            return document.querySelector(selector).checked;
        },[exampleSection.submitFile(index)], function(result){
            if(result.value === true){
                page.clearMultiSelect(exampleSection.fileTypes(index));
            }
            done();
        })
    })
    page
        .setValue(exampleSection.name(index), example.name.value)
        .setValue(exampleSection.description(index), example.description)
        .setValue(exampleSection.weighting(index), example.weighting.value)
        .setValue(exampleSection.points(index), example.points.value)
        .setCheckbox(exampleSection.submitFile(index), example.submitFile)
        .setCheckbox(exampleSection.mandatory(index), example.mandatory)
    if(example.submitFile){
        for(const fileType of example.fileTypes.value){
            page.setMultiSelect(exampleSection.fileTypes(index), undefined, fileType)
        }
    }
    page
        .assert.isValidInput(exampleSection.name(index), 'valid', example.name.valid)
        .assert.isValidInput(exampleSection.weighting(index), 'valid', example.weighting.valid)
        .assert.isValidInput(exampleSection.points(index), 'valid', example.points.valid)
        .assert.isValidInput(exampleSection.fileTypesInput(index), 'valid', example.fileTypes.valid)

    if(example.points.expected !== undefined){
        page.assert.value(exampleSection.points(index), example.points.expected);
    }

    if(example.weighting.expected !== undefined){
        page.assert.value(exampleSection.weighting(index), example.weighting.expected);
    }
    if(example.validator.value !== ''){
        exampleSection.validatorUpload(index, Path.resolve(`${__dirname}/testFiles/${example.validator.value}.jar`));
        if(example.validator.valid){
            page.assert.successPresent();
        }
        else{
            page.assert.errorPresent();
        }
    }

    exampleSection.save(index);
    page.assert.not.toastPresent();
}

module.exports = {
    before: browser => {
        courseTest.before(browser, function (){
            courseTest['select exerciseSheet'](browser);
        });
    },
    'modify information': browser => {
        const page = browser.page.exerciseSheetManagement();
        const information = page.section.information;
        const exerciseSheets = [
            {
                name: 'Modified Exercise Sheet',
                issueDate: '30-06-2022T23:55',
                issueDateValue: '2022-06-30T23:55',
                submissionDate: '30-11-2022T23:51',
                submissionDateValue: '2022-11-30T23:51',
                submissionDateFormat: '30.11.2022, 23:51:00',
                description: 'My modified exercise sheet',
                minKreuzel: '100',
                minPoints: '30',
                kreuzelType: 1
            },
            testExerciseSheet //to maintain the right exercisesheet name, just set and save it after modification
        ]
        for(const exerciseSheet of exerciseSheets){
            information
                .clearValue2('@name')
                .clearDate('@issueDate')
                .clearDate('@submissionDate')
                .clearValue('@description') // clear part 1
                .setValue('@description', ` ${browser.Keys.BACK_SPACE}`) //clear part 2 (custom clear for editor)
                .clearValue2('@minKreuzel')
                .clearValue2('@minPoints')

                .setValue('@name', exerciseSheet.name)
                .setValue('@issueDate', exerciseSheet.issueDate.replace('T', browser.Keys.RIGHT_ARROW))
                .setValue('@submissionDate', exerciseSheet.submissionDate.replace('T', browser.Keys.RIGHT_ARROW))
                .setValue('@description', exerciseSheet.description)
                .setValue('@minKreuzel', exerciseSheet.minKreuzel)
                .setValue('@minPoints', exerciseSheet.minPoints)
                .click(`@kreuzelType${exerciseSheet.kreuzelType}`)

                .submit()
                .assert.successPresent()
                .closeToast();
        }
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
    'create example': function(browser, example){
        const page = browser.page.exerciseSheetManagement();
        const self = this;
        const examples = example ? [example] : testExamplesRight;
        const exampleSection = page.section.example;

        for(const example of examples){
            browser.perform(function(done){
                exampleExists(self, browser, example, false);
                page.newExample(browser, next)

                function next(index){
                    page
                        .clearValue2(exampleSection.name(index))
                        .clearValue(exampleSection.description(index)) // clear part 1
                        .setValue(exampleSection.description(index), ` ${browser.Keys.BACK_SPACE}`) //clear part 2
                        .clearValue2(exampleSection.weighting(index))
                        .clearValue2(exampleSection.points(index))

                        .setValue(exampleSection.name(index), example.name)
                        .setValue(exampleSection.description(index), example.description);

                    page.assert.not.elementPresent(exampleSection.validatorContainer(index));

                    if(example.subExamples.length === 0 || example.forceEnterInformation){
                        page.setValue(exampleSection.weighting(index), example.weighting)
                            .setValue(exampleSection.points(index), example.points)
                            .setCheckbox(exampleSection.submitFile(index), example.submitFile)
                            .setCheckbox(exampleSection.mandatory(index), example.mandatory)
                        
                        if(example.validator){
                            exampleSection.validatorUpload(index, Path.resolve(`${__dirname}/testFiles/${'validatorRight'}.jar`));
                            page.assert.successPresent();
                            page.closeToast();
                            page.assert.containsText(exampleSection.validatorName(index),'validatorRight.jar')
                        }
                        if(example.submitFile){
                            page.assert.elementPresent(exampleSection.validatorContainer(index));
                            for(const fileType of example.fileTypes){
                                page.setMultiSelect(exampleSection.fileTypes(index), undefined, fileType)
                            }
                        }
                    }
                    if(example.subExamples.length !== 0){
                        exampleSection.newSubExample(index);
                        page
                            .assert.not.elementPresent(exampleSection.weighting(index))
                            .assert.not.elementPresent(exampleSection.points(index))
                            .assert.not.elementPresent(exampleSection.validatorContainer(index))
                            .assert.not.elementPresent(exampleSection.mandatory(index))
                            .assert.not.elementPresent(exampleSection.submitFile(index))
                            
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
                        page.assert.not.elementPresent(exampleSection.deleteButton(index));
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

                        if(example.subExamples[i].submitFile){
                            page.assert.not.elementPresent(exampleSection.validatorContainer(index));
                            for(const fileType of example.subExamples[i].fileTypes){
                                page.setMultiSelect(exampleSection.fileTypes(index), undefined, fileType)
                            }
                        }
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
    'delete example': function(browser, example){
        const page = browser.page.exerciseSheetManagement();
        const exampleSection = page.section.example;
        const deleteModal = page.section.deleteModal;
        const self = this;
        example = example || testExamplesRight[0];
        
        exampleExists(self, browser, example, true);
        page.selectExample(browser, example.name, next);

        function next(index){
            exampleSection.showDeleteModal(index);
            deleteModal.submit();
            page.assert.successPresent();
            page.closeToast();
            exampleSection.exampleNotPresent(example.name);
        }
    },
    'modify example invalid': function(browser){
        const testExample = {
            name: 'TestExample',
            description: '',
            weighting: 1,
            points: 10,
            submitFile: true,
            mandatory: false,
            fileTypes: ['Word', 'zip'],
            validator: true,
            subExamples: []
        }
        const self = this;
        const page = browser.page.exerciseSheetManagement();
        exampleExists(self, browser, testExample, true);
        page.selectExample(browser, testExample.name, function(index){
            for(const example of testExamplesInvalid){
                enterInvalidExample(browser, example, index);
            }
        });
    },
    'modify subexample invalid': function(browser){
        const testExample = {
            name: 'TestExample2',
            description: '',
            weighting: 1,
            points: 10,
            submitFile: true,
            mandatory: false,
            fileTypes: ['Word', 'zip'],
            validator: true,
            forceEnterInformation: true,
            subExamples: [
                {
                    name: 'TestSubExample',
                    description: '',
                    weighting: 1,
                    points: 10,
                    submitFile: true,
                    mandatory: false,
                    fileTypes: ['Word', 'zip'],
                    validator: true
                }
            ]
        };
        const self = this;
        const page = browser.page.exerciseSheetManagement();
        const exampleSection = page.section.example;
        const deleteModal = page.section.deleteModal;
        exampleExists(self, browser, testExample, true);
        page.selectExample(browser, testExample.name, function(index){
            exampleSection.selectLastSubExample(index);
            page.assert.not.elementPresent(exampleSection.deleteButton(index));
            for(const example of testSubExamplesInvalid){
                enterInvalidExample(browser, example, index);
            }
            exampleSection.selectParent(index);
            page
                .assert.not.elementPresent(exampleSection.weighting(index))
                .assert.not.elementPresent(exampleSection.points(index))
                .assert.not.elementPresent(exampleSection.validatorContainer(index))
                .assert.not.elementPresent(exampleSection.mandatory(index))
                .assert.not.elementPresent(exampleSection.submitFile(index))
            
            exampleSection.showDeleteModalSubExample(index);
            deleteModal.submit();

            page.assert.successPresent();
            page.closeToast();
            exampleSection.subExampleNotPresent(testExample.name, testExample.subExamples[0].name)

            page
                .assert.elementPresent(exampleSection.weighting(index))
                .assert.elementPresent(exampleSection.points(index))
                .assert.not.elementPresent(exampleSection.validatorContainer(index))
                .assert.elementPresent(exampleSection.mandatory(index))
                .assert.elementPresent(exampleSection.submitFile(index))

            page
                .assert.value(exampleSection.weighting(index), 1)
                .assert.value(exampleSection.points(index), 0)
                .expect.element(exampleSection.submitFile(index)).to.not.be.checked;
            page.expect.element(exampleSection.mandatory(index)).to.not.be.checked;
        });
    }
}