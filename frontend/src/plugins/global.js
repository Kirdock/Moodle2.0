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
    currentDateTime(){
        const date = new Date();
        return new Date(date.getTime() - (date.getTimezoneOffset() * 60000)).toISOString().split(/ *:..\..../)[0];
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