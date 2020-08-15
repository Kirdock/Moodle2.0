/**
 * A Nightwatch page object. The page object name is the filename.
 *
 * Example usage:
 *   browser.page.homepage.navigate()
 *
 * For more information on working with page objects see:
 *   https://nightwatchjs.org/guide/working-with-page-objects/
 *
 */

const modalCommands = require('./../modalCommands.js');

const userCommands = {
    showModalNew: function(){
        this.section.creation.click('@newButton');
        return this.section.modal_new.waitForElementVisible('@submitButton', 1000);
    },
    showModalDelete: function(matriculationNumber){
        this.click('xpath', `//table//tr[td[1][contains(text(),"${matriculationNumber}")]]/td[5]/a[2]`);
        return this.section.modal_delete.waitForElementVisible('@submitButton',1000);
    },
    showModalEdit: function(matriculationNumber){
        return this.click('xpath', `//table//tr[td[1][contains(text(),"${matriculationNumber}")]]/td[5]/a[1]`);
    },
    isUserModalPresent: function(){
        return this.expect.section('@modal_new').to.be.visible;
    },
    userModalNotPresent: function(){
        return this.expect.section('@modal_new').to.not.be.present;
    },
    deleteModalPresent: function(){
        return this.expect.section('@modal_delete').to.be.visible;
    },
    deleteModalNotPresent: function(){
        return this.expect.section('@modal_delete').to.not.be.present;
    },
    userExists: function(browser, matriculationNumber, callback){
        browser.element('xpath', `//table//tr[td[1][contains(text(),"${matriculationNumber}")]]`, callback)
    },
    userNotPresent: function(matriculationNumber){
        return this.assert.not.elementPresent( {
            selector: `//table//tr[td[1][contains(text(),"${matriculationNumber}")]]`,
            locateStrategy: 'xpath'
        });
    },
    userPresent: function(matriculationNumber){
        return this.assert.elementPresent({
            selector: `//table//tr[td[1][contains(text(),"${matriculationNumber}")]]`,
            locateStrategy: 'xpath'
        });
    },
    userPresentStrict: function(user){
        return this.assert.elementPresent({
            selector: `//table//tr[td[1][contains(text(),"${user.matriculationNumber}")] and td[2][contains(text(),"${user.surname}")] and td[3][contains(text(),"${user.forename}")] and td[4][contains(text(),"${user.isAdmin ? 'Ja' : 'Nein'}")]]`,
            locateStrategy: 'xpath'
        });
    }
};


module.exports = {
    url: '/Admin/UserManagement',
    commands: [userCommands],
    elements: {
        searchBar: '#searchText',
        tableEntries: 'table tr',
        firstTableCell: 'table tr:first-child td:first-child',
        rowEditButton: {
            selector: 'table tr:first-child td:last-child a',
            index: 0
        },
        rowDeleteButton: {
            selector: 'table tr:first-child td:last-child a',
            index: 1
        },
        tableCellmatriculationNumber: 'table tr:first-child td:nth-child(1)',
        tableCellsurname: 'table tr:first-child td:nth-child(2)',
        tableCellforename: 'table tr:first-child td:nth-child(3)',
        tableCellisAdmin: 'table tr:first-child td:nth-child(4)'
    },
    sections: {
        creation: {
            selector: '.form-horizontal .form-inline',
        
            elements: {
                newButton: {
                    selector: 'button',
                    index: 0
                },
                uploadButton: 'input[type="file"]',
                isAdmin: 'input[type="checkbox"]',
            },
        },
        modal_delete: {
            selector: '#modal-delete-user',
            commands: [modalCommands],
            elements: {
                submitButton: '.modal-footer button.btn.btn-primary'
            }
        },
        modal_new: {
            selector: '.modal',
            commands: [modalCommands],
            elements: {
                isAdmin: '.user_info input[type="checkbox"]',
                username: {
                    selector: '.user_info input[type="text"]',
                    index: 0
                },
                matriculationNumber: {
                    selector: '.user_info input[type="text"]',
                    index: 1
                },
                surname: {
                    selector: '.user_info input[type="text"]',
                    index: 2
                },
                forename: {
                    selector: '.user_info input[type="text"]',
                    index: 3
                },
                email: '.user_info input[type="email"]',
                submitButton: '.modal-footer button.btn.btn-primary',
            }
        }
    }
}