package watchtower.workflow.camunda.listener;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class SendEmailListener implements TaskListener {

  public void notify(DelegateTask delegateTask) {
    // Add code to send email to asignee
  }
}