# eID Identity Provider Java Example

This example demonstrates eID Identity Provider integration in a Java EE web application.
For authentication protocol we use OpenID 2.0 via the OpenID4Java library.

Compile this example via:
```
mvn clean install
```

Deploy on a local running JBoss/WildFly application server via:
```
mvn jboss-as:deploy
```

Point your web browser to:
```
http://localhost:8080/eid-idp-java-example/
```