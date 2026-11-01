package br.com.dummyjson.allure;

import br.com.dummyjson.data.Tags;
import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.Link;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.TestResult;
import java.util.Objects;

/**
 * Ajusta cada resultado logo antes de o Allure gravá-lo (registrado em {@code
 * META-INF/services/io.qameta.allure.listener.TestLifecycleListener}).
 *
 * <p>Testes com a tag {@code defeito} que falham ganham o prefixo {@code [Defeito conhecido
 * DEF-xxx]} na mensagem, usado pela categoria "Defeitos conhecidos" do {@code categories.json}. O
 * ID vem do {@code @Issue} do teste.
 */
public class AllureResultEnricher implements TestLifecycleListener {

  private static final String TAG_LABEL = "tag";
  private static final String ISSUE_LINK = "issue";

  @Override
  public void beforeTestWrite(TestResult result) {
    boolean knownDefect =
        result.getLabels().stream()
            .anyMatch(
                label ->
                    TAG_LABEL.equals(label.getName())
                        && Tags.KNOWN_DEFECT.equals(label.getValue()));
    StatusDetails details = result.getStatusDetails();
    if (!knownDefect
        || details == null
        || !(result.getStatus() == Status.FAILED || result.getStatus() == Status.BROKEN)) {
      return;
    }
    String defectId =
        result.getLinks().stream()
            .filter(link -> ISSUE_LINK.equals(link.getType()))
            .map(Link::getName)
            .findFirst()
            .orElse("sem ID");
    details.setKnown(true);
    details.setMessage(
        "[Defeito conhecido %s] %s"
            .formatted(defectId, Objects.toString(details.getMessage(), "")));
  }
}
