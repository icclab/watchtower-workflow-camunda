package watchtower.workflow.camunda.delegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import watchtower.common.automation.Job;
import watchtower.common.event.Event;
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

    logger.info("Analysing {}", currentIncident);

    List<Incident> incidents =
        entityManager.createQuery("SELECT i FROM Incident i").getResultList();

    if (incidents != null) {
      for (Incident incident : incidents)
        if (incident.getStatus() == IncidentStatus.RESOLVED) {
          boolean similar = true;
          logger.info("Found resolved {} ", incident);
          for (Event event : currentIncident.getEvents()) {
            boolean temporarySimilar = false;

            for (Event otherEvent : incident.getEvents())
              if (event.isSimilarTo(otherEvent)) {
                temporarySimilar = true;
                break;
              }

            if (!temporarySimilar) {
              similar = false;
              break;
            }
          }

          if (similar) {
            logger.info("Found similar {}", incident);
            currentIncident.setPriority(incident.getPriority());
            currentIncident.setSeverity(incident.getSeverity());
            currentIncident.setImpact(incident.getImpact());

            List<Job> jobs = new ArrayList<Job>();

            for (Job job : incident.getJobs()) {
              logger.info("Adding job from similar incident {}", job);
              job.setExecution(null);
              jobs.add(job);
            }

            currentIncident.setJobs(jobs);

            entityManager.merge(currentIncident);

            break;
          }
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