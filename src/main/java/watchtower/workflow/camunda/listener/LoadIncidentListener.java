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

public class LoadIncidentListener implements TaskListener {

  public void notify(DelegateTask delegateTask) {
    EntityManagerFactory entityManagerFactory =
        Persistence.createEntityManagerFactory("CloudIncidentManagement");
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();

    Map<String, Object> variables = delegateTask.getVariables();

    Incident incident = entityManager.find(Incident.class, (String) variables.get("incidentId"));

    incident.setStatus(IncidentStatus.WORK_IN_PROGRESS);

    entityManager.merge(incident);

    delegateTask.setVariable("incident", IncidentUtils.toJson(incident));

    entityManager.getTransaction().commit();
    entityManager.close();
  }
}