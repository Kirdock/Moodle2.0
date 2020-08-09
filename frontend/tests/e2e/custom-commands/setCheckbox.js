module.exports = class {
    command (selector, value) {
        if(typeof value !== 'boolean') return;

        this.api.execute(function (selector){
            if (typeof selector === 'object' && selector.selector) {
                selector = selector.selector
            }
            return document.querySelector(selector).checked; //Dont't set checked! The v-model of the vue-component does not change if it is done this way
        },[selector],function(result){
            if(result.value !== value){
                this.click(selector);
            }
        })
    }
  }
  