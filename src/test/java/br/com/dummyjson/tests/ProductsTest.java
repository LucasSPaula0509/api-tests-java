package br.com.dummyjson.tests;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

import br.com.dummyjson.client.ProductClient;
import br.com.dummyjson.data.Tags;
import br.com.dummyjson.data.TestData;
import br.com.dummyjson.model.ProductRequest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Epic("DummyJSON API")
@Feature("Produtos")
@DisplayName("Produtos (/products)")
class ProductsTest {

  @Test
  @Tag(Tags.SMOKE)
  @Tag(Tags.REGRESSION)
  @DisplayName("GET /products retorna 200, lista não vazia e respeita o contrato")
  void listProductsMatchesContract() {
    Response response = ProductClient.listAll();

    assertThat(response.statusCode()).as("Status de GET /products").isEqualTo(200);
    response.then().body(matchesJsonSchemaInClasspath("schemas/products-list.schema.json"));
    assertThat(response.jsonPath().getInt("total")).as("Total de produtos").isPositive();
  }

  @Test
  @Tag(Tags.REGRESSION)
  @DisplayName("Paginação com limit, skip e select retorna a página e os campos pedidos")
  void paginationReturnsRequestedPage() {
    Response response = ProductClient.page(5, 10, "title");

    assertThat(response.statusCode()).as("Status da paginação").isEqualTo(200);
    List<Map<String, Object>> products = response.jsonPath().getList("products");
    assertThat(products).as("Produtos da página").hasSize(5);
    assertThat(response.jsonPath().getInt("skip")).as("skip").isEqualTo(10);
    assertThat(response.jsonPath().getInt("products[0].id"))
        .as("Primeiro id da página")
        .isEqualTo(11);
    assertThat(products)
        .as("Campos retornados com select=title")
        .allSatisfy(product -> assertThat(product).containsOnlyKeys("id", "title"));
  }

  @Test
  @Tag(Tags.REGRESSION)
  @DisplayName("Busca por termo retorna resultados")
  void searchReturnsResults() {
    Response response = ProductClient.search(TestData.SEARCH_TERM);

    assertThat(response.statusCode()).as("Status da busca").isEqualTo(200);
    assertThat(response.jsonPath().getInt("total")).as("Total encontrado").isPositive();
    assertThat(response.jsonPath().getList("products")).as("Produtos encontrados").isNotEmpty();
  }

  @Test
  @Tag(Tags.REGRESSION)
  @DisplayName("Filtro por categoria retorna só produtos da categoria")
  void categoryFilterReturnsOnlyThatCategory() {
    Response response = ProductClient.byCategory(TestData.CATEGORY);

    assertThat(response.statusCode()).as("Status do filtro por categoria").isEqualTo(200);
    assertThat(response.jsonPath().getList("products.category", String.class))
        .as("Categorias retornadas")
        .isNotEmpty()
        .containsOnly(TestData.CATEGORY);
  }

  @Test
  @Tag(Tags.SMOKE)
  @Tag(Tags.REGRESSION)
  @DisplayName("GET /products/{id} existente retorna 200 com os campos essenciais")
  void getExistingProduct() {
    Response response = ProductClient.getById(TestData.EXISTING_PRODUCT_ID);

    assertThat(response.statusCode()).as("Status do produto existente").isEqualTo(200);
    assertThat(response.jsonPath().getInt("id")).as("id").isEqualTo(TestData.EXISTING_PRODUCT_ID);
    assertThat(response.jsonPath().getString("title")).as("title").isNotBlank();
    assertThat(response.jsonPath().getString("category")).as("category").isNotBlank();
  }

  @Test
  @Tag(Tags.REGRESSION)
  @DisplayName("GET /products/{id} inexistente retorna 404 not found")
  void getMissingProductReturnsNotFound() {
    Response response = ProductClient.getById(TestData.MISSING_PRODUCT_ID);

    assertThat(response.statusCode()).as("Status do produto inexistente").isEqualTo(404);
    assertThat(response.jsonPath().getString("message")).as("Mensagem").contains("not found");
  }

  @Test
  @Tag(Tags.SMOKE)
  @Tag(Tags.REGRESSION)
  @DisplayName("POST /products/add retorna 201 e ecoa os campos enviados")
  void addProductReturnsCreated() {
    ProductRequest product = TestData.NEW_PRODUCT;

    Response response = ProductClient.add(product);

    assertThat(response.statusCode()).as("Status de POST /products/add").isEqualTo(201);
    assertThat(response.jsonPath().getString("title")).as("title").isEqualTo(product.title());
    assertThat(response.jsonPath().getString("category"))
        .as("category")
        .isEqualTo(product.category());
    assertThat(response.jsonPath().getInt("id"))
        .as("id do produto criado")
        .isGreaterThanOrEqualTo(TestData.FIRST_CREATED_PRODUCT_ID);
  }

  @Test
  @Tag(Tags.REGRESSION)
  @DisplayName("PUT /products/{id} retorna o produto com o campo alterado")
  void updateProductReturnsChangedField() {
    String newTitle = "Título atualizado";

    Response response =
        ProductClient.update(TestData.EXISTING_PRODUCT_ID, Map.of("title", newTitle));

    assertThat(response.statusCode()).as("Status de PUT /products/{id}").isEqualTo(200);
    assertThat(response.jsonPath().getInt("id")).as("id").isEqualTo(TestData.EXISTING_PRODUCT_ID);
    assertThat(response.jsonPath().getString("title")).as("title").isEqualTo(newTitle);
  }

  @Test
  @Tag(Tags.REGRESSION)
  @DisplayName("DELETE /products/{id} retorna o produto marcado como excluído")
  void deleteProductMarksAsDeleted() {
    Response response = ProductClient.delete(TestData.EXISTING_PRODUCT_ID);

    assertThat(response.statusCode()).as("Status de DELETE /products/{id}").isEqualTo(200);
    assertThat(response.jsonPath().getBoolean("isDeleted")).as("isDeleted").isTrue();
    assertThat(response.jsonPath().getString("deletedOn")).as("deletedOn").isNotBlank();
  }
}
