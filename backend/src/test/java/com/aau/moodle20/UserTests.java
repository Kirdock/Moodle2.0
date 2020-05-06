package com.aau.moodle20;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;

public class UserTests  extends AbstractTest{

    @Test
    public void secured_api_should_react_with_unauthorized_per_default() {

        /*get("/api/users")
                .then()
                .assertThat()
                .statusCode(401);*/

        given().basePath("/api").get("/users").then().statusCode(401);
    }
}
