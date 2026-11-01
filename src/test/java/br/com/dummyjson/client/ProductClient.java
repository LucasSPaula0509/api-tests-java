package br.com.dummyjson.client;

import static io.restassured.RestAssured.given;

import br.com.dummyjson.model.ProductRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.Map;

/** Endpoints públicos de produtos ({@code /products/*}). */
public final class ProductClient {

  private ProductClient() {}

  @Step("GET /products")
  public static Response listAll() {
    return given().spec(Specs.json()).get("/products");
  }

  @Step("GET /products?limit={limit}&skip={skip}&select={select}")
  public static Response page(int limit, int skip, String select) {
    return given()
        .spec(Specs.json())
        .queryParams(Map.of("limit", limit, "skip", skip, "select", select))
        .get("/products");
  }

  @Step("GET /products/search?q={query}")
  public static Response search(String query) {
    return given().spec(Specs.json()).queryParam("q", query).get("/products/search");
  }

  @Step("GET /products/category/{category}")
  public static Response byCategory(String category) {
    return given().spec(Specs.json()).get("/products/category/{category}", category);
  }

  @Step("GET /products/{id}")
  public static Response getById(int id) {
    return given().spec(Specs.json()).get("/products/{id}", id);
  }

  @Step("POST /products/add")
  public static Response add(ProductRequest product) {
    return given().spec(Specs.json()).body(product).post("/products/add");
  }

  @Step("PUT /products/{id}")
  public static Response update(int id, Map<String, Object> changes) {
    return given().spec(Specs.json()).body(changes).put("/products/{id}", id);
  }

  @Step("DELETE /products/{id}")
  public static Response delete(int id) {
    return given().spec(Specs.json()).delete("/products/{id}", id);
  }
}
