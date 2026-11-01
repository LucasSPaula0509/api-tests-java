package br.com.dummyjson.tests;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

import br.com.dummyjson.client.AuthClient;
import br.com.dummyjson.client.UserClient;
import br.com.dummyjson.data.Tags;
import br.com.dummyjson.data.TestData;
import br.com.dummyjson.model.Credentials;
import br.com.dummyjson.model.LoginRequest;
import br.com.dummyjson.model.LoginResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Epic("DummyJSON API")
@Feature("Autenticação")
@DisplayName("Autenticação")
class AuthTest {

  @Test
  @Tag(Tags.SMOKE)
  @Tag(Tags.REGRESSION)
  @DisplayName("Login com usuário de /users retorna tokens e respeita o contrato")
  void loginReturnsTokensAndMatchesContract() {
    Credentials credentials = UserClient.firstUserCredentials();

    Response response = AuthClient.login(credentials.toLoginRequest());

    response.then().body(matchesJsonSchemaInClasspath("schemas/login-response.schema.json"));
    LoginResponse body = response.as(LoginResponse.class);
    assertThat(body.accessToken()).as("accessToken").isNotBlank();
    assertThat(body.refreshToken()).as("refreshToken").isNotBlank();
    assertThat(body.username()).as("Usuário autenticado").isEqualTo(credentials.username());
  }

  @Test
  @Tag(Tags.REGRESSION)
  @Tag(Tags.KNOWN_DEFECT)
  @Issue("DEF-001")
  @DisplayName("Login com credenciais válidas retorna 201 Created (documentação)")
  void loginReturnsCreated() {
    Response response = AuthClient.login(UserClient.firstUserCredentials().toLoginRequest());

    assertThat(response.statusCode()).as("Status de POST /auth/login").isEqualTo(201);
  }

  @Test
  @Tag(Tags.REGRESSION)
  @DisplayName("Login com senha incorreta retorna 400 Invalid credentials")
  void loginWithWrongPasswordIsRejected() {
    String username = UserClient.firstUserCredentials().username();

    Response response = AuthClient.login(new LoginRequest(username, TestData.WRONG_PASSWORD));

    assertThat(response.statusCode()).as("Status com senha incorreta").isEqualTo(400);
    assertThat(response.jsonPath().getString("message"))
        .as("Mensagem de erro")
        .isEqualTo("Invalid credentials");
  }

  @Test
  @Tag(Tags.REGRESSION)
  @DisplayName("GET /auth/me com token válido retorna o usuário autenticado")
  void meReturnsAuthenticatedUser() {
    Credentials credentials = UserClient.firstUserCredentials();
    LoginResponse login = AuthClient.login(credentials.toLoginRequest()).as(LoginResponse.class);

    Response response = AuthClient.me(login.accessToken());

    assertThat(response.statusCode()).as("Status de GET /auth/me").isEqualTo(200);
    assertThat(response.jsonPath().getString("username"))
        .as("Usuário retornado")
        .isEqualTo(credentials.username());
  }

  @Test
  @Tag(Tags.REGRESSION)
  @DisplayName("POST /auth/refresh com refreshToken válido retorna novos tokens")
  void refreshReturnsNewTokens() {
    LoginResponse login =
        AuthClient.login(UserClient.firstUserCredentials().toLoginRequest())
            .as(LoginResponse.class);

    Response response = AuthClient.refresh(login.refreshToken());

    assertThat(response.statusCode()).as("Status de POST /auth/refresh").isEqualTo(200);
    assertThat(response.jsonPath().getString("accessToken")).as("Novo accessToken").isNotBlank();
    assertThat(response.jsonPath().getString("refreshToken")).as("Novo refreshToken").isNotBlank();
  }
}
