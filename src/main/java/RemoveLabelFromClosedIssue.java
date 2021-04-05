import config.QuarkusBotConfig;
import config.QuarkusBotConfigFile;
import io.quarkiverse.githubapp.ConfigFile;
import io.quarkiverse.githubapp.event.Issue;

import org.jboss.logging.Logger;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHLabel;
import util.Labels;

import javax.inject.Inject;
import java.io.IOException;

public class RemoveLabelFromClosedIssue {

   private static final Logger LOG = Logger.getLogger(RemoveLabelFromClosedIssue.class);

   @Inject
   QuarkusBotConfig quarkusBotConfig;

   void onClose(@Issue.Closed GHEventPayload.Issue issuePayload,
                    @ConfigFile("quarkus-bot.yml") QuarkusBotConfigFile quarkusBotConfigFile) throws IOException {

      if (quarkusBotConfigFile == null) {
         LOG.error("Unable to find triage configuration.");
         return;
      }

      GHIssue issue = issuePayload.getIssue();
      for (GHLabel label : issue.getLabels()) {
         if (label.getName().equals(Labels.TRIAGE_NEEDS_TRIAGE)){
            if (!quarkusBotConfig.isDryRun()) {
               issue.comment("TEST CLOSE");
               issue.removeLabels(Labels.TRIAGE_NEEDS_TRIAGE);
            } else {
               LOG.info("Issue #" + issue.getNumber() + " - Remove label: " + String.join(", ", label.getName()));
            }
         }
      }
   }
}
