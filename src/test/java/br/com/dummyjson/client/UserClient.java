package br.com.dummyjson.client;

import static io.restassured.RestAssured.given;

import br.com.dummyjson.model.Credentials;
import io.qameta.allure.Step;
import io.restassured.response.Response;

/** Endpoints de usuários ({@code /users}). */
public final class UserClient {

  private UserClient() {}

  @Step("GET /users")
  public static Response list() {
    return given().spec(Specs.json()).get("/users");
  }

  /** Credenciais do primeiro usuário de {@code /users}, usadas como massa de login. */
  @Step("Obter credenciais do primeiro usuário de /users")
  public static Credentials firstUserCredentials() {
    Response users =
        given()
            .spec(Specs.json())
            .queryParam("limit", 1)
            .queryParam("select", "username,password")
            .get("/users");
    return new Credentials(
        users.jsonPath().getString("users[0].username"),
        users.jsonPath().getString("users[0].password"));
  }
}
