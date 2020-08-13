exports.command = function (message) {
    return this.perform(function (browser, done) {
        console.log('\033[32m√ \033[0m' + message);
        done();
    });
};