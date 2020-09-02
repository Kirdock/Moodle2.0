/**
 * A custom Nightwatch assertion. The assertion name is the filename.
 *
 * Example usage:
 *   browser.assert.isValidInput('input#password', 'valid', false)
 *
 *
 * @param {string|object} selector
 * @param {string} stateAttr
 * @param {object} stateValue
 */
exports.assertion = function isValidInput (selector, stateAttr, stateValue, isXpath = false) {
    this.message = 'Testing if element <' + (selector.selector || selector) + '> has ValidityState ' + stateAttr + ': ' + stateValue
    this.expected = stateValue;
    
    this.pass = function(val){
      return val === this.expected;
    }
    this.value = function(res){
      return res.value[stateAttr];
    }
  
    this.command = function (cb) {
      return this.api.execute(
        function (selector, isXpath){
          const index = selector.index || 0;
          if (typeof selector === 'object' && selector.selector) {
            selector = selector.selector
          }
          let element;
          if(isXpath){
            element = document.evaluate(selector, document, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null).snapshotItem(index);
          }
          else{
            element = document.querySelectorAll(selector)[index];
          }
          return element.validity;
        },
        [selector, isXpath],
        cb
      )
    }
}