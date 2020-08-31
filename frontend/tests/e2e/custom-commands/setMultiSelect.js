module.exports = class {
    command (selector, index, value) {
        if (typeof selector === 'object' && selector.selector) {
            selector = selector.selector
        }
        const self = this;
        this.api.element('css selector',selector, function (res) {
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
                self.api.moveTo(res.value.ELEMENT, -1, -1, function() { //close multiselect
                    self.api.mouseButtonClick(0);
                });
            })
        });
    }
  }
  