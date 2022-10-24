# Alfresco REST Client

This project includes an Alfresco Rest Client to get information from Audit Module.

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

# Audit parameters
audit.app.id=search
audit.api.max.items=100
audit.query.similarity.threshold=0.95
```

Modifying these values requires to re-compile the project.

## Running the command

Run the program from command line.

```
$ java -jar target/alfresco-rest-client-1.0.0.jar
```
