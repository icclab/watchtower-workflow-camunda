package watchtower.workflow.camunda.listener;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import watchtower.common.incident.Incident;
import watchtower.common.incident.IncidentStatus;
import watchtower.common.incident.IncidentUtils;

public class SaveIncidentListener implements TaskListener {

  public void notify(DelegateTask delegateTask) {
    EntityManagerFactory entityManagerFactory =
        Persistence.createEntityManagerFactory("CloudIncidentManagement");
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();

    Map<String, Object> variables = delegateTask.getVariables();

    Incident incident = entityManager.find(Incident.class, (String) variables.get("incidentId"));

    Incident incidentUpdated = IncidentUtils.fromJson((String) variables.get("incident"));

    incident.setJobs(incidentUpdated.getJobs());
    incident.setStatus(IncidentStatus.RESOLVED);

    entityManager.merge(incident);
    entityManager.flush();

    entityManager.getTransaction().commit();
    entityManager.close();

    delegateTask.removeVariables(variables.keySet());

    delegateTask.setVariable("incidentId", incident.getId());
  }
}