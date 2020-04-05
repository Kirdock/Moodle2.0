import AXIOS from 'axios';
import store from '../store/index';

const axios = AXIOS.create({
  baseURL: `/api`
});

axios.interceptors.request.use(function (config) {
    config.headers.Authorization = 'Bearer ' + store.getters.token;
    return config;
  });


export default {
    login(user, password){
        return axios.post('/login',{
            auth:{
                username: user,
                password: password
            }
        })
    },
    fetchSemesters(){
        return axios.get('/semesters');
    },
    fetchCourses(){
        return axios.get('/courses');
    }
}


