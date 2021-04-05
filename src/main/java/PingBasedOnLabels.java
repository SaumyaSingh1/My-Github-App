import config.QuarkusBotConfig;
import config.QuarkusBotConfigFile;
import io.quarkiverse.githubapp.ConfigFile;
import io.quarkiverse.githubapp.event.Issue;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHLabel;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

public class PingBasedOnLabels {

   private static final Logger LOG = Logger.getLogger(PingBasedOnLabels.class);

   @Inject
   QuarkusBotConfig quarkusBotConfig;

   // ping when label is added
   void pingBasedOnLabel(@Issue.Labeled GHEventPayload.Issue issuePayload,
   @ConfigFile("quarkus-bot.yml") QuarkusBotConfigFile quarkusBotConfigFile) throws IOException {

      if (quarkusBotConfigFile == null) {
         LOG.error("Unable to find triage configuration.");
         return;
      }

      GHIssue issue = issuePayload.getIssue();

      // So you should find the persons to ping based on the labels.
      Set<String> allLabels = new TreeSet<>();

      for (GHLabel label : issue.getLabels()) {
         allLabels.add(label.toString());
      }
   }
}
