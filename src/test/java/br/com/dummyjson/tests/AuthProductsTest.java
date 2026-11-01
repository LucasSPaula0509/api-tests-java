package br.com.dummyjson.tests;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.dummyjson.client.AuthClient;
import br.com.dummyjson.client.UserClient;
import br.com.dummyjson.data.Tags;
import br.com.dummyjson.data.TestData;
import br.com.dummyjson.model.LoginResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Epic("DummyJSON API")
@Feature("Produtos autenticados")
@DisplayName("Produtos autenticados (/auth/products)")
class AuthProductsTest {

  @Test
  @Tag(Tags.SMOKE)
  @Tag(Tags.REGRESSION)
  @DisplayName("Token válido retorna 200 e a lista de produtos")
  void validTokenReturnsProducts() {
    String token =
        AuthClient.login(UserClient.firstUserCredentials().toLoginRequest())
            .as(LoginResponse.class)
            .accessToken();

    Response response = AuthClient.products(token);

    assertThat(response.statusCode()).as("Status com token válido").isEqualTo(200);
    assertThat(response.jsonPath().getList("products")).as("Produtos").isNotEmpty();
  }

  @Test
  @Tag(Tags.REGRESSION)
  @Tag(Tags.KNOWN_DEFECT)
  @Issue("DEF-002")
  @DisplayName("Sem o cabeçalho Authorization retorna 403 Forbidden (documentação)")
  void missingTokenIsForbidden() {
    Response response = AuthClient.productsWithoutToken();

    assertThat(response.statusCode()).as("Status sem token").isEqualTo(403);
    assertThat(response.jsonPath().getString("message"))
        .as("Mensagem de erro")
        .containsIgnoringCase("Authentication");
  }

  @Test
  @Tag(Tags.REGRESSION)
  @DisplayName("Token inválido retorna 401 Invalid/Expired Token")
  void invalidTokenIsUnauthorized() {
    Response response = AuthClient.products(TestData.INVALID_TOKEN);

    assertThat(response.statusCode()).as("Status com token inválido").isEqualTo(401);
    assertThat(response.jsonPath().getString("message"))
        .as("Mensagem de erro")
        .containsIgnoringCase("Invalid/Expired Token");
  }

  @Test
  @Tag(Tags.REGRESSION)
  @Tag(Tags.KNOWN_DEFECT)
  @Issue("DEF-003")
  @DisplayName("Token JWT malformado retorna 401, e não erro de servidor")
  void malformedTokenIsUnauthorized() {
    Response response = AuthClient.products(TestData.MALFORMED_JWT);

    assertThat(response.statusCode()).as("Status com JWT malformado").isEqualTo(401);
  }
}
