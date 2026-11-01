package tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static util.AuthClient.getAuthProducts;
import static util.AuthClient.login;
import static util.UserClient.getUsers;

import base.BaseApiTest;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import models.LoginRequest;
import org.junit.jupiter.api.Test;

public class AuthProductsTest extends BaseApiTest {

  private String getValidToken() {
    var users = getUsers();
    String u = users.jsonPath().getString("users[0].username");
    String p = users.jsonPath().getString("users[0].password");
    var auth = login(new LoginRequest(u, p));
    return auth.jsonPath().getString("token");
  }

  @Test
  @Description("GET /auth/products com token válido deve retornar 200 e lista de produtos")
  void authProducts_ok() {
    String token = getValidToken();
    Response r = getAuthProducts(token);
    assertThat(r.statusCode(), is(200));
    assertThat(r.jsonPath().getList("products"), is(not(empty())));
  }

  @Test
  @Description("GET /auth/products sem Authorization deve retornar 403")
  void authProducts_withoutToken_403() {
    Response r = getAuthProducts(""); // sem header válido
    assertThat(r.statusCode(), is(403));
    assertThat(r.jsonPath().getString("message"), containsStringIgnoringCase("Authentication"));
  }

  @Test
  @Description("GET /auth/products com token inválido deve retornar 401")
  void authProducts_invalidToken_401() {
    Response r = getAuthProducts("this.is.not.a.valid.token");
    assertThat(r.statusCode(), is(401));
    assertThat(
        r.jsonPath().getString("message"), containsStringIgnoringCase("Invalid/Expired Token"));
  }
}
