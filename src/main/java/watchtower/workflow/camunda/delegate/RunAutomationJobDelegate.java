/*
 * Copyright 2015 Zurich University of Applied Sciences
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package watchtower.workflow.camunda.delegate;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import watchtower.common.automation.Job;
import watchtower.common.automation.JobExecution;
import watchtower.common.automation.JobUtils;
import watchtower.common.incident.Incident;
import watchtower.workflow.camunda.configuration.WatchtowerWorkflowCamundaConfiguration;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RunAutomationJobDelegate extends AbstractBpmnActivityBehavior {
  private static final Logger logger = LoggerFactory.getLogger(RunAutomationJobDelegate.class);

  public void execute(final ActivityExecution execution) throws Exception {
    EntityManagerFactory entityManagerFactory =
        Persistence.createEntityManagerFactory("Watchtower");
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();

    Map<String, Object> variables = execution.getVariables();

    Incident currentIncident =
        entityManager.find(Incident.class, (String) variables.get("incidentId"));

    logger.info("Running automation jobs for {}", currentIncident);

    String endpoint = WatchtowerWorkflowCamundaConfiguration.getInstance().getEndpoint();

    for (Job job : currentIncident.getJobs()) {
      Client client = Client.create();


      WebResource webResource = client.resource(endpoint);

      ClientResponse response =
          webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
              .post(ClientResponse.class, JobUtils.toJson(job));

      if (response.getStatus() != 200) {
        logger.error("Failed to send job to watchtower-workflow with HTTP error code: "
            + response.getStatus());
      }
    }

    entityManager.getTransaction().commit();
    entityManager.close();
  }

  @Override
  public void signal(ActivityExecution execution, String signalName, Object signalData)
      throws Exception {
    EntityManagerFactory entityManagerFactory =
        Persistence.createEntityManagerFactory("Watchtower");
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();

    if (!(signalData instanceof JobExecution)) {
      logger.error("Got something which is not a job execution {}", signalData);

      return;
    }

    JobExecution jobExecution = (JobExecution) signalData;

    Job currentJob = entityManager.find(Job.class, jobExecution.getJobId());

    currentJob.addExecution(jobExecution);

    entityManager.merge(currentJob);

    entityManager.getTransaction().commit();
    entityManager.close();

    leave(execution);
  }
}