const testExamplesRight = require("./testExamplesRight");
const testExerciseSheets = require("./testExerciseSheets");

module.exports = [
    {
        exerciseSheet: testExerciseSheets[0],
        mandatory: 1,
        mandatoryAfter: 1, //after selecting given examples
        points: 18,
        pointsAfter: 18,
        kreuzel: 3,
        kreuzelAfter: 2,
        examples: [//always write all available examples of the exercise sheet in order here. I iterate through all available examples of this exercisesheet in 'kreuzel test user' of modifyCourse.js
            {
                name: testExamplesRight[0].name,
                isSubExample: false,
                type: true,
                description: 'My kreuzel description',
                submitFile: false,
                uploadCount: -1 //infinity
            },
            {
                name: testExamplesRight[1].subExamples[0].name,
                type: true,
                isSubExample: true,
                submitFile: false
            },
            {
                name: testExamplesRight[2].name,
                isSubExample: false,
                type: false,
                submitFile: false,
                uploadCount: 1
            },
        ]
        
    }
]