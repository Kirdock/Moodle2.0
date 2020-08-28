module.exports = [
    {
        name: 'My Example Nr. 1',
        description: 'My Example 1 description',
        weighting: 1,
        points: 10,
        submitFile: true,
        fileTypes: ['Word', 'zip'],
        validator: true,
        mandatory: false,
        subExamples: []
    },
    {
        name: 'My Example Nr. 2',
        description: 'My Example 2 description',
        forceEnterInformation: true,
        weighting: 0,
        points: '',
        submitFile: true,
        fileTypes: [],
        validator: true,
        mandatory: false,
        subExamples: [
            {
                name: 'My SubExample Nr. 1',
                description: 'My SubExample 1 description',
                weighting: 1,
                points: 15,
                submitFile: false,
                mandatory: false,
            }
        ]
    }
]