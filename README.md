# eID Identity Provider Java Example

This example demonstrates eID Identity Provider integration in a Java EE web application.
For authentication protocol we use OpenID 2.0 via the OpenID4Java library.


## Compilation

We use Maven as build system.

Compile this example via:
```
mvn clean install
```

## Deployment

Deploy on a local running JBoss EAP 6 application server via:
```
mvn jboss-as:deploy
```

Deploy on a local running WildFly 9 application server via:
```
mvn wildfly:deploy 
```


## Usage

Point your web browser to:
```
http://localhost:8080/eid-idp-java-example/
```