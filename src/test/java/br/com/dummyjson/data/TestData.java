package br.com.dummyjson.data;

import br.com.dummyjson.model.ProductRequest;

/** Massa de dados e valores esperados usados pelos testes. */
public final class TestData {

  public static final int EXISTING_PRODUCT_ID = 1;
  public static final int MISSING_PRODUCT_ID = 0;

  /** O DummyJSON tem 100 produtos fixos; os criados recebem ids a partir de 101. */
  public static final int FIRST_CREATED_PRODUCT_ID = 101;

  public static final String SEARCH_TERM = "phone";
  public static final String CATEGORY = "smartphones";

  public static final String WRONG_PASSWORD = "senha-incorreta";

  /** Token com cinco segmentos: inválido, mas no formato aceito pela validação da API. */
  public static final String INVALID_TOKEN = "this.is.not.a.valid.token";

  /** Token no formato JWT (três segmentos), mas com conteúdo que não decodifica. */
  public static final String MALFORMED_JWT = "x.y.z";

  public static final ProductRequest NEW_PRODUCT =
      new ProductRequest(
          "Perfume Oil",
          "Mega Discount, Impression of A...",
          13,
          8.4,
          4.26,
          65,
          "Impression of Acqua Di Gio",
          "fragrances",
          "https://i.dummyjson.com/data/products/11/thumnail.jpg");

  private TestData() {}
}
