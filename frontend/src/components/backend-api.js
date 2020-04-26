import AXIOS from 'axios';
const axios = AXIOS.create({
  baseURL: `/api`
});
let Token;

axios.interceptors.request.use(function (config) {
    if(Token){
        config.headers.Authorization = 'Bearer ' + Token;
    }
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
    },
    setToken(token){
        Token = token;
    },
    createUser(userData){
        return axios.put('/user',{
            userData: userData
        });
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
    }
}


