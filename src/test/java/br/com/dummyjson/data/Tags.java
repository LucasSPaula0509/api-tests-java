package br.com.dummyjson.data;

/** Tags do JUnit usadas para filtrar a execução (ver README, seção Tags). */
public final class Tags {

  /** Caminho crítico; roda primeiro no CI. */
  public static final String SMOKE = "smoke";

  /** Faz parte da regressão. */
  public static final String REGRESSION = "regressivo";

  /** Divergência conhecida da documentação: fica fora da execução padrão. */
  public static final String KNOWN_DEFECT = "defeito";

  private Tags() {}
}
