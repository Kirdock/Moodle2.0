const modalCommands = require("../modalCommands");

module.exports = {
    url: '/Course/%s/ExerciseSheet/%s',
    commands: [
        {
            validateExerciseSheet(exerciseSheet){
                return this.api.assert.elementPresent({
                    selector: `//div[@class="exerciseSheet"]/h2[contains(text(),"${exerciseSheet.submissionDateFormat}")]`,
                    locateStrategy: 'xpath'
                }).assert.elementPresent({
                    selector: `//div[@class="exerciseSheet"]/div[1]/label/strong[text()="${exerciseSheet.minKreuzel}%"]`,
                    locateStrategy: 'xpath'
                }).assert.elementPresent({
                    selector: `//div[@class="exerciseSheet"]/div[2]/label/strong[text()="${exerciseSheet.minPoints}%"]`,
                    locateStrategy: 'xpath'
                })
            },
            checkExample(example, exerciseSheet, isSubExample){
                return this.api.assert.elementPresent({
                    selector: `//div[@class="exerciseSheet"]//table/tbody/tr[td[1][${isSubExample ? 'boolean(text()) = true' : `text()=" ${example.name} "`}] and td[2][${isSubExample ? `text()=" ${example.name} "` : 'boolean(text()) = true'}] and td[3][text()=" ${example.mandatory ? 'Ja' : 'Nein'} "] and td[4][text()=" ${example.weighting} "] and td[5][text()=" ${example.points} "] and td[6][${exerciseSheet.kreuzelType === 1 ? `count(.//input[@type="radio"${exerciseSheet.deadlineReached ? 'and @disabled="disabled"' : 'and not(@disabled="disabled")'}]) = 3` : `input[@type="checkbox" ${exerciseSheet.deadlineReached ? 'and @disabled="disabled"' : `and not(@disabled="disabled")`}]`}] and td[7][${example.submitFile && example.uploadCount === 0 ? 'span[contains(@class, "fa-infinity")]': example.submitFile ? `contains(text(),${exerciseSheet.uploadCount})` : 'boolean(text()) = true'}] and td[8][${example.submitFile ? `label/input[${exerciseSheet.deadlineReached ? '@disabled="disabled"' : `not(@disabled="disabled")`}]` : 'boolean(text()) = true'}]]`,
                    locateStrategy: 'xpath'
                });
            },
            checkKreuzelInfo(kreuzelInfo, bySystem){
                if(typeof kreuzelInfo.type === 'boolean'){
                    const validation = this.expect.element({
                        selector: this.kreuzel(kreuzelInfo.name, kreuzelInfo.isSubExample),
                        locateStrategy: 'xpath'
                    }).to;
                    if(kreuzelInfo.type){
                        validation.be.selected;
                    }
                    else{
                        validation.not.be.selected;
                    }
                }
                else{
                    this.expect.element({
                            selector: this.kreuzelOption(kreuzelInfo.name, kreuzelInfo.isSubExample, kreuzelInfo.type),
                            locateStrategy: 'xpath'
                        }).to.be.selected;
                    if(kreuzelInfo.type === 3 && !bySystem){
                        this.assert.value({
                            selector: this.description(kreuzelInfo.name, kreuzelInfo.isSubExample),
                            locateStrategy: 'xpath'
                            }, kreuzelInfo.description);
                    }
                }
            },
            validateExample(example, exerciseSheet){
                if(example.subExamples.length === 0){
                    this.checkExample(example, exerciseSheet, false);
                }
                else{
                    this.api.assert.elementPresent({
                        selector: `//div[@class="exerciseSheet"]//table/tbody/tr[td[1][text()=" ${example.name} "] and td[2][boolean(text()) = true] and td[3][boolean(text()) = true] and td[4][boolean(text()) = true] and td[5][boolean(text()) = true] and td[6][boolean(text()) = true] and td[7][boolean(text()) = true] and td[8][boolean(text()) = true]] `,
                        locateStrategy: 'xpath'
                    })
                }
                for(const subExample of example.subExamples){
                    this.checkExample(subExample, exerciseSheet, true);
                }
            },
            validateKreuzelInfo(kreuzel, bySystem = false){
                //ignore submitFile and description for changes done by owner or admin
                const self = this;
                for(const example of kreuzel.examples){
                    self.api.perform(function(done){
                        self.checkKreuzelInfo(example, bySystem);
                        if(example.uploadCount !== undefined){
                            self.checkUploadCount(example.name, example.isSubExample, example.submitFile && !bySystem ? example.uploadCount - 1 : example.uploadCount);
                            if(example.submitFile && !bySystem){
                                self.checkExampleAfterUpload(example);
                            }
                        }
                        done();
                    })
                }
                self.checkMandatory(kreuzel.mandatoryAfter, kreuzel.mandatory);
                self.checkKreuzel(kreuzel.kreuzelAfter, kreuzel.kreuzel, kreuzel.exerciseSheet.minKreuzel)
                self.checkPoints(kreuzel.pointsAfter, kreuzel.points, kreuzel.exerciseSheet.minPoints)
            },
            checkMandatory(first, second){
                return this.api.assert.elementPresent({
                    selector: `//tr[last()]/td[3][contains(@style,"color: ${first === second ? 'green' : 'red'};") and text()=" ${first}/${second} "]`,
                    locateStrategy: 'xpath'
                })
            },
            checkKreuzel(reached, maximum, required){
                return this.api.assert.elementPresent({
                    selector: `//tr[last()]/td[6][contains(@style,"color: ${(maximum * required /100) <= reached ? 'green' : 'red'};") and text()=" ${reached}/${maximum} "]`,
                    locateStrategy: 'xpath'
                })
            },
            checkPoints(reached, maximum, required){
                return this.api.assert.elementPresent({
                    selector: `//tr[last()]/td[5][contains(@style,"color: ${(maximum * required /100) <= reached ? 'green' : 'red'};") and text()=" ${reached}/${maximum} "]`,
                    locateStrategy: 'xpath'
                })
            },
            checkUploadCount(exampleName, isSubExample, count){
                return this.api.assert.elementPresent({
                    selector: `//div[@class="exerciseSheet"]//table/tbody/tr[td[1][${isSubExample ? 'boolean(text()) = true' : `text()=" ${exampleName} "`}] and td[2][${isSubExample ? `text()=" ${exampleName} "` : 'boolean(text()) = true'}]]/td[7][${count === -1 ? 'span[contains(@class,"fa-infinity")]' : `text() = " ${count} "`}]`,
                    locateStrategy: 'xpath'
                });
            },
            kreuzel(exampleName, isSubExample){
                return `//div[@class="exerciseSheet"]//table/tbody/tr[td[1][${isSubExample ? 'boolean(text()) = true' : `text()=" ${exampleName} "`}] and td[2][${isSubExample ? `text()=" ${exampleName} "` : 'boolean(text()) = true'}]]/td[6]/input`
            },
            kreuzelOption(exampleName, isSubExample, option){
                return `//div[@class="exerciseSheet"]//table/tbody/tr[td[1][${isSubExample ? 'boolean(text()) = true' : `text()=" ${exampleName} "`}] and td[2][${isSubExample ? `text()=" ${exampleName} "` : 'boolean(text()) = true'}]]/td[6]/div/div[${option}]/input`
            },
            setKreuzelBool(exampleName, isSubExample, status){
                return this.setCheckbox(`//div[@class="exerciseSheet"]//table/tbody/tr[td[1][${isSubExample ? 'boolean(text()) = true' : `text()=" ${exampleName} "`}] and td[2][${isSubExample ? `text()=" ${exampleName} "` : 'boolean(text()) = true'}]]/td[6]/input`, status, true)
            },
            setKreuzel(exampleName, isSubExample, option){
                if(typeof option === 'boolean'){
                    return this.setKreuzelBool(exampleName, isSubExample, option);
                }
                else{
                    return this.setKreuzelRadio(exampleName, isSubExample, option);
                }
            },
            setKreuzelRadio(exampleName, isSubExample, option){
                return this.api.click('xpath', `//div[@class="exerciseSheet"]//table/tbody/tr[td[1][${isSubExample ? 'boolean(text()) = true' : `text()=" ${exampleName} "`}] and td[2][${isSubExample ? `text()=" ${exampleName} "` : 'boolean(text()) = true'}]]/td[6]/div/div[${option}]/input`)
            },
            description(exampleName, isSubExample){
                return `//div[@class="exerciseSheet"]//table/tbody/tr[td[1][${isSubExample ? 'boolean(text()) = true' : `text()=" ${exampleName} "`}] and td[2][${isSubExample ? `text()=" ${exampleName} "` : 'boolean(text()) = true'}]]/td[6]//textarea`;
            },
            uploadButton(exampleName, isSubExample){
                return `//div[@class="exerciseSheet"]//table/tbody/tr[td[1][${isSubExample ? 'boolean(text()) = true' : `text()=" ${exampleName} "`}] and td[2][${isSubExample ? `text()=" ${exampleName} "` : 'boolean(text()) = true'}]]/td[8]//input`;
            },
            resultButton(exampleName, isSubExample){
                return `//div[@class="exerciseSheet"]//table/tbody/tr[td[1][${isSubExample ? 'boolean(text()) = true' : `text()=" ${exampleName} "`}] and td[2][${isSubExample ? `text()=" ${exampleName} "` : 'boolean(text()) = true'}]]/td[8]/a[3]`;
            },
            checkExampleAfterUpload(example){
                this.checkUploadCount(example.name, example.isSubExample, example.uploadCount - 1);
                const resultModal = this.section.resultModal;
                if(example.uploadCount === 1){
                    this.expect.element({
                            selector: this.uploadButton(example.name, example.isSubExample),
                            locateStrategy: 'xpath'
                        }).to.not.be.enabled;
                }
                this.api.click('xpath',this.resultButton(example.name, example.isSubExample)).pause(1000);
                resultModal.containsResult();
                resultModal.cancelX();
                resultModal.pause(1000);
                this.expect.section('@resultModal').to.not.be.present;
            },
            submit(){
                return this.click('@submitButton');
            }
        }
    ],
    elements: {
        submitButton: 'button[type=submit]'
    },
    sections: {
        resultModal: {
            selector: '#modal-kreuzelResult',
            commands: [modalCommands,{
                containsResult(amount = 1){
                    const result = 'mamuts1243';
                    return this.api.assert.elementPresent({
                        selector: `//div[@id="modal-kreuzelResult"]//tr/td[2][text() = " ${result} "]`,
                        locateStrategy: 'xpath'
                    }).assert.elementCount('#modal-kreuzelResult tr', amount);
                }
            }]
        }
    }
}