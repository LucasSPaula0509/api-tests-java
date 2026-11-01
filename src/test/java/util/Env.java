package util;

public class Env {
  public static String baseUrl() {
    // Prioridade: -DbaseUrl=..., depois env BASE_URL, depois default
    String sys = System.getProperty("baseUrl");
    if (sys != null && !sys.isBlank()) return sys;
    String env = System.getenv("BASE_URL");
    if (env != null && !env.isBlank()) return env;
    return "https://dummyjson.com";
  }
}
