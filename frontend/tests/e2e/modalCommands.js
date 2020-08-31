module.exports = {
    submit: function() {
        return this.click('.modal-footer button.btn.btn-primary');
    },
    cancel: function(){
        return this.click('.modal-footer button.btn.btn-secondary');
    },
    cancelX: function(){
        return this.click('.modal-header button.close')
    },
    cancelClick: function(){
        // return this.click('.modal') //does not work, idk why
        return this.api.execute(function(){
            document.querySelector('.modal').click();
        },[]);
    }
};