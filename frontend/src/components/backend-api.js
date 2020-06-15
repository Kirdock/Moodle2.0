import AXIOS from 'axios';
import store from '@/store/index';
import i18n from '@/plugins/i18n';

const axios = AXIOS.create({
  baseURL: `/api`
});

axios.interceptors.request.use(config => {
    if(store.getters.token){
        config.headers.Authorization = 'Bearer ' + store.getters.token;
    }
    return config;
});

axios.interceptors.response.use(config =>{
    if(config.status === 401){
        store.dispatch('logout', true);
    }
    return config;
});


export default {
    login(user, password){
        return axios.post('/login',{
            username: user,
            password: password
        })
    },
    getIsOwner(){
        return axios.get('/user/isOwner');
    },
    getSemesters(){
        return axios.get('/semesters');
    },
    getSemestersAssigned(){
        return axios.get('/semesters/assigned');
    },
    getCourse(data){
        return axios.get(`/course/${data.courseId}`); //all data
    },
    getCourses(semesterData){
        return axios.get(`/semester/${semesterData.id}/courses`); //only id, name, number
    },
    getCourseAssigned(data){
        return axios.get(`/courseAssigned/${data.courseId}`); //all data
    },
    getCoursesAssigned(semesterData){
        return axios.get(`/semester/${semesterData.id}/coursesAssigned`);
    },
    createCourse(data){
        return axios.put('/course', data);
    },
    updateCourse(data){
        return axios.post('/course',data);
    },
    updateCourseDefaultTemplate(templateData){
        return axios.post('/course/template', templateData);
    },
    copyCourse(data){
        return axios.post(`/course/copy`, data);
    },
    updateCourseUsers(data){
        return axios.post('/course/assign', data);
    },
    deleteCourse(data){
        return axios.delete(`/course/${data.id}`);
    },
    getUsers({courseId}){
        return axios.get('/users' + (courseId ? `/course/${courseId}` : '')); //without admin
    },
    getUser(){
        return axios.get(`/user/${store.getters.userInfo.matriculationNumber}`);
    },
    createUser(userData){
        return axios.put('/user',userData);
    },
    createUsers(formData){
        return axios.put('/users',
            formData,
            {
                headers: {
                    'Content-Type': undefined
                }
            }
        );
    },
    assignCourseUsers(formData){
        return axios.post('/course/assignFile',
            formData,
            {
                headers: {
                    'Content-Type': undefined
                }
            }
        )
    },
    udpatePassword(data){
        return axios.post('/user/password', data);
    },
    updateUser(userData){
        return axios.post('/user', userData);
    },
    deleteUser(matriculationNumber){
        return axios.delete(`/user/${matriculationNumber}`);
    },
    createSemester(data){
        return axios.put('/semester',data);
    },
    getFileTypes(){
        return axios.get('/fileTypes');
    },
    createExerciseSheet(data){
        return axios.put('/exerciseSheet', data);
    },
    getExerciseSheets(courseData){
        return axios.get(`/course/${courseData.id}/exerciseSheets`);
    },
    getExerciseSheet(sheetId){
        return axios.get(`/exerciseSheet/${sheetId}`)
    },
    getExerciseSheetAssigned(sheetId){
        return axios.get(`/exerciseSheetAssigned/${sheetId}`);
    },
    updateExerciseSheet(sheetData){
        return axios.post('/exerciseSheet', sheetData);
    },
    deleteExerciseSheet(sheetId){
        return axios.delete(`/exerciseSheet/${sheetId}`);
    },
    createExample(exampleData){
        return axios.put('/example', exampleData);
    },
    updateExample(exampleData){
        return axios.post('/example', exampleData);
    },
    deleteExample(exampleId){
        return axios.delete(`/example/${exampleId}`);
    },
    updateExampleOrder(changedOrders){
        return axios.post('/examples/order', changedOrders);
    },
    getAttendanceList(courseId){
        return axios.get(`/course/${courseId}/attendanceList`,{
            responseType: 'blob',
            headers:{
                'Accept-Language': i18n.locale
            }
        });
    },
    getKreuzelList(sheetId){
        return axios.get(`/exerciseSheet/${sheetId}/kreuzel`)
    },
    saveKreuzel(examples){
        return axios.post('/user/kreuzel', examples);
    },
    saveKreuzelMulti(kreuzel){
        return axios.post('/user/kreuzelMulti', kreuzel);
    },
    addExampleAttachment(formData){
        return axios.post('/user/kreuzel/attachment',
            formData,
            {
                headers: {
                    'Content-Type': undefined
                }
            }
        )
    },
    getExampleAttachment(id){
        return axios.get(`/user/kreuzel/attachment/${id}`,{
            responseType: 'blob'
        });
    },
    getUserKreuzel({matriculationNumber, courseId}){
        return axios.get(`/user/${matriculationNumber}/kreuzel/${courseId}`);
    },
    updatePresented(data){
        return axios.post('/user/examplePresented', data);
    },
    getCoursePresented(courseId){
        return axios.get(`/course/${courseId}/presented`);
    }
}


