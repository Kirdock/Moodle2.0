/**
 * A better `clearValue` for inputs having a more complex interaction.
 * Why? clearValue is reset if setValue is called after.
 * Why not setValue with empty string? Because setValue appends text instead of replacing
 * Example:
 *  browser.setValue('input#user', 'adminUser')
 *  .setValue('input#password','')
 *  .clearValue('input#user')
 *  .clearValue('input#password')
 *  .setValue('input#password','adminPassword') // 'input#user' value is reverted to 'adminUser'
 * 
 * @export
 * @param {string} selector 
 * @returns 
 */

module.exports = class {
  command (selector, isXpath = false) {
    const using = isXpath ? 'xpath' : 'css selector'
    const { RIGHT_ARROW, BACK_SPACE } = this.api.Keys;
    return this.api.getValue(using, selector, result => {
      if(result.status !== -1){
        const chars = result.value.split('');
        // Make sure we are at the end of the input
        chars.forEach(() => this.api.setValue(using, selector, RIGHT_ARROW));
        // Delete all the existing characters
        chars.forEach(() => this.api.setValue(using, selector, BACK_SPACE));
      }
    });
  }
}
