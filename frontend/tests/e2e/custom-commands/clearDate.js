module.exports = class {
  command (selector) {
    const { RIGHT_ARROW, BACK_SPACE } = this.api.Keys;
    return this.api.getValue(selector, result => {
      const chars = result.value.split(/[\-\:T]/);
      chars.forEach(() => this.api.setValue(selector, `${BACK_SPACE}${RIGHT_ARROW}`));
    });
  }
}
