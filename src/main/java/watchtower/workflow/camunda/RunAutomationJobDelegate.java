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

import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import watchtower.common.automation.Job;
import watchtower.common.automation.JobUtils;

public class RunAutomationJobDelegate implements JavaDelegate {
  private static final Logger logger = LoggerFactory.getLogger(RunAutomationJobDelegate.class);

  public void execute(DelegateExecution delegationExecution) throws Exception {
    Job job = new Job("5d180993-ec16-4318-8fdf-98d884b53099", null, null);

    Client client = Client.create();
    
    WebResource webResource = client.resource("http://watchtower:9010/v1.0/jobs");

    ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, JobUtils.toJson(job));
    
    if (response.getStatus() != 200) {
      logger.error("Failed to send job to watchtower-workflow with HTTP error code: " + response.getStatus());
    }
  }
}