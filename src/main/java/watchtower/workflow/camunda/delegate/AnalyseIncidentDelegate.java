package watchtower.workflow.camunda.delegate;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import watchtower.common.incident.Incident;
import watchtower.common.incident.IncidentStatus;

public class AnalyseIncidentDelegate implements JavaDelegate {
  private static final Logger logger = LoggerFactory.getLogger(AnalyseIncidentDelegate.class);

  public void execute(DelegateExecution delegateExecution) throws Exception {
    EntityManagerFactory entityManagerFactory =
        Persistence.createEntityManagerFactory("CloudIncidentManagement");
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();

    Map<String, Object> variables = delegateExecution.getVariables();

    Incident currentIncident =
        entityManager.find(Incident.class, (String) variables.get("incidentId"));

    List<Incident> incidents =
        entityManager.createQuery("SELECT i FROM Incident i").getResultList();

    if (incidents != null) {
      for (Incident incident : incidents)
        if (incident.getEvents().containsAll(currentIncident.getEvents()))
          if (incident.getStatus() == IncidentStatus.RESOLVED) {
            currentIncident.setPriority(incident.getPriority());
            currentIncident.setSeverity(incident.getSeverity());
            currentIncident.setImpact(incident.getImpact());
          }
    }

    if (currentIncident.getJobs().size() > 0)
      delegateExecution.setVariable("hasJobs", true);
    else
      delegateExecution.setVariable("hasJobs", false);

    entityManager.getTransaction().commit();
    entityManager.close();
  }
}