module.exports = class {
    command (selector, value, isXpath = false) {
        if(typeof value !== 'boolean') return;

        this.api.execute(function (selector, isXpath){
            if (typeof selector === 'object' && selector.selector) {
                selector = selector.selector
            }
            let element;
            if(isXpath){
                element = document.evaluate(selector, document, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null).snapshotItem(0);
            }
            else{
                element = document.querySelector(selector);
            }
            return element.checked; //Dont't set checked! The v-model of the vue-component does not change if it is done this way
        },[selector, isXpath],function(result){
            if(result.value !== value){
                this.click(isXpath ? 'xpath' : 'css selector',selector);
            }
        })
    }
  }
  