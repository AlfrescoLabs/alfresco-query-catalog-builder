# Alfresco Search Query Catalog Builder

Before upgrading to [Alfresco Search Enterprise](https://docs.alfresco.com/search-enterprise/latest/), verifying that every different search string used in the current ACS environment support is recommended. This tool provides a simple framework to build a Search Query Catalog that may be used as reference for this task.

The project includes following elements:

* [ACS Sample Docker Compose Deployment](docker), includes a pre-configured Audit application for searching actions
* [ACS REST API Client](alfresco-rest-client), includes a Spring Boot app to process the entries from ACS Audit application

## Docker Compose

Alfresco Docker Image has been customized to enable Search Queries [auditing](https://docs.alfresco.com/content-services/community/admin/audit/) by adding following configuration to `docker-compose.yml` file:

```
alfresco:
    environment:
        JAVA_OPTS : '
            -Daudit.enabled=true
            -Daudit.tagging.enabled=false
        '
    volumes:
        - ./alfresco/config/SearchAudit.xml:/usr/local/tomcat/shared/classes/alfresco/extension/audit/SearchAudit.xml
```

The audit configuration is based in [SearchService.java](https://github.com/Alfresco/alfresco-community-repo/blob/master/data-model/src/main/java/org/alfresco/service/cmr/search/SearchService.java) class, including audit entries for the following paths:

```
/alfresco-api/post/SearchService/query/args
/alfresco-api/post/SearchService/selectNodes/args
```

## REST API Client

The REST API Client is using the [audit entity](https://docs.alfresco.com/content-services/latest/develop/rest-api-guide/audit-apps/) from the Core API. Audit Entries are processed in batches, adding every search query to the Search Query Catalog only according to similarity criteria.

Configuration can be made in `src/main/resources/application.properties` file:

```
# Alfresco Server
content.service.url=http://localhost:8080
content.service.security.basicAuth.username=admin
content.service.security.basicAuth.password=admin
content.service.path=/alfresco/api/-default-/public/alfresco/versions/1
search.service.path=/alfresco/api/-default-/public/search/versions/1

# Audit parameters
audit.app.id=search
audit.api.max.items=100
audit.query.similarity.threshold=0.95
```

## Using the framework

Start Docker Compose to record Search Query entries into the Audit Module.

```
$ cd docker
$ docker compose up
```

Once ACS is started, you may navigate the Share Web Application or the ACA / ADW applications. Additionally, you can run all the integrations or scheduled jobs in order to have a representative set of common actions in your deployment.

Audit information will be stored in `alf_audit_*` database tables and audit entry details can be retrieved from REST API. For instance, to get the information for the entry **1** of the **search** audit app, following HTTP GET request may be used:

http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/audit-applications/search/audit-entries/1

```
{
  "entry": {
    "createdAt": "2022-10-24T09:57:15.402+0000",
    "createdByUser": {
      "id": "admin",
      "displayName": "Administrator"
    },
    "values": {
      "/search/SELECT/contextNodeRef/value": "5b9a8bdb-c967-4fba-ad99-db6e4fa858de",
      "/search/SELECT/parameters/value": null,
      "/search/SELECT/xpath/value": "./app:company_home/st:sites",
      "/search/SELECT/followAllParentLinks/value": false,
      "/search/SELECT/language/value": "xpath"
    },
    "auditApplicationId": "search",
    "id": 1
  }
}
```

Once all the audit information has been populated, compile the Alfresco Rest Client application to get the Search Query Catalog. Before building and running the project, verify that `src/main/resources/application.properties` file includes the right settings.

```
$ cd alfresco-rest-client
$ mvn clean package

$ java -jar target/alfresco-rest-client-1.0.0.jar

SYNTAX db-cmis
select * from cmis:document order by cmis:lastModificationDate
select * from cmis:document where contains('PATH:\"/app:company_home/st:sites/cm:swsdp/*/*"') order by cmis:lastModificationDate
-----------------
SYNTAX xpath
./app:company_home/st:sites
-----------------
SYNTAX cmis-alfresco
select * from cmis:document where contains('PATH:\"/app:company_home/st:sites/cm:swsdp/*/*"') order by cmis:lastModificationDate
-----------------
SYNTAX lucene
 +@cm\:modified:[2022\-10\-14T00\:00\:00.000 TO 2022\-10\-21T23\:59\:59.999] +@cm\:modifier:"admin" +TYPE:"cm:content" -TYPE:"cm:systemfolder" -TYPE:"fm:forums" -TYPE:"fm:forum" -TYPE:"fm:topic" -TYPE:"fm:post" +(TYPE:"content" OR TYPE:"app:filelink" OR TYPE:"folder")
+PATH:"/app:company_home/st:sites/cm:swsdp/cm:documentLibrary//*" +@cm\:modified:[2022\-10\-14T00\:00\:00.000 TO 2022\-10\-21T23\:59\:59.999] +@cm\:modifier:"admin" +TYPE:"cm:content" -TYPE:"cm:systemfolder" -TYPE:"fm:forums" -TYPE:"fm:forum" -TYPE:"fm:topic" -TYPE:"fm:post" +(TYPE:"content" OR TYPE:"app:filelink" OR TYPE:"folder")
+PATH:"/app:company_home/st:sites/cm:swsdp/cm:documentLibrary//*" +PATH:"/cm:generalclassifiable/member"
+PATH:"/app:company_home/st:sites/cm:swsdp/cm:documentLibrary//*" +TYPE:"cm:content" +@cm\:content.mimetype:image/*
+PATH:"/cm:generalclassifiable/cm:Languages/*" -PATH:"/cm:generalclassifiable/cm:Languages/member"
+PATH:"/cm:generalclassifiable/cm:Languages/cm:English/*" -PATH:"/cm:generalclassifiable/cm:Languages/cm:English/member"
-----------------
SYNTAX fts-alfresco
(bu AND +TYPE:"cm:content") AND -TYPE:"cm:thumbnail" AND -TYPE:"cm:failedThumbnail" AND -TYPE:"cm:rating" AND -TYPE:"fm:post" AND -ASPECT:"sys:hidden" AND -cm:creator:System
(budget ) AND ({http://www.alfresco.org/model/content/1.0}content.size:("0".."10240" ))
+TYPE:"{http://www.alfresco.org/model/site/1.0}site" AND ( cm:name:" swsdp*" OR  cm:title: ("swsdp*" ) OR cm:description:"swsdp")
PATH:"/"
TYPE:"cm:content" AND (cm:content.mimetype:"application/msword") AND (budget )
TYPE:"{http://www.alfresco.org/model/content/1.0}person" AND ("*admin*")
-----------------
```

## Additional steps

When sharing the Search Query Catalog obtained with third parties, remember to apply any anonymization to sensitive data.
