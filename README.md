This application is able to build with Jenkins EBC servers.

[EBC Jenkins Pipeline](https://libh-proxy1.fyre.ibm.com/cognitive-dev/pipelineList.html?weeks=4&group=WAS-SVT)


# quicksec-jakarta
This is the version of QuickSec which is converted to Jakarta EE using the transformation tool as per [this TER](https://github.ibm.com/websphere/system-test/issues/403). This application is being complied and run with jdk 8 as there are still issues comlping java client with java 11. 

EE10 version is updated to run with Java 18 as well.

Java EE version of this application is at [quicksec-jee](https://github.ibm.com/was-svt/quicksec-jee)
 
 * Prerequisite: 
   Server.xml does not have LDAP configuration but a sepate [ldap-config.xml](https://github.ibm.com/was-svt/quicksec-jakarta/blob/main/config/ldap-config.xml) file is used for LDAP conffiguration. 

1. This project uses travis to build the application ear file as well create a container image. Container images are pushed to artifactory at docker-na-public.artifactory.swg-devops.com/hyc-wassvt-team-image-registry-docker-local/nest-websphere-liberty/quicksec:`<tag>`. All the images for QuickSec can be found by logging into artifactory: https://na.artifactory.swg-devops.com/ui/repos/tree/General/hyc-wassvt-team-image-registry-docker-local
3. Application builds different images based on 
   *  Open Liberty latest GA kernel image in icr (cr.io/appcafe/open-liberty:kernel-slim-java8-openj9-ubi): tags are `latest`
   *  Open Liberty daily full image at docker hub (openliberty/daily:full-java8-openj9): tags is `ol-jdk8-java8` 'ol-jdk8-java11' 'ol-jdk8-java17`
   *  WebSphere Liberty full ga image (icr.io/appcafe/websphere-liberty:full-java8-openj9-ubi): tag is `wl-jdk8-java17`
   *  WebSphere Liberty build for datagrid tag is: `wl-jdk8-java17-nest-datagrid`
   *  You can create a custom build by providing base image in Travis and providing your own tag. Default travis tag is `travis_java17`
   * Currently there is only one application container image for `quicksec-jakarta10:ol-jdk8-java17` 
   *  It also use the git commit SHA value to roll back to previous images which match to a specific commit of git. Make sure not to have too many of these images and delete some time to time. - This is not done anymore to save artifactory space and we did not see need to go back to old images.
4. SVT utility applications are added int the Containerfile by downloading the EE9 versions and copying to the docker image:

```
    https://github.ibm.com/was-svt/svtMessageApp/releases
    https://github.ibm.com/was-svt/microwebapp/releases
    https://github.ibm.com/was-lumberjack/badapp/releases
```
 4. DB2 repo has correct files to configure SVT DB2 contianer for QuickSec at https://github.ibm.com/was-svt/db2Container
 
 5. Yaml files to deploy DB2, QuickSec application and Jmeter can be applied directly or using argoCD
 
 6. Quicksec application can be deployed to OCP cluster using app-deploy.yaml file. This file needs to be updated when using non-default ( default is to use `latest` tag) applicaiton image from artifactory. You need to make sure that `open liberty/WebSphere Liberty operator` is installed on the cluster first manually or using argoCD
  
 7. Use JMETER yaml files to deploy them to OCP cluster. Update jmeter yaml file to adjust script, threads and time of the jmeter run. Jmeter image has most of the application scripts already added to the jmeter image: https://github.ibm.com/was-svt/jmeterStressContainer

URLs: As we want to use different context roots for the application including utility applications, app-deploy.yaml is not using path to add context root to the route. 

**QuickSec Application**

https://quicksec-jakarta-quicksec.apps.mst10.cp.fyre.ibm.com/QuickSecForm/

Login with persona1/ppersona1
Click on Go/Get to access DB2. If the DB2 is not setup correctly to work with application there will be errors on this page.

https://quicksec-jakarta-quicksec.apps.mst10.cp.fyre.ibm.com/QuickSecBaisc/

**Utility apps**

   https://quicksec-jakarta-qs.apps.mst10.cp.fyre.ibm.com/svtMessageApp/printMessage?message=mstmsg

   https://quicksec-jakarta-qs.apps.mst10.cp.fyre.ibm.com/badapp/Angry

   https://quicksec-jakarta-qs.apps.mst10.cp.fyre.ibm.com/microwebapp/


---------

NOTE: This repo does not utilizes submodule anymore. Below commands are kept for reference in future if submodule is used.

```
git clone git@github.ibm.com:was-svt/quicksec-jakarta.git
cd quicksec-jakarta
git submodule update --init --recursive
```
test
