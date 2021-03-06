module.exports = class {
    command (selector, index, value) {
        if (typeof selector === 'object' && selector.selector) {
            selector = selector.selector
        }
        const self = this;
        this.api.perform( done => {
            self.api.element('css selector',selector, function (res) {
                self.api.moveTo(res.value.ELEMENT, 2, 2, function() {
                    self.api.mouseButtonClick(0);
                    if(value === undefined){
                        self.api.click(`${selector} .multiselect__element:nth-child(${index+1})>span`)
                    }
                    else{
                        self.api.keys(value);
                        if(index === undefined){
                            self.api.keys(self.api.Keys.ENTER)
                        }
                        else{
                            self.api.click(`${selector} .multiselect__element:nth-child(${index+1})>span`)
                        }
                    }
                    
                    self.api.element('css selector', `${selector}.multiselect--active`, function(res2){//close multiselect if still open
                        if(res2.value.ELEMENT){
                            self.api.click(`${selector} .multiselect__select`, ()=> self.api.pause(1000, done))
                        }
                        else{
                            self.api.pause(1000, done)
                        }
                    })
                })
            });
        })
    }
  }
  