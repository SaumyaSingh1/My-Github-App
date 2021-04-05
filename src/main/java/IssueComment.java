import io.quarkiverse.githubapp.event.Issue;
import io.quarkiverse.githubapp.event.PullRequest;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHubBuilder;
import util.GHIssues;
import util.Labels;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class IssueComment {
   void onOpen(@Issue.Opened GHEventPayload.Issue issuePayload) throws IOException {
      issuePayload.getIssue().comment("Thank you for creating the issue");
   }

   void onAssign(@Issue.Assigned GHEventPayload.Issue issuePayload) throws IOException {
      String assignedUser= issuePayload.getIssue().getAssignee().getName();
      issuePayload.getIssue().comment("Issue successfully assigned to " + assignedUser);
   }

   void onOpenPR(@PullRequest.Opened GHEventPayload.PullRequest pullRequestPayload) throws IOException{
      String contributorName= pullRequestPayload.getPullRequest().getUser().getName();
      pullRequestPayload.getPullRequest().comment("Thanks " + contributorName + " for opening the PR. The team appreciate " +
              "your contribution!");
   }

   void onLabelRemoved(@Issue.Unlabeled GHEventPayload.Issue issuePayLoad) throws IOException {
      GHIssue issue= issuePayLoad.getIssue();


   }

   void onMergePR(@PullRequest.Closed GHEventPayload.PullRequest pullRequestPayLoad) throws IOException{
      GHPullRequest pullRequest= pullRequestPayLoad.getPullRequest();

      if(pullRequest.isMerged()){
         if(!pullRequest.getLabels().isEmpty()) {
            Collection<GHLabel> labels = pullRequestPayLoad.getPullRequest().getLabels();
            for (GHLabel label : labels) {
               if (label.getName().equals("triage/waiting-for-ci")) {
                  pullRequest.comment("TEST: Removed Label");
                  pullRequest.removeLabels("triage/waiting-for-ci");
               }
            }
         }
      }
   }

   void onClose(@Issue.Closed GHEventPayload.Issue issuePayload) throws IOException {

      Collection<GHLabel> labels = issuePayload.getIssue().getLabels();

//      for(GHLabel label : labels){
//         if(label.getName().equals("triage/needs-triage")){
//            issuePayload.getIssue().comment("Closed");
//            labels.remove("triage/needs-triage");
//         }
//      }
//      for (GHLabel label : labels) {
//         if(label.getName().equals("triage/needs-triage")){
//            issuePayload.getIssue().comment("Closed");
//            issuePayload.getIssue().removeLabels("triage/needs-triage");
//         }
//      }
//
//   }
   }
}
