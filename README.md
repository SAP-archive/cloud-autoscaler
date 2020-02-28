![](https://img.shields.io/badge/STATUS-NOT%20CURRENTLY%20MAINTAINED-red.svg?longCache=true&style=flat)

# Important Notice
We have decided to stop the maintenance of this public GitHub repository.

## HCP Automatic Application Scaler ##

This is an example application that demonstrates how elastic horizontal scaling can be achieved on the SAP HANA Cloud platform using the platform-provided APIs for [Lifecycle Management](https://api.hana.ondemand.com/lifecycle/v1/documentation) and [Monitoring](https://api.hana.ondemand.com/monitoring/v1/documentation).

It can be deployed as an HCP application, but can also be adapted to run elsewhere.

### Quick Start ###

First, clone the repo `git clone https://github.com/SAP/cloud-autoscaler`.
Use Maven to build it.
Deploy the WAR file on your HCP account.

> Please be mindful that the scaler uses two APIs provided by HCP, therefore two destinations should be created, named **hcprestapi** and **hcpmonitoringapi**. You can simply import the destinations we provide [here](https://github.com/SAP/cloud-autoscaler/blob/master/src/main/resources/destinations/hcprestapi.destination) and [here](https://github.com/SAP/cloud-autoscaler/blob/master/src/main/resources/destinations/hcpmonitoringapi.destination), just add your user name and password.


### Usage ###

After starting the application, point your browser to `https://<your app base url>/appscaler`.

In the UI form that shows up, enter the details of the application you wish to monitor (account and application name). 

Then press the **Get & Update Application Info** button. A JSON string should have appeared showing information about the application. If it didn't, most likely the entries are incorrect or your user doesn't have the rights to access that account.

Now there are three buttons, that are pretty much self-explanatory (**Start Monitoring**, **Start New Process** and **Stop Process**).

If you wish to quickly test the elastic scaler, you can do that by forcing it to scale down an application. Just start a new process of your application using the given button, and then press the **Start Monitoring** button. Supposing that your application has just one running instance and it doesn't consume too many resources (or at least not that many that it needs up-scaling), then it wouldn't meet the thresholds for keeping a second instance running. If that is the case, then once the second process is started, you will notice that the scaler will shut down one of the two automatically.

That's basically it!

### Copyright and license ###

Copyright (c) 2015 SAP SE

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

----------

This program references/bundles the following third party open source or other free download components. 
The third party licensors of these components may provide additional license rights, 
terms and conditions and/or require certain notices as described below. 

jQuery (http://jquery.com/)
Licensed under MIT - https://github.com/jquery/jquery/blob/master/MIT-LICENSE.txt

Twitter Bootstrap (http://twitter.github.com/bootstrap/)
Licensed under Apache License, Version 2.0 - http://www.apache.org/licenses/LICENSE-2.0

json.org (json.org)
Licensed under json.org License (http://www.json.org/license.html)

