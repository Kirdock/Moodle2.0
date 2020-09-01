module.exports = [
    {
        name: 'My Example Nr. 1',
        description: 'My Example 1 description',
        weighting: 1,
        points: 10,
        uploadCount: 0,
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
        uploadCount: 10,
        fileTypes: [],
        validator: true,
        mandatory: false,
        subExamples: [
            {
                name: 'My SubExample Nr. 1',
                description: 'My SubExample 1 description',
                weighting: 2,
                points: 4,
                submitFile: false,
                mandatory: true,
            }
        ]
    },
    {
        name: 'My Example Nr. 3',
        description: 'My Example 3 description',
        weighting: 1,
        points: 0,
        uploadCount: 1,
        submitFile: true,
        fileTypes: ['Word', 'zip'],
        validator: true,
        mandatory: false,
        subExamples: []
    }
]