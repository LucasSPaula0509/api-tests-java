package util;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.LoginRequest;

import static io.restassured.RestAssured.given;

public class AuthClient {

    public static Response login(LoginRequest body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/auth/login")
                .then()
                .extract().response();
    }

    public static Response getAuthProducts(String bearer) {
        return given()
                .header("Authorization", "Bearer " + bearer)
                .header("Content-Type", "application/json")
                .when()
                .get("/auth/products")
                .then()
                .extract().response();
    }
}

