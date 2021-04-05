import config.QuarkusBotConfig;
import config.QuarkusBotConfigFile;
import el.SimpleELContext;
import io.quarkiverse.githubapp.ConfigFile;
import io.quarkiverse.githubapp.event.Issue;


import org.jboss.logging.Logger;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHIssue;
import util.GHIssues;
import util.Labels;
import util.Patterns;
import util.Strings;

import javax.el.ELContext;
import javax.el.ELManager;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

class TriageIssue {

    private static final Logger LOG = Logger.getLogger(TriageIssue.class);

    @Inject
    QuarkusBotConfig quarkusBotConfig;

    void triageIssue(@Issue.Opened GHEventPayload.Issue issuePayload,
            @ConfigFile("quarkus-bot.yml") QuarkusBotConfigFile quarkusBotConfigFile) throws IOException {

        if (quarkusBotConfigFile == null) {
            LOG.error("Unable to find triage configuration.");
            return;
        }

        GHIssue issue = issuePayload.getIssue();
        boolean triaged = false;
        Set<String> labels = new TreeSet<>();
        Set<String> mentions = new TreeSet<>();

        for (QuarkusBotConfigFile.TriageRule rule : quarkusBotConfigFile.triage.rules) {
            if (matchRule(issue, rule)) {
                if (!rule.labels.isEmpty()) {
                    labels.addAll(rule.labels);
                }
                if (!rule.notify.isEmpty()) {
                    for (String mention : rule.notify) {
                        if (!mention.equals(issue.getUser().getLogin())) {
                            mentions.add(mention);
                        }
                    }
                }
                triaged = true;
            }
        }

        if (!labels.isEmpty()) {
            if (!quarkusBotConfig.isDryRun()) {
                issue.addLabels(labels.toArray(new String[0]));
            } else {
                LOG.info("Issue #" + issue.getNumber() + " - Add labels: " + String.join(", ", labels));
            }
        }

        if (!mentions.isEmpty()) {
            if (!quarkusBotConfig.isDryRun()) {
                issue.comment("/cc @" + String.join(", @", mentions));
            } else {
                LOG.info("Issue #" + issue.getNumber() + " - Mentions: " + String.join(", ", mentions));
            }
        }

        if (!triaged && !GHIssues.hasAreaLabel(issue)) {
            if (!quarkusBotConfig.isDryRun()) {
                issue.addLabels(Labels.TRIAGE_NEEDS_TRIAGE);
            } else {
                LOG.info("Issue #" + issue.getNumber() + " - Add label: " + Labels.TRIAGE_NEEDS_TRIAGE);
            }
        }
//
//        if(GHIssues.hasAreaLabel(issue) && !triaged){
//            if  (!quarkusBotConfig.isDryRun())  {
//                issue.comment("/cc @" + String.join(", @", mentions));
//            } else {
//                LOG.info("Issue #" + issue.getNumber() + " - Mentions: " + String.join(", ", mentions));
//            }
//        }

        if (labels.contains(Labels.TRIAGE_NEEDS_TRIAGE) && GHIssues.hasAreaLabel(issue)) {
            if (labels.remove(Labels.TRIAGE_NEEDS_TRIAGE)) {

                        issue.comment("Mention working");

            }
        }
    }

    private static boolean matchRule(GHIssue issue, QuarkusBotConfigFile.TriageRule rule) {
        try {
            if (Strings.isNotBlank(rule.title)) {
                if (Patterns.find(rule.title, issue.getTitle())) {
                    return true;
                }
            }
        } catch (Exception e) {
            LOG.error("Error evaluating regular expression: " + rule.title, e);
        }

        try {
            if (Strings.isNotBlank(rule.body)) {
                if (Patterns.find(rule.body, issue.getBody())) {
                    return true;
                }
            }
        } catch (Exception e) {
            LOG.error("Error evaluating regular expression: " + rule.body, e);
        }

        try {
            if (Strings.isNotBlank(rule.titleBody)) {
                if (Patterns.find(rule.titleBody, issue.getTitle()) || Patterns.find(rule.titleBody, issue.getBody())) {
                    return true;
                }
            }
        } catch (Exception e) {
            LOG.error("Error evaluating regular expression: " + rule.titleBody, e);
        }

        try {
            if (Strings.isNotBlank(rule.expression)) {
                String expression = "${" + rule.expression + "}";

                ExpressionFactory expressionFactory = ELManager.getExpressionFactory();

                ELContext context = new SimpleELContext(expressionFactory);
                context.getVariableMapper().setVariable("title",
                        expressionFactory.createValueExpression(issue.getTitle(), String.class));
                context.getVariableMapper().setVariable("body",
                        expressionFactory.createValueExpression(issue.getBody(), String.class));
                context.getVariableMapper().setVariable("titleBody",
                        expressionFactory.createValueExpression(issue.getTitle() + "\n\n" + issue.getBody(), String.class));

                ValueExpression valueExpression = expressionFactory.createValueExpression(context, expression, Boolean.class);

                Boolean value = (Boolean) valueExpression.getValue(context);
                if (Boolean.TRUE.equals(value)) {
                    return true;
                }
            }
        } catch (Exception e) {
            LOG.error("Error evaluating expression: " + rule.expression, e);
        }

        return false;
    }
}
