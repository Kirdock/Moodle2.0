const testExamplesRight = require("./testExamplesRight");
const testExerciseSheets = require("./testExerciseSheets");

module.exports = [
    {
        exerciseSheet: testExerciseSheets[1],
        mandatory: 1,
        mandatoryAfter: 1, //after selecting given examples
        points: 18,
        pointsAfter: 18,
        kreuzel: 3,
        kreuzelAfter: 3,
        examples: [
            {
                name: testExamplesRight[0].name,
                isSubExample: false,
                type: 3,
                description: 'My kreuzel description',
                submitFile: false,
                uploadCount: -1 //infinity
            },
            {
                name: testExamplesRight[1].subExamples[0].name,
                type: 1,
                isSubExample: true,
                submitFile: false
            },
            {
                name: testExamplesRight[2].name,
                type: 1,
                isSubExample: false,
                submitFile: true,
                uploadCount: 1
            }
        ]
        
    }
]