module.exports = class {
    command (selector, index, value) {
        if (typeof selector === 'object' && selector.selector) {
            selector = selector.selector
        }
        const self = this;
        this.api.perform(done =>{
            self.api.elements('css selector',`${selector} .multiselect__tag-icon`, function (res) {
                if(res.value && res.value.length !== 0){
                    for(let i = res.value.length - 1; i >= 0; i--){ //if it is done from start to end the position of the elements after the first one are changed which results in invalid elements
                        self.api.elementIdClick(res.value[i].ELEMENT);
                    }
                    done();
                }
                else{
                    done();
                }
            })
        })
    }
  }
  