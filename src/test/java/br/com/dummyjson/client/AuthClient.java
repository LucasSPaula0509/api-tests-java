package br.com.dummyjson.client;

import static io.restassured.RestAssured.given;

import br.com.dummyjson.model.LoginRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.Map;

/** Endpoints de autenticação ({@code /auth/*}). */
public final class AuthClient {

  private AuthClient() {}

  @Step("POST /auth/login como {request.username}")
  public static Response login(LoginRequest request) {
    return given().spec(Specs.json()).body(request).post("/auth/login");
  }

  @Step("POST /auth/refresh")
  public static Response refresh(String refreshToken) {
    return given()
        .spec(Specs.json())
        .body(Map.of("refreshToken", refreshToken))
        .post("/auth/refresh");
  }

  @Step("GET /auth/me com token")
  public static Response me(String accessToken) {
    return given().spec(Specs.json()).auth().oauth2(accessToken).get("/auth/me");
  }

  @Step("GET /auth/products com token")
  public static Response products(String accessToken) {
    return given().spec(Specs.json()).auth().oauth2(accessToken).get("/auth/products");
  }

  @Step("GET /auth/products sem o cabeçalho Authorization")
  public static Response productsWithoutToken() {
    return given().spec(Specs.json()).get("/auth/products");
  }
}
