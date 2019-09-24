# KIT Data Manager - Authentication Service

This project contains authentication microservice for the KIT DM infrastructure. The service provides
user and group management as well as a login endpoint creating JSON Web Token credentials, which can
be handed over to other services for authorization decisions.

## How to build

After obtaining the sources change to the folder where the sources are located perform the following steps:

```
user@localhost:/home/user/auth-service$ ./gradlew -Pclean-release build
> Configure project :
Using release profile for building auth-service
<-------------> 0% EXECUTING [0s]
[...]
user@localhost:/home/user/auth-service$
```

The Gradle wrapper will now take care of downloading the configured version of Gradle, checking out all required libraries, build these
libraries and finally build the auth-service microservice itself. As a result, a fat jar containing the entire service is created at 'build/jars/auth-service.jar'.

## How to start

Before you are able to start the authentication microservice, you have to modify the application properties according to your local setup. 
Therefor, copy the file 'settings/application.properties' to your project folder and customize it. Special attentioned should be payed to the
properties in the 'datasource' section as well as the 'jwtSecret', which should have assigned a random value of a certain length, e.g. 32 characters,
so it is impossible to guess this secret. 

As soon as 'application.properties' is completed, you may start the authentication microservice by executing the following command inside the project folder, 
e.g. where the service has been built before:

```
user@localhost:/home/user/auth-service$ ./build/libs/auth-service.jar

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.0.0.RELEASE)
[...]
1970-01-01 00:00:00.000  INFO 56918 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''

```

If your 'application.properties' is not located inside the project folder you can provide it using the command line argument --spring.config.location=<PATH_TO_APPLICATION.PROPERTIES>
As soon as the microservice is started, you can browse to 

http://localhost:8080/swagger-ui.html

in order to see available RESTful endpoints and their documentation. Furthermore, you can use this Web interface to test single API calls in order to get familiar with the 
service. A small documentation guiding you through the first steps of using the RESTful API you can find at

http://localhost:8080/static/docs/documentation.html

## More Information

* [Information about JSON Web Token](https://jwt.io/)

## License

The KIT Data Manager is licensed under the Apache License, Version 2.0.