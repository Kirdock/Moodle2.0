import config from './config/config.js';
import axios from 'axios';

const options = {
    headers:{
        'Content-Type': 'application/json'
    }
};

let dataservice = {
    fetchCourses: fetchCourses, 
    fetchSemesters: fetchSemesters
}

function fetchSemesters(){
    return axios.get(config.api+'/semesters');
}

function fetchCourses(){
    return axios.get(config.api+'/courses');
}

export default dataservice;