package br.com.dummyjson.tests;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import br.com.dummyjson.client.Specs;
import br.com.dummyjson.data.Tags;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Epic("DummyJSON API")
@Feature("Saúde")
@DisplayName("Saúde da API")
class HealthTest {

  @Test
  @Tag(Tags.SMOKE)
  @Tag(Tags.REGRESSION)
  @DisplayName("GET /test retorna 200 com status ok")
  void healthCheckIsOk() {
    Response response = given().spec(Specs.json()).get("/test");

    assertThat(response.statusCode()).as("Status de GET /test").isEqualTo(200);
    assertThat(response.jsonPath().getString("status"))
        .as("Campo status")
        .isEqualToIgnoringCase("ok");
    assertThat(response.jsonPath().getString("method")).as("Campo method").isEqualTo("GET");
  }
}
