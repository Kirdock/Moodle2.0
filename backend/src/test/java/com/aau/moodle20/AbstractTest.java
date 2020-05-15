package com.aau.moodle20;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static io.restassured.RestAssured.given;

@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public abstract class AbstractTest {

	protected MockMvc mvc;
	@LocalServerPort
	int port;

	@Autowired
	private WebApplicationContext context;

	@Before
	public void setup() {
		this.mvc = MockMvcBuilders.webAppContextSetup(context).build();
		RestAssured.port = port;
	}


	protected String mapToJson(Object obj) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(obj);
	}
	protected <T> T mapFromJson(String json, Class<T> clazz)
			throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(json, clazz);
	}


	protected String obtainAccessToken(String username, String password) throws Exception {
		JSONObject requestParams = new JSONObject();
		requestParams.put("username", "admin");
		requestParams.put("password", "admin");

		Response response = given()
				.header("Content-Type", "application/json")
				.body(requestParams.toString())
				.when().basePath("/api").post("/login");

		JSONObject jsonObject = new JSONObject(response.getBody().asString());
		String accessToken = jsonObject.get("accessToken").toString();
		String tokenType = jsonObject.get("tokenType").toString();
		return tokenType + " " + accessToken;
	}
}
