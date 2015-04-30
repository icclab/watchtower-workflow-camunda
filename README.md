*Please note that all watchtower components are under heavy development and the norm is that things will break. Please be patient with us until the first stable release.*

<div align="center">
	<img src="https://raw.githubusercontent.com/icclab/watchtower-common/master/watchtower.png" alt="Watchtower" title="Watchtower">
</div>

# Overview

**watchtower-workflow-camunda** is a component of **watchtower** which is deployed onto Camunda and contains both the BPM workflow and the REST service to which **watchtower-workflow** connects.

## Workflow

**watchtower-workflow-camunda** is built around the following BPM workflow:

![Cloud Incident Management Workflow] (https://raw.githubusercontent.com/icclab/watchtower-workflow-camunda/master/src/main/resources/CloudIncidentManagement.png)

## General Instructions

### Building

The best way to install **watchtower-workflow-camunda** is to download and build it with Maven. Please note it depends on **watchtower-common** which needs to be installed prior to it.

```
git clone https://github.com/icclab/watchtower-workflow-camunda.git
cd watchtower-workflow-camunda
mvn clean install
```

### Installation

The build process will output a `war` file which you can then deploy onto Tomcat or any other application environment in which Camunda is already deployed.

### Configuration

Configuration can be changed only prior to the build process. One can change persistence settings by editing `src/main/resources/META-INF/persistence.xml`.

### Running

Once deployed you can login to Camunda and the `Cloud Incident Management Workflow` should be visible in the Cockpit.

# License

Copyright 2015 Zurich University of Applied Sciences

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied.
See the License for the specific language governing permissions and
limitations under the License.

# Author Information

For further information or assistance please contact [**Victor Ion Munteanu**](https://github.com/nemros).