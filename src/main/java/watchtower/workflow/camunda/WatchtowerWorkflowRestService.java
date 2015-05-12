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
package watchtower.workflow.camunda;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import watchtower.common.automation.JobExecution;
import watchtower.common.event.Event;
import watchtower.common.incident.Incident;
import watchtower.common.incident.IncidentStatus;
import watchtower.common.incident.IncidentUtils;

@Path("/v1.0/workflow")
public class WatchtowerWorkflowRestService {
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createWorkflowInstance(@Context UriInfo uriInfo, Event event) {
    ProcessEngineService processEngineService =
        RuntimeContainerDelegate.INSTANCE.get().getProcessEngineService();

    ProcessEngine defaultProcessEngine = processEngineService.getDefaultProcessEngine();

    RuntimeService runtimeService = defaultProcessEngine.getRuntimeService();

    List<Event> events = new ArrayList<Event>();
    events.add(event);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("events", events);

    ProcessInstance processInstance =
        runtimeService.startProcessInstanceByKey("WatchtowerWorkflow", variables);

    Incident incident =
        new Incident(processInstance.getId(), events.get(0).getMessage(), IncidentStatus.NEW, null,
            null, null, events, null, new Date(), new Date(), 0);

    return Response.status(Status.OK).entity(IncidentUtils.toJson(incident)).build();
  }

  @POST
  @Path("/{workflowInstanceId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response attachJobExecutionToWorkflowInstance(@Context UriInfo uriInfo,
      @PathParam("workflowInstanceId") String workflowInstanceId, JobExecution execution) {
    ProcessEngineService processEngineService =
        RuntimeContainerDelegate.INSTANCE.get().getProcessEngineService();

    ProcessEngine defaultProcessEngine = processEngineService.getDefaultProcessEngine();

    RuntimeService runtimeService = defaultProcessEngine.getRuntimeService();

    runtimeService.signal(workflowInstanceId, null, execution, null);

    return Response.status(Status.OK).build();
  }
}