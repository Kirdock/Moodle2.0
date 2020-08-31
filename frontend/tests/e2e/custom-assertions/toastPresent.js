/**
 * A custom Nightwatch assertion. The assertion name is the filename.
 *
 * Example usage:
 *   browser.assert.toastPresent()
 */
exports.assertion = function () {
    const selector = '.b-toast';
    this.message = `Testing if element <${selector}> is ${this.negate ? 'not ' : ''}present`
    this.expected = !this.negate;
  
    this.pass = function(val){
      return  val === true;
    }
    this.value = function(res){
      return res.value === true; //if result.value is true it is true, else it is an empty array []
    }
    
    this.command = function (cb) {
      this.api.isVisible({selector, suppressNotFoundErrors: true, timeout: 2000}, cb)
    }
}