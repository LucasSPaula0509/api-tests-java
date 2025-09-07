package util;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.ProductRequest;

import static io.restassured.RestAssured.given;

public class ProductClient {

    public static Response listAll() {
        return given().when().get("/products").then().extract().response();
    }

    public static Response getById(int id) {
        return given().when().get("/products/" + id).then().extract().response();
    }

    public static Response add(ProductRequest body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/products/add")
                .then()
                .extract().response();
    }
}
