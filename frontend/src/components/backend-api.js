import AXIOS from 'axios';
import store from '@/store/index';

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
    getExerciseSheet(sheedId){
        return axios.get(`/exerciseSheet/${sheedId}`)
    },
    updateExerciseSheet(sheetData){
        return axios.post('/exerciseSheet', sheetData);
    },
    deleteExerciseSheet(sheedId){
        return axios.delete(`/exerciseSheet/${sheedId}`);
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
            responseType: 'blob'
        });
    },
    saveKreuzel(examples){
        return axios.post('/user/kreuzel', examples);
    },
    addExampleAttachement(formData){
        return axios.post('/user/kreuzel/attachement',
            formData,
            {
                headers: {
                    'Content-Type': undefined
                }
            }
        )
    }
}


