# Build to Manage - Java application monitoring and logging lab

During this lab we will instrument a sample java application for logging to use with log analytics tools like [Elastic stack](http://elastic.co) as well as for monitoring with [Prometheus](https://prometheus.io) and [Grafana](https://grafana.com).

Instrumentation of the application code for monitoring and logging is part of the general concept we call **Build to Manage**. It specifies the practice of activities developers can do in order to provide manageability aspects as part of an application release.

## Lab outline

- Fork and clone the Github repository with a simple java servlet and review the source code
- Configure logging library and add log messages to the application code
- Enable monitoring features of the WebSphere Liberty Profile
- Build the application with Apache Maven
- Configure and run Elastic stack in Docker Compose
- Configure and run Prometheus and Grafana stack in Docker Compose
- Build the java application container and integrate with Prometheus and ELK stack

## Prerequisites
Install the following software on your workstation or use a provided VM.

- JDK 8
- Apache Maven
- Docker
- Docker Compose

## Review the application code and run it locally

Logon to GitHub using your user and password.
Access the following repository and click `Fork`.

```
https://github.com/rafal-szypulka/b2m-java
```

From now on you will work on your own fork of the application project

```
https://github.com/<username>/b2m-java
```

Clone `b2m-java` lab repository to your home directory using:

```
cd
git clone https://github.com/<username>/b2m-java
```

Most of the commands in this lab should be executed from the `b2m-java` directory:

```
cd b2m-java
```

Review the application code in `src/main/java/application/rsapp/checkout.java`. 
This minimalistic application simulates a transaction with random response time on the following URL:

```
http://localhost:9080/rsapp/checkout
```

About 5% of requests should return error and 500 HTTP response code.

Run the following command to build the java application and package it into `target/rsapp.war`:

```
mvn clean install
```
 and then 
``` 
mvn liberty:run-server
``` 
to start the local WLP application server.


Use internet browser to access `http://localhost:9080/rsapp/checkout`
The expected output is JSON formatted:

```
{"status":"RSAP0001I: Transaction OK","transactionTime":"22ms"}
```

or:

```
{"error":"RSAP0010E: Severe problem detected"}
```

Refresh the page a couple of times and you should see random transaction response times. 