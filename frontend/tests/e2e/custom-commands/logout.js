module.exports = {
    command: function () {
        const self = this;
        self.perform(done =>{
            self.element('xpath', '//nav/div[1]/a[last()]', function(result){
                if(result.value && result.value.ELEMENT){
                    self.elementIdClick(result.value.ELEMENT, done)
                }
                else{
                    done();
                }
            })
        })
    }
}