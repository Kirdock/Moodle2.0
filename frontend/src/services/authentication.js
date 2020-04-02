'use strict'

let cachedToken;
const storage = window.localStorage;
const tokenName = 'SessionToken';

const authorization = {
    getToken: getToken,
    setToken: setToken,
    hasToken: hasToken,
    getDecodedToken: getDecodedToken,
    deleteToken: deleteToken,
    isLoggedIn: isLoggedIn,
    isTeacher: isTeacher
}



function getToken(){
    if (!cachedToken) {
        const tempToken = storage.getItem(tokenName);
        if(tempToken){
            cachedToken = tempToken;
        }
    }
    return cachedToken;
}

function setToken(token){
    cachedToken = token;
    storage.setItem(tokenName, token);
}

function hasToken(){
    return !!getToken();
}

function deleteToken(){
    cachedToken = undefined;
    storage.removeItem(tokenName);
}


function getDecodedToken () {
    let decoded_payload;
    let payload;
    const token = getToken();

    if (token) {
        payload = token.split(".")[1];
        switch (payload.length % 4) {
            case 0:
                break;
            case 1:
                payload += "===";
                break;
            case 2:
                payload += "==";
                break;
            case 3:
                payload += "=";
                break;
        }
        decoded_payload = JSON.parse(atob(payload));
    }
    return decoded_payload;
}

function isLoggedIn(){
    return !!getToken();
}

function isTeacher(){
    const payload = getDecodedToken();
    return payload && payload.teacher;
}

export default authorization;