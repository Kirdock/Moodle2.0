const modalCommands = require("../modalCommands")
//div[@id="exerciseSheetTab"]/div[@class="tab-content"]/div[count(//div[@id="exerciseSheetTab"]/div[1]/ul/li[a[text()="Beispiel 1"]]/preceding-sibling::*)+1]
module.exports = {
    commands: [{
        getExample: function(browser, name, callback){
            browser.element('xpath', `//ul[@class="nav nav-tabs"]/li[a[text()="${name}"]]`, callback)
        },
        newExample: function(browser, callback){
            this.click('ul.nav.nav-tabs li:last-child').pause(1000, function(){
                browser.elements('css selector', 'ul.nav.nav-tabs li', result => callback(result.value.length - 2))
            })
        },
        selectExample: function(browser, name, done){
            browser.perform(function(){
                browser.elements('css selector', `ul.nav.nav-tabs li a`, function(result){
                    for(let i = 1; i < result.value.length - 1; i++){
                        browser.perform(function(done2){
                            browser.elementIdText(result.value[i].ELEMENT,function(res){
                                if(res.value === name){
                                    browser.elementIdClick(result.value[i].ELEMENT,()=> browser.pause(1000, ()=>done(i)))
                                    done2();
                                }
                                done2();
                            })
                        })
                        
                    }
                })
            })
        }
    }],
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
                examplePresent: function(name){
                    return this.api.assert.elementPresent('xpath',`//div[@id="exerciseSheetTab"]/div[1]/ul/li[a[text()="${name}"]]`);
                },
                exampleNotPresent: function(name){
                    return this.api.assert.not.elementPresent('xpath',`//div[@id="exerciseSheetTab"]/div[1]/ul/li[a[text()="${name}"]]`);
                },
                subExampleNotPresent: function(exampleName, subExampleName){
                    return this.api.assert.not.elementPresent('xpath',`//div[@id="exerciseSheetTab"]/div[@class="tab-content"]/div[count(//div[@id="exerciseSheetTab"]/div[1]/ul/li[a[text()="${exampleName}"]]/preceding-sibling::*)+1]]//div[contains(@class, 'subExamples')]//tr/td[1][contains(text(),"${subExampleName}")]`);
                },
                tabContent: function(sortIndex){
                    return `#exerciseSheetTab .tab-content>div:nth-child(${sortIndex+1})`;
                },
                selectParent: function(sortIndex){
                    return this.api.click(`${this.tabContent(sortIndex)}>div a`).pause(1000);
                },
                newSubExample: function(sortIndex){
                    return this.api.click(`${this.tabContent(sortIndex)} .subExamples button`)
                        .pause(1000);
                },
                selectLastSubExample: function(sortIndex){
                    return this.api.click(`${this.tabContent(sortIndex)} .subExamples tr:last-child a:nth-child(1)`);
                },
                newSubExampleAndSelect: function(sortIndex){
                    return this.api.click(`${this.tabContent(sortIndex)} .subExamples button`)
                        .pause(1000)
                        .click(`${this.tabContent(sortIndex)} .subExamples tr:last-child a:nth-child(1)`)
                        .pause(1000);
                },
                save: function(sortIndex){
                    return this.api.click(`${this.tabContent(sortIndex)} button[type=submit]`);
                },
                deleteButton: function(sortIndex){
                    return `${this.tabContent(sortIndex)} button.btn.btn-danger`;
                },
                showDeleteModal: function(sortIndex){
                    return this.api.click(`${this.tabContent(sortIndex)} button.btn.btn-danger`).pause(1000);
                },
                subExampleRows: function(sortIndex){
                    return `${this.tabContent(sortIndex)} .subExamples tr`;
                },
                showDeleteModalSubExample: function(sortIndex){
                    return this.api.click(`${this.tabContent(sortIndex)} .subExamples tr:nth-child(1) a:nth-child(2)`).pause(1000);
                },
                validatorUpload: function(sortIndex, path){
                    return this.api.setValue(`${this.tabContent(sortIndex)} .validatorGroup input[type=file]`, path);
                },
                validatorName: function(sortIndex){
                    return `${this.tabContent(sortIndex)} .validatorGroup label:first-child`;
                },
                name: function(sortIndex){
                    return `${this.tabContent(sortIndex)} input[type=text]`;
                },
                description: function(sortIndex){
                    return `${this.tabContent(sortIndex)} .note-editable`;
                },
                weighting: function(sortIndex){
                    return `${this.tabContent(sortIndex)} input.eInfoWeighting`;
                },
                points: function(sortIndex){
                    return `${this.tabContent(sortIndex)} input.eInfoPoints`;
                },
                submitFile: function(sortIndex){
                    return `${this.tabContent(sortIndex)} input.eInfoSubmitFile`;
                },
                fileTypes: function(sortIndex){
                    return `${this.tabContent(sortIndex)} .multiselect`;
                },
                fileTypesInput: function(sortIndex){
                    return `${this.tabContent(sortIndex)} .multiselect input[type="text"]`;
                },
                mandatory: function(sortIndex){
                    return `${this.tabContent(sortIndex)} input.eInfoMandatory`;
                },
                validatorContainer: function(sortIndex){
                    return `${this.tabContent(sortIndex)} .validatorContainer`;
                },
                deleteValidatorIcon: function(sortIndex){
                    return `${this.tabContent(sortIndex)} .validatorGroup a:nth-of-type(2)`;
                },
                deleteValidator: function(sortIndex){
                    return this.api.click(this.deleteValidatorIcon(sortIndex));
                },
                validatorActions: function(sortIndex){
                    return `${this.tabContent(sortIndex)} .validatorGroup a`;
                }
            }],
            elements: {
            }
        },
        deleteModal: {
            selector: '#modal-delete-example',
            commands: [modalCommands]
        }
    }
}