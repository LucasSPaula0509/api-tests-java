package br.com.dummyjson.allure;

import br.com.dummyjson.config.Env;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

/**
 * Grava o {@code environment.properties} do Allure uma vez por execução (registrado em {@code
 * META-INF/services/org.junit.platform.launcher.LauncherSessionListener}).
 */
public class AllureEnvironment implements LauncherSessionListener {

  @Override
  public void launcherSessionOpened(LauncherSession session) {
    Path dir = Path.of(System.getProperty("allure.results.directory", "target/allure-results"));
    String content =
        """
        Base.URL=%s
        Java=%s
        OS=%s
        """
            .formatted(
                Env.baseUrl(), System.getProperty("java.version"), System.getProperty("os.name"));
    try {
      Files.createDirectories(dir);
      Files.writeString(dir.resolve("environment.properties"), content, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Falha ao gravar environment.properties do Allure", e);
    }
  }
}
