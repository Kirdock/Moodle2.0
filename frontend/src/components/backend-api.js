import AXIOS from 'axios';
import store from '../store/index';
const axios = AXIOS.create({
  baseURL: `/api`
});

axios.interceptors.request.use(function (config) {
    if(store.getters.token){
        config.headers.Authorization = 'Bearer ' + store.getters.token;
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
        return axios.get(`/semesters/${data.semesterId}/course/${data.courseId}`); //all data
    },
    getCourses(semesterData){
        return axios.get(`/semesters/${semesterData.id}/courses`); //only id, name, number
    },
    getUsers(){
        return axios.get('/users'); //without admin
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
    createSemester(data){
        return axios.put('/semester',data);
    },
    createCourse(data){
        return axios.put('/course', data);
    },
    deleteCourse(data){
        return axios.delete(`/course/${data.id}`);
    }
}


