package br.com.dummyjson.client;

import br.com.dummyjson.config.Env;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Especificação base de todas as requisições: URL, JSON e o filtro do Allure, que anexa a
 * requisição e a resposta de cada chamada ao relatório. Sem estado global do Rest-Assured.
 */
public final class Specs {

  private Specs() {}

  public static RequestSpecification json() {
    return new RequestSpecBuilder()
        .setBaseUri(Env.baseUrl())
        .setContentType(ContentType.JSON)
        .setAccept(ContentType.JSON)
        .addFilter(new AllureRestAssured())
        .build();
  }
}
