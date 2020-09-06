module.exports = [
    {
        name: {
            value: '',
            valid: false
        },
        description: '',
        weighting: {
            value: 0,
            valid: false
        },
        points: {
            value: 'abc',
            expected: '',
            valid: false
        },
        submitFile: true,
        uploadCount: {
            value: 0,
            valid: true
        },
        mandatory: false,
        fileTypes: {
            value: ['Word', 'zip'],
            valid: true
        },
        validator: {
            value: 'validatorWrong',
            valid: false
        }
    }
]