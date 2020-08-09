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

const modalCommands = {
    submit: function() {
        return this.click('@submitButton');
    },
    isPresent: function(){
        return this.expect.section('@modal_new').to.be.visible;
        // return this.elementPresent('@submitButton');
    }
}

const userCommands = {
    showModalNew: function(){
        this.section.creation.click('@newButton');
        return this.section.modal_new.waitForElementVisible('@submitButton', 1000);
    }
};


module.exports = {
    url: '/Admin/UserManagement',
    commands: [userCommands],
  
    // A page object can have elements
    elements: {
        searchBar: '#searchText'
    },
  
    // Or a page objects can also have sections
    sections: {
        creation: {
            selector: '.form-horizontal .form-inline',
        
            elements: {
                newButton: {
                    selector: 'button',
                    index: 0
                },
                uploadButton: {
                    selector: 'button',
                    index: 1
                },
                isAdmin: 'input'
            },
        },
        modal_new: {
            selector: '#modal-new-user',
            commands: [modalCommands],
            elements: {
                isAdmin: '#modal-new-user input[type="checkbox"]',
                username: {
                    selector: '#modal-new-user input[type="text"]',
                    index: 0
                },
                matriculationNumber: {
                    selector: '#modal-new-user input[type="text"]',
                    index: 1
                },
                surname: {
                    selector: '#modal-new-user input[type="text"]',
                    index: 2
                },
                forename: {
                    selector: '#modal-new-user input[type="text"]',
                    index: 3
                },
                email: '#modal-new-user input[type="email"]',
                submitButton: '.modal-footer button.btn.btn-primary',
                cancelButton: '.modal-footer button.btn.btn-secondary',
                closeButton: 'button.close'
            }
        }
    }
}