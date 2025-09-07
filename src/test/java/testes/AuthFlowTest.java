package tests;

import base.BaseApiTest;
import io.qameta.allure.Description;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.LoginRequest;
import models.LoginResponse;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static util.UserClient.getUsers;
import static util.AuthClient.login;

public class AuthFlowTest extends BaseApiTest {

    @Test
    @Description("Fluxo: pegar um user de /users -> logar em /auth/login (201) -> validar schema e presença do token")
    void login_usingUsersEndpoint_schemaAndToken() {
        Response users = getUsers();
        assertThat(users.statusCode(), is(200));

        JsonPath jp = users.jsonPath();
        String username = jp.getString("users[0].username");
        String password = jp.getString("users[0].password"); // campo exposto na API dummy
        assertThat(username, not(emptyOrNullString()));
        assertThat(password, not(emptyOrNullString()));

        LoginRequest req = new LoginRequest(username, password);
        Response auth = login(req);
        assertThat(auth.statusCode(), is(201));

        // validação de contrato do login
        auth.then().assertThat()
                .body(matchesJsonSchemaInClasspath("schemas/login-response.schema.json"));

        LoginResponse body = auth.as(LoginResponse.class);
        assertThat(body.token, not(isEmptyOrNullString()));
        assertThat(body.username, equalTo(username));
    }
}
