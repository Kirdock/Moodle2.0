module.exports = class {
    command (selector, value) {
        if(typeof value !== 'number') return;
        
        if (typeof selector === 'object' && selector.selector) {
            selector = selector.selector
        }
        this.api.execute(function (selector, index){
            document.querySelector(`${selector} .multiselect__element:nth-child(${index+1})>span`).click();
        },[selector, value])
    }
  }
  