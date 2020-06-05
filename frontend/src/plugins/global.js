import i18n from '@/plugins/i18n';

export const orderManagement = {
    move(array, oldIndex, newIndex) {
        array.splice(newIndex, 0, array.splice(oldIndex, 1)[0]);
    },
    sort(array){
        array.sort((a,b) => a-b);
    },
    revertSort(array){
        array.forEach((element, index) =>{
            element.order = index;
        });
    },
    deletedAt(array, index){
        const changedArray = [];
        for(let i = index + 1; i < array.length; i++){
            array[i].order--;
            changedArray.push({
                id: array[i].id,
                order: array[i].order
            });
        }
        array.splice(index,1);
        return changedArray;
    },
    moveTo(array, oldIndex, newIndex){
        array[oldIndex].order = newIndex;
        const changedArray = [{
            id: array[oldIndex].id,
            order: array[oldIndex].order
        }];
        
        if(oldIndex > newIndex){
            for(let i = newIndex; i < oldIndex; i++){
                array[i].order++;
                changedArray.push({
                    id: array[i].id,
                    order: array[i].order
                });
            }
        }
        else{
            for(let i = newIndex; i > oldIndex; i--){
                array[i].order--;
                changedArray.push({
                    id: array[i].id,
                    order: array[i].order
                });
            }
        }
        return changedArray;
    }
}

export const dateManagement = {
    currentDateTime(customDate){
        const date = customDate || new Date();
        return new Date(date.getTime() - (date.getTimezoneOffset() * 60000)).toISOString().split(/ *:..\..../)[0];
    },
    midnightDateTime(){
        const date = new Date();
        date.setHours(23,59);
        return this.currentDateTime(date);
    }
}

export const userManagement = {
    roles (){ 
        return [
            {
                key: 'l',
                value: i18n.t('lecturer'),
            },
            {
                key: 't',
                value: i18n.t('tutor'),
            },
            {
                key: 's',
                value: i18n.t('student'),
            },
        ]
    },
    rolesWithAll(){
        return [
            {
                key: 'a',
                value: i18n.t('all')
            }
        ].concat(this.roles());
    },
    rolesAllAssign(){
        return this.rolesWithAll().concat([
            {
                key: 'z',
                value: i18n.t('assigned')
            }
        ]);
    },
    filteredUsers ({users, role = 'a', searchText}){
        let result = users;

        if(role === 'z'){
            result = result.filter(user => user.courseRole !== 'n');
        }
        else if(role !== 'a'){
            result = result.filter(user => role === user.courseRole)
        }
        
        if(searchText){
            result = result.filter(user => user.matriculationNumber.indexOf(searchText) !== -1
                                        || user.surname.indexOf(searchText) !== -1
                                        || user.forename.indexOf(searchText) !== -1);
        }
        return result;
    }
}

export const editorManagement = {
    onReady(editor) {
        editor.ui.getEditableElement().parentElement.insertBefore(
            editor.ui.view.toolbar.element,
            editor.ui.getEditableElement()
        );
    }
}

export const fileManagement = {
    getFileNameOutOfHeader(headers){
        let fileName = '';
        const disposition = headers['content-disposition'];
        if (disposition && disposition.indexOf('filename') !== -1) {
            const reg = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            const matches = reg.exec(disposition);
            if (matches != null && matches[1]) { 
              fileName = matches[1].replace(/['"]/g, '');
            }
        }
        return fileName;
    },
    downloadFile(data, headers){
        const url = window.URL.createObjectURL(data);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', this.getFileNameOutOfHeader(headers));
        document.body.appendChild(link);
        link.click();
    }
}

export const exampleManagement = {
    selectMany(array){
        let result = [];
        for(const example of array){
            if(example.subExamples.length === 0){
                result.push({
                    exampleId: example.id,
                    state: example.state,
                    description: example.state === 'm' ? example.submitDescription : undefined
                });
            }
            else{
                result = result.concat(example.subExamples.map(subExample => {
                    return {
                        exampleId: subExample.id,
                        state: subExample.state,
                        description: subExample.state === 'm' ? subExample.submitDescription : undefined
                    }
                }));
            }
        }
        return result;
    }
}