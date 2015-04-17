package watchtower.workflow.camunda.delegate;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import watchtower.common.event.Event;
import watchtower.common.incident.Incident;
import watchtower.common.incident.IncidentStatus;

public class PersistIncidentDelegate implements JavaDelegate {

  public void execute(DelegateExecution delegateExecution) throws Exception {
    EntityManagerFactory entityManagerFactory =
        Persistence.createEntityManagerFactory("CloudIncidentManagement");
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();

    Map<String, Object> variables = delegateExecution.getVariables();

    Incident incident = new Incident();
    incident.setId(delegateExecution.getProcessInstanceId());
    incident.setSummary(((List<Event>) variables.get("events")).get(0).getMessage());
    incident.setStatus(IncidentStatus.NEW);
    incident.setDateCreated(new Date());
    incident.setDateLastUpdated(new Date());

    entityManager.persist(incident);
    entityManager.flush();

    List<Event> events = (List<Event>) variables.get("events");

    if (events != null)
      for (Event event : events) {
        event.setIncidentId(incident.getId());
      }

    incident.setEvents(events);

    entityManager.merge(incident);
    entityManager.flush();

    delegateExecution.removeVariables(variables.keySet());

    delegateExecution.setVariable("incidentId", incident.getId());

    entityManager.getTransaction().commit();
    entityManager.close();
  }
}