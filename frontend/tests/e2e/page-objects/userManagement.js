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
    showModalDelete: function(){
        this.click('@rowDeleteButton');
        return this.section.modal_delete.waitForElementVisible('@submitButton',1000);
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
    }
};


module.exports = {
    url: '/Admin/UserManagement',
    commands: [userCommands],
    elements: {
        searchBar: '#searchText',
        tableEntries: 'table tr',
        firstTableCell: 'table tr:nth-child(1) td:first-child', 
        rowEditButton: {
            selector: 'table tr:nth-child(1) td:last-child a',
            index: 0
        },
        rowDeleteButton: {
            selector: 'table tr:nth-child(1) td:last-child a',
            index: 1
        }
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