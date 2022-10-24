# Alfresco REST Client

This project includes an Alfresco Rest Client performing following operations:

* `create-site` populates several sites using several users with documents
* `search` perform sample queries in a loop

## Compiling the project

Use default Maven command.

```
$ mvn clean package
```

## Configuration options

Default values can be modified in `application.properties` file.

```
$ cat src/main/resources/application.properties

# Alfresco Server
content.service.url=http://localhost:8080
content.service.security.basicAuth.username=
content.service.security.basicAuth.password=

content.service.path=/alfresco/api/-default-/public/alfresco/versions/1
search.service.path=/alfresco/api/-default-/public/search/versions/1

# Action (create-site, search)
action.name=search

# JSON Path produced with "sizing-guide-data-generator" project
action.site.json.path=/Users/aborroy/Desktop/git/sizing-guide-data-generator/generated/json/docx50_pptx0_pdf50_jpg0_txt0_metadataId1/0
action.site.sites=5
action.site.users=10

action.search.count=100
```

Modifying these values requires to re-compile the project.

## Running the command

Run the program from command line.

```
$ java -jar target/alfresco-rest-client-1.0.0.jar
```
