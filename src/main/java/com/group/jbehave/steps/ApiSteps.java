package com.group.jbehave.steps;

import com.group.bdd.framework.ConfigLoader;
import com.group.bdd.framework.StorySteps;
import com.group.bdd.framework.api.RestAssuredAPI;
import com.group.jbehave.entity.Constants;
import io.restassured.response.Response;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

import java.util.HashMap;

import static com.group.bdd.framework.Asserts.assertEquals;
import static com.group.bdd.framework.ConfigLoader.config;

@StorySteps
public class ApiSteps {

    ThreadLocal<Response> response = new ThreadLocal<>();

    private RestAssuredAPI restAssure = new RestAssuredAPI();

    String env = ConfigLoader.config().getString("test.environment");

    @Given("Generate the GET uri and parameters for '$baseuri' endpoint")
    public void generateUri(String baseUri) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Content-Type", Constants.CONST_CONTENT_TYPE_JSON);
        map.put("custom", "mega");

        restAssure.buildGETRequest(config().getString(env + "." + baseUri) + "?page=2", map);
    }

    @Given("Generate the POST uri and payload for '$baseuri' endpoint")
    public void generatePostUri(String baseUri) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Content-Type", Constants.CONST_CONTENT_TYPE_JSON);
        map.put("custom", "mega");

        String payload = "{\n" +
                "    \"name\": \"morpheus\",\n" +
                "    \"job\": \"leader\"\n" +
                "}";

        restAssure.buildPOSTRequest(config().getString(env + "." + baseUri), map, payload);
    }

    @Given("Generate the PUT uri and payload for '$baseuri' endpoint")
    public void generatePutUri(String baseUri) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Content-Type", Constants.CONST_CONTENT_TYPE_JSON);
        map.put("custom", "mega");

        String payload = "{\n" +
                "    \"name\": \"morpheus\",\n" +
                "    \"job\": \"leader\"\n" +
                "}";

        restAssure.buildPOSTRequest(config().getString(env + "." + baseUri) + "/2", map, payload);
    }

    @Given("Generate the PATCH uri and payload for '$baseuri' endpoint")
    public void generatePatchUri(String baseUri) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Content-Type", Constants.CONST_CONTENT_TYPE_JSON);
        map.put("custom", "mega");

        String payload = "{\n" +
                "    \"name\": \"morpheus\",\n" +
                "    \"job\": \"leader\"\n" +
                "}";

        restAssure.buildPOSTRequest(config().getString(env + "." + baseUri) + "/2", map, payload);
    }

    @Given("Generate the DELETE uri and parameters for '$baseuri' endpoint")
    public void generateDeleteUri(String baseUri) {
        HashMap<String, String> map = new HashMap<>();
        restAssure.buildGETRequest(config().getString(env + "." + baseUri) + "?page=2", map);
    }

    @When("I send a GET request")
    public void get() {
        response.set(restAssure.apiGET());
    }

    @When("I send a POST request")
    public void post() {
        response.set(restAssure.apiPOSTMessage());
    }

    @When("I send a PUT request")
    public void put() {
        response.set(restAssure.apiPUTMessage());
    }

    @When("I send a PATCH request")
    public void patch() {
        response.set(restAssure.apiPATCHMessage());
    }

    @When("I send a DELETE request")
    public void delete() {
        response.set(restAssure.apiDELETE());
    }

    @Then("I validate the response with '$code'")
    public void validate(int code) {
        assertEquals("StatusCode", code, response.get().getStatusCode(), false);
    }
}
