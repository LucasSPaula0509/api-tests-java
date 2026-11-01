package base;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import util.Env;

public abstract class BaseApiTest {

  @BeforeAll
  static void setup() {
    RestAssured.baseURI = Env.baseUrl();
  }

  @Step("Bearer setado no Rest-Assured")
  protected void setBearer(String token) {
    RestAssured.requestSpecification =
        RestAssured.given()
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json");
  }
}
