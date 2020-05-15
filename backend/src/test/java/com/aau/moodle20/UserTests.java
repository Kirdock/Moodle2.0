package com.aau.moodle20;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserTests  extends AbstractTest{

    @Test
    public void secured_api_should_react_with_unauthorized_per_default() throws Exception {

        /*get("/api/users")
                .then()
                .assertThat()
                .statusCode(401);*/



        given().basePath("/api").get("/users").then().statusCode(401);
    }

    @Test
    public void test_secured_api_with_admin_credential() throws Exception {

        String accessToken = obtainAccessToken("admin","admin");

       Response response= given().header("Authorization",accessToken)
                .basePath("/api").get("/users");
        assertEquals(response.getStatusCode(), HttpStatus.OK.value());
    }
}
