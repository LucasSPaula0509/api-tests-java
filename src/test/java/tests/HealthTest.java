package tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import base.BaseApiTest;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

public class HealthTest extends BaseApiTest {

  @Test
  @Description("GET /test deve retornar 200 e status=ok")
  void getTest_ok() {
    Response r = given().when().get("/test").then().extract().response();
    assertThat(r.statusCode(), is(200));
    assertThat(r.jsonPath().getString("status"), equalToIgnoringCase("ok"));
    assertThat(r.jsonPath().getString("method"), equalTo("GET"));
  }
}
