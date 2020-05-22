package com.aau.moodle20;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExerciseSheetTests extends AbstractTest{




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
