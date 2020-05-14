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
    deleteUser(matrikelNummer){
        return axios.delete(`/user/${matrikelNummer}`);
    },
    createSemester(data){
        return axios.put('/semester',data);
    },
    createCourse(data){
        return axios.put('/course', data);
    },
    updateCourse(data){
        return axios.post('/course',data);
    },
    updateCourseUsers(data){
        return axios.post('/course/assign', data);
    },
    deleteCourse(data){
        return axios.delete(`/course/${data.id}`);
    }
}


