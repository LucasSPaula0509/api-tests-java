package util;

import static io.restassured.RestAssured.given;

import io.restassured.response.Response;

public class UserClient {

  public static Response getUsers() {
    return given().when().get("/users").then().extract().response();
  }
}
