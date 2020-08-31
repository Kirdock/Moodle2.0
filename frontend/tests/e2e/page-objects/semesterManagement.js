module.exports = {
    url: '/Admin/SemesterManagement',
    commands: [
        {
            submit: function(){
                return this.click('@submitButton');
            }
        }
    ],
    elements: {
        year: 'input[type=number]',
        submitButton: 'button.btn.btn-primary',
        sSemester: {
            selector: 'input[type=radio]',
            index: 0
        },
        wSemester: {
            selector: 'input[type=radio]',
            index: 1
        }
    }
}