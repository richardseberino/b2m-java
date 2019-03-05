## Containerize the app and deploy to IBM Cloud Private

### Create a Docker container

Use provided `Dockerfile` to build application container:

```
docker build -t b2m-java .
```

Test container locally (make sure you stopped local WLP server).

```
docker run -d -p 9080:9080 b2m-java
```
Access the `http://localhost:9080/rsapp/checkout` to verify the application is running.


Now, our Java application container can be deployed on ICP cluster. Make sure the `kubectl` client is configured to connect to your ICP cluster. More information [here](https://www.ibm.com/support/knowledgecenter/SSBS6K_3.1.1/manage_cluster/install_kubectl.html).

The `b2m-java` application container image has been uploaded to public Docker Hub: `rszypulka/b2m-java`. You can also upload it to the local ICP Container Registry.

### Deploy b2m-java application to the ICP cluster

Review provided YAML file `b2m-java-icp.yml` and use it to deploy the application to IBM Cloud Private cluster. `b2m-java` deployment object will pull application image container `rszypulka/b2m-java` from Docker Hub.

```
kubectl apply -f b2m-java-icp.yml
```

Verify deployment status.

```
$ kubectl get deploy b2m-java
NAME         DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
b2m-java   1         1         1            1           4h
```

Find the external IP of the worker node where the pod is running (look for column `NODE`.

```
kubectl get pod <pod_name> -o wide
```

Get the external `NodePort`.

```
kubect get svc b2m-java
```

Use the browser to access the application URL: `http://<node_external_ip>:<external_nodeport>` 



## Enable monitoring using ICP Prometheus and Grafana

Add the following configuration in the `monitoring-prometheus` ConfigMap, `scrape_configs:` section.

```
    scrape_configs:
      - job_name: 'b2m-java'
        scrape_interval: 20s
        static_configs:
          - targets:
            - b2m-java.default.svc:80
            labels:
              service: 'btm-java'
              group: 'production'
```
Make sure the indentation is correct.

[Import](http://docs.grafana.org/reference/export_import/) provided Grafana dashboard `ibm-open-liberty-grafana-dashboard.json`.

Generate ICP application traffic using provided script:

```
./load_test_icp.sh <application_url>
```
Use the `<application_url>` collected in previous chapter.

Access the ICP Grafana console and verify it properly shows metrics.


## Define kubernetes liveness probe for use with built-in application health check

The provided `b2m-java-icp.yml` deployment YAML file defines the [liveness probe](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-probes/#define-a-liveness-http-request) that use the implemented `/health` route.

```
   livenessProbe:
     httpGet:
       path: /health
       port: 3001
     initialDelaySeconds: 3
     periodSeconds: 10
```
Check the URL: `http://<node_external_ip>:<external_nodeport>/health` to verify current health status.

>Expected output: `{"status":"ok"}`


