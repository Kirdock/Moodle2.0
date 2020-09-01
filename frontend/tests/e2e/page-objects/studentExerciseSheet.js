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
                    selector: `//div[@class="exerciseSheet"]//table/tbody/tr[td[1][${isSubExample ? 'boolean(text()) = true' : `text()=" ${example.name} "`}] and td[2][${isSubExample ? `text()=" ${example.name} "` : 'boolean(text()) = true'}] and td[3][text()=" ${example.mandatory ? 'Ja' : 'Nein'} "] and td[4][text()=" ${example.weighting} "] and td[5][text()=" ${example.points} "] and td[6][${exerciseSheet.kreuzelType === 1 ? `count(.//input[@type="radio"${exerciseSheet.deadlineReached ? 'and @disabled="disabled"' : 'and not(@disabled="disabled")'}]) = 3` : `input[@type="checkbox" ${exerciseSheet.deadlineReached ? 'and @disabled="disabled"' : `and not(@disabled="disabled")`}]`}] and td[7][${example.submitFile && example.uploadCount === 0 ? 'span[contains(@class, "fa-infinity")]': example.submitFile ? `contains(text(),/${exerciseSheet.uploadCount})` : 'boolean(text()) = true'}] and td[8][${example.submitFile ? `label/input[${exerciseSheet.deadlineReached ? '@disabled="disabled"' : `not(@disabled="disabled")`}]` : 'boolean(text()) = true'}]]`,
                    locateStrategy: 'xpath'
                });
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
        }
    ]
}