# cf-kie-server
Red Hat Business Automation Execution Server on Cloud Foundry


## Usage

On Cloud Foundry:

1. Install business-central, if you want to manage your execution cluster. To quickly deploy on a VM or OpenShift use the [RHPAM7 Quick Install Demo](https://github.com/jbossdemocentral/rhpam7-install-demo)
2. Install cloud foundry client and a local pcfdev or remote service (see: [PCF Dev Local Environment Setup](https://pivotal.io/platform/pcf-tutorials/getting-started-with-pivotal-cloud-foundry-dev/introduction))
3. Configure `src/main/resources/application.properties`
   * Update the `kieserver.controllers` to point to the business-central management/controller/repo you deployed in 1, if you want to manage your execution cluster.
   * Set `cfkieserver.remoterepo.enabled` to true and update the URL, user, and pass for the business-central management/controller/repo you deployed in 1.
   * Update the user/pass settings as necessary for the system component
4. Start the process with `./deploy-cf.sh`

As a standalone SpringBoot process:

1. Install business-central, if you want to manage your execution cluster. To quickly deploy on a VM or OpenShift use the [RHPAM7 Quick Install Demo](https://github.com/jbossdemocentral/rhpam7-install-demo)
2. Install a local database service. For example, postgres (default), mysql, or other JDBC RDBMS
3. Configure `src/main/resources/application.pgsql.properties`
   * Set `server.address` to your external or bridge IP address
   * Set your database settings under `spring.datasource.*`
   * If you want to manage your execution cluster, set `cfkieserver.remoterepo.enabled` to true and update the URL, user, and pass for the business-central management/controller/repo you deployed in 1.
   * Update the user/pass settings as necessary for the system component
4. Start the process with `./deploy-spring-boot.sh`, which just uses `mvn spring-boot:run` with the appropriate properties file

## Architecture

### Topology Components

* kie-server: execution service for rules, process, case, event processing, and planning engine. This process can run as a service with execution package deployed to it, or run as an immutable image. (cannot be embedded in CloudFoundry)
* sso provider: kie-server uses java standard interfaces allowing authentication/authorization providers to be integrated.
* controller: service embedded in business-central for deploying and managing clusters of kie-servers. (cannot be embedded in CloudFoundry).
* business-central: management, activity monitoring, and/or authoring. (cannot be embedded in CloudFoundry).
* jbpm database: data source for jbpm process/case current state, process/case audit logs, and supplemental user information
* supplemental reporting datasources

### Topologies

You have several deployment topology options:

1. On cloud foundry managed by a remote business-central and controller service (`./deploy-spring-boot.sh`)
1. As a plain spring-boot app managed by a remote business-central and controller service (`./deploy-cf.sh`)
1. On cloud foundry as an immutable image
1. As an immutable spring-boot app

### Transaction Management

The kie-server embedded in SpringBoot uses the Narayana transaction manager. Narayana by default journal transaction status to the filesystem in case recovery is necessary. Cloud Foundry 1.12 allows for persistent volumes to be mounted by applications, which is required to use this by default. In addition the `spring.jta.narayana.transaction-manager-id` property needs to unique for reach instance of the cf-kie-server.

As an alternative, the following is currently being investigated. Narayana allows the XID journal to be logged to a jdbc database. Currently it doesn't allow for distributed recovery. Cloud Foundry enforces that there is always an application instance with instance index `0` even after node/container failure or after a scale down. So it may be possible to use the `cloud.application.instance_index` property to create an HA singleton instance that is responsible for recovering transactions exclusively.

## CF & KIE Knowledge Base

###### Viewing App Properties

View the full set of process properties from the system, app, and cloud level using [Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html). Go to the `http://cf-kie-server-<yourapp>.local.pcfdev.io/env` address, and log in with username `admin`, and password `asdfasdf`. As you have changed the security settings this may vary, the user must have the `ACTUATOR` role to view the environment variables.

###### Connecting to the CF MySQL Instance

For a local pcfdev development environment you can run mysql behind applications. Here is how to connect to the local mysql database to query/manipulate from your local machine. The following should also work for remote environments as well:

1. Install a local mysql client `brew install mysql-client`, or 'sudo yum install mysql-client`
1. Install the CF mysql plugin `cf install-plugin -r "CF-Community" mysql-plugin`
1. List the mysql database service names `cf services`
1. Connect to the kie-server mysql database `cf mysql cf-kie-server-db` (adjusting the name as applicable)

More:
* [Using MySql for PCF](https://docs.pivotal.io/p-mysql/2-1/use.html)
* [cf-mysql-plugin](https://github.com/andreasf/cf-mysql-plugin)

###### Creating a Data Source in Business-Central

1. Go to Menu > Design > Pages
1. Select the setting tab (icon with three horizontal lines)
1. Expand the navigation tree; click the gear icon on 'Design' node; click '+ New Page'
1. Select 'Data Sources' from the drop down
1. Click the check box
1. Got to Menu > Design > Data Sources
1. Click '+ Add Datasource'
1. Get your URI, username, and password from the `cloud.services.cf-kie-server-db.connection.jdbcurl` property using the `\env` actuator page.
1. Click 'Test Connection' to verify
1. Save/Update the connection

###### Monitoring JVM in Cloud Foundry

1. Start process with `./deploy-cf.sh` (The `manifest.yml > JBP_CONFIG_JMX: '{enabled: true}'` setting, plus the `cf ssh -N -T -L 5000:localhost:5000 cf-kie-server` command in the deploy script enables jmx connections)
1. Use `jconsole` and connect to `localhost:5000`
1. Use `jvisualvm` and connect to `localhost:5000`

More:
* [CloudFoundry: Enabling Java JMX/RMI access for remote containers](https://fabianlee.org/2017/12/09/cloudfoundry-enabling-java-jmx-rmi-access-for-remote-containers/)
* [CloudFoundry: Java thread and heap dump analysis on remote containers](https://fabianlee.org/2017/12/08/cloudfoundry-java-thread-and-heap-dump-analysis-on-remote-containers/)
* [Download JVisualVM](https://visualvm.github.io/download.html)
* Java Home is OSX is `/System/Library/Frameworks/JavaVM.framework/Versions/Current/Commands/`

## Additional Links

### Red Hat Process Automation
* [KIE Server jBPM Spring Boot Starter](https://github.com/kiegroup/droolsjbpm-integration/tree/master/kie-spring-boot/kie-spring-boot-starters/kie-server-spring-boot-starter-jbpm)
* [Spring Boot starters for jBPM and KIE Server](http://mswiderski.blogspot.com/2018/01/spring-boot-starters-for-jbpm-and-kie.html)
* [Quick Install Demo](https://github.com/jbossdemocentral/rhpam7-install-demo)

### Cloud Foundry
* [PCF Dev Local Environment Setup](https://pivotal.io/platform/pcf-tutorials/getting-started-with-pivotal-cloud-foundry-dev/introduction)
* [PCF Performance Tuning](http://engineering.pivotal.io/post/profiling_cpu_on_pcf/)

### Property Reference
* [KIE Server Property Names and Constants](https://github.com/kiegroup/droolsjbpm-integration/blob/5788f5fc0a151dc1b2c005172c1dd3007de12994/kie-server-parent/kie-server-api/src/main/java/org/kie/server/api/KieServerConstants.java)
* [SpringBoot Common Application Properties Reference](https://docs.spring.io/spring-boot/docs/1.5.9.RELEASE/reference/html/common-application-properties.html)
* [Cloud Foundry Environment Variables Reference](https://docs.run.pivotal.io/devguide/deploy-apps/environment-variable.html)
