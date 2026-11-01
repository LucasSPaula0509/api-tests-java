package br.com.dummyjson.config;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Configuração da execução. Precedência: propriedade de sistema ({@code -DbaseUrl=...}) → variável
 * de ambiente ({@code BASE_URL}) → valor padrão.
 */
public final class Env {

  private static final String DEFAULT_BASE_URL = "https://dummyjson.com";

  private Env() {}

  public static String baseUrl() {
    return find("baseUrl", "BASE_URL").orElse(DEFAULT_BASE_URL).replaceAll("/+$", "");
  }

  private static Optional<String> find(String property, String envVariable) {
    return Stream.of(System.getProperty(property), System.getenv(envVariable))
        .filter(value -> value != null && !value.isBlank())
        .map(String::trim)
        .findFirst();
  }
}
