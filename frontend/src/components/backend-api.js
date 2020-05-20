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
    getSemesters(){
        return axios.get('/semesters');
    },
    getCourse(data){
        return axios.get(`/course/${data.courseId}`); //all data
    },
    getCourses(semesterData){
        return axios.get(`/semester/${semesterData.id}/courses`); //only id, name, number
    },
    createCourse(data){
        return axios.put('/course', data);
    },
    updateCourse(data){
        return axios.post('/course',data);
    },
    copyCourse(data){
        return axios.put(`/course/copy`, data);
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
    udpatePassword(data){
        return axios.post('/user/password', data);
    },
    deleteUser(matrikelNummer){
        return axios.delete(`/user/${matrikelNummer}`);
    },
    createSemester(data){
        return axios.put('/semester',data);
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
    }
}


