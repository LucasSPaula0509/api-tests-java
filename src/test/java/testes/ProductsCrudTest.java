package tests;

import base.BaseApiTest;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import models.ProductRequest;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static util.ProductClient.*;

public class ProductsCrudTest extends BaseApiTest {

    @Test
    @Description("GET /products retorna 200 e valida contrato mínimo")
    void listProducts_ok_schema() {
        Response r = listAll();
        assertThat(r.statusCode(), is(200));
        r.then().assertThat()
                .body(matchesJsonSchemaInClasspath("schemas/products-list.schema.json"));
        assertThat(r.jsonPath().getInt("total"), greaterThan(0));
    }

    @Test
    @Description("GET /products/{id} existente retorna 200 e contém campos essenciais")
    void getProductById_ok() {
        Response r = getById(1);
        assertThat(r.statusCode(), is(200));
        assertThat(r.jsonPath().getString("title"), not(isEmptyOrNullString()));
        assertThat(r.jsonPath().getString("category"), not(isEmptyOrNullString()));
        assertThat(r.jsonPath().getInt("id"), is(1));
    }

    @Test
    @Description("GET /products/{id} inexistente retorna 404")
    void getProductById_notFound() {
        Response r = getById(0);
        assertThat(r.statusCode(), is(404));
        assertThat(r.jsonPath().getString("message"), containsString("not found"));
    }

    @Test
    @Description("POST /products/add com payload válido retorna 201 e ecoa campos")
    void addProduct_created201() {
        ProductRequest req = new ProductRequest();
        req.title = "Perfume Oil";
        req.description = "Mega Discount, Impression of A...";
        req.price = 13;
        req.discountPercentage = 8.4;
        req.rating = 4.26;
        req.stock = 65;
        req.brand = "Impression of Acqua Di Gio";
        req.category = "fragrances";
        req.thumbnail = "https://i.dummyjson.com/data/products/11/thumnail.jpg";

        Response r = add(req);
        assertThat(r.statusCode(), is(201));
        assertThat(r.jsonPath().getString("title"), equalTo(req.title));
        assertThat(r.jsonPath().getInt("id"), greaterThanOrEqualTo(101));
    }
}
