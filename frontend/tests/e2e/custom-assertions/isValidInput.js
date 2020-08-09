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
exports.assertion = function isValidInput (selector, stateAttr, stateValue) {
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
        function (selector){
          const index = selector.index || 0;
          if (typeof selector === 'object' && selector.selector) {
            selector = selector.selector
          }
          return document.querySelectorAll(selector)[index].validity;
        },
        [selector],
        cb
      )
    }
}