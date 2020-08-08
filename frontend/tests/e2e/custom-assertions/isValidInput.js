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
    this.message = 'Testing if element <' + selector + '> has ValidityState ' + stateAttr + ': ' + stateValue
    this.expected = stateValue
  
    this.pass = function(val){
      console.log('val', val, 'expected', this.expected)
      return val === this.expected;
    }
    this.value = function(res){
      return res.value[stateAttr];
    }
    
  
    this.command = function (cb) {
      var self = this
      return this.api.execute(
        function (selector){
          return document.querySelector(selector).validity
        },
        [selector],
        cb //or just cb?
      )
    }
}