# CatsHostel - Ktor-Version 
> [Udemy](https://www.udemy.com/course/web-development-with-kotlin/) 
> [Spring Boot Version](https://github.com/MikeMitterer/kotlin-catshostel-sb) 
> [GH Repo](https://github.com/AlexeySoshin/KotlinWebDevelopment)  
> [MyBatis](https://blog.mybatis.org/)    

This example is based on the according [Udemy](https://www.udemy.com/course/web-development-with-kotlin/) course.

I added some other features:
  
   - iBatis (works with SQLite or Postgres-DB)
   - JWT support
   - [KTor](https://github.com/MikeMitterer/kotlin-catshostel/blob/master/test/unit/at/mikemitterer/catshostel/jwt/JavaWebTokenTest.kt#L51)
   - AutoRefresh (see below)
     
## Database

    # cd to DB-Directory
    cd resources/db
    
    # Import schema (Postgres works just fine here)
    sqlite catshostel.db < sqlite-1-schema.sql

Set your working dir to $MODULE_WORKING_DIR$

![WorkingDir](doc/images/working-dir.png)


## Application
> [Fertige App im GH-Repo](https://github.com/AlexeySoshin/KotlinWebDevelopment/tree/chapter9-10)
    
    gradle run
    
## WebSocket
> [KTOR.io - WebSockets](https://ktor.io/servers/features/websockets.html)    
> [Chat-Example](https://ktor.io/samples/app/chat.html)
> [How to implement a chat with WebSockets](https://ktor.io/quickstart/guides/chat.html)   

## FatJar
> [Shadow-Plugin](https://imperceptiblethoughts.com/shadow/)
> [KTor-Doku (schwach)](https://ktor.io/servers/deploy/packing/fatjar.html)

    # Erstellt das JAR
    gradle shadowJar
    
## Auto-Refresh
> [KTor - AutoReload](https://ktor.io/servers/autoreload.html)

You need **Java > 8** for this

    # Consol-Windows 1
    gradle -t installDist
    
    # Consol-Windows 2
    gradle run
    
[Open REST-Server](http://0.0.0.0:8080/)    

## KeyCloak

To setup a user with KeyCloak follow one of these tutorials:

   - [A Quick Guide to Using Keycloak](https://www.baeldung.com/spring-boot-keycloak)
   - [API login and JWT token generation using Keycloak](https://developers.redhat.com/blog/2020/01/29/api-login-and-jwt-token-generation-using-keycloak/)
   - [Getting started with Keycloak](https://codergists.com/redhat/keycloak/security/authentication/2020/01/07/getting-started-with-keycloak-on-rhel8.html)
       