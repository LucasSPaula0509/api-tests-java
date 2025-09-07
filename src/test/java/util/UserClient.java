package util;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class UserClient {

    public static Response getUsers() {
        return given()
                .when()
                .get("/users")
                .then()
                .extract().response();
    }
}
