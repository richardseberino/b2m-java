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

IBM provides helm charts for WebSphere Liberty and Open Liberty. These helm charts simplify configuration of monitoring and logging and allow to enable built-in [monitoring](https://www.ibm.com/support/knowledgecenter/en/SSEQTP_liberty/com.ibm.websphere.wlp.doc/ae/twlp_mp_metrics_monitor.html) and [logging](https://www.ibm.com/support/knowledgecenter/en/SSEQTP_liberty/com.ibm.websphere.wlp.doc/ae/rwlp_logging.html) features of Liberty without manual modification of Liberty config files we did earlier. WebSphere Liberty provides also a [health feature](https://www.ibm.com/support/knowledgecenter/en/SSEQTP_liberty/com.ibm.websphere.wlp.doc/ae/twlp_microprofile_healthcheck.html) that allows to implement application health checks and expose a health API that can be used by kubernetes [liveness probe](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-probes/) as well as external monitoring tools. 

Setup your CLI environment:

```
cloudctl login -a https://<ICP_CLUSTER_IP>:8443
```
replace <ICP_CLUSTER_IP> with the IP address of you ICP cluster.

Verify you can connect your helm client with tiller running in the ICP cluster:

```
$ helm version --tls
Client: &version.Version{SemVer:"v2.9.1", GitCommit:"20adb27c7c5868466912eebdf6664e7390ebe710", GitTreeState:"clean"}
Server: &version.Version{SemVer:"v2.9.1+icp", GitCommit:"8ddf4db6a545dc609539ad8171400f6869c61d8d", GitTreeState:"clean"}
```

Run the following command to deploy our java application on ICP Cluster:

```
helm install --name btm-java --namespace default ibm-charts/ibm-open-liberty \
--set monitoring.enabled=true \
--set image.repository=rszypulka/b2m-java \
--set image.tag=latest,ssl.enabled=false \
--set ssl.useClusterSSLConfiguration=false \
--set ssl.createClusterSSLConfiguration=false \
--set service.port='9080' --set service.targetPort='9080' \
--set ingress.enabled=false \
--set jmsService.enabled=false \
--set iiopService.enabled=false \
--set logs.persistLogs=false \
--set logs.persistTransactionLogs=false \
--set autoscaling.enabled=false \
--set resources.constraints.enabled=false \
--set microprofile.health.enabled=true \
--set sessioncache.hazelcast.enabled=false \
--set license=accept --tls
```

The command above is just an example and helm chart for WebSphere Liberty provides more options. More information [here](https://github.com/IBM/charts/tree/master/stable/ibm-websphere-liberty).
You can also deploy the WebSphere Liberty helm chart from the ICP Catalog.

Verify status of `btm-java` helm release:

```
$ helm status btm-java --tls
LAST DEPLOYED: Mon Mar 18 15:44:31 2019
NAMESPACE: default
STATUS: DEPLOYED

RESOURCES:
==> v1/ConfigMap
NAME                      DATA  AGE
btm-java-ibm-open-libert  5     1h

==> v1/Service
NAME                      TYPE      CLUSTER-IP  EXTERNAL-IP  PORT(S)         AGE
btm-java-ibm-open-libert  NodePort  10.0.0.122  <none>       9080:31936/TCP  1h

==> v1/Deployment
NAME                      DESIRED  CURRENT  UP-TO-DATE  AVAILABLE  AGE
btm-java-ibm-open-libert  1        1        1           1          1h

==> v1/Pod(related)
NAME                                      READY  STATUS   RESTARTS  AGE
btm-java-ibm-open-libert-b6cdbbd59-qw58b  1/1    Running  0         1h
```

Get the application URL by running these commands:
```
export NODE_PORT=$(kubectl get --namespace default \
-o jsonpath="{.spec.ports[0].nodePort}" services btm-java-ibm-open-libert)

export NODE_IP=$(kubectl get nodes -l proxy=true \
-o jsonpath="{.items[0].status.addresses[?(@.type==\"Hostname\")].address}")

echo http://$NODE_IP:$NODE_PORT
```

Use an internet browser or `curl` to access:
- Application URL: `http://$NODE_IP:$NODE_PORT/rsapp/checkout`
- Prometheus metrics URL: `http://$NODE_IP:$NODE_PORT/metrics`
- Health API URL: `http://$NODE_IP:$NODE_PORT/health`

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

The WebSphere Liberty helm chart configures a default liveness and readiness probes that checks `/health` route. It can be modified if needed both during helm chart deployment (by configuring image.readinessProbe and image.livenessProbe parameters) or by editing the application deployment. The application container is considered healthy if connection can be established and http response code equals `200`, otherwise it's considered a failure.

More information about configuring liveness and readiness probes can be found [here](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-probes/).

The default liveness probe definition:
```
        livenessProbe:
          failureThreshold: 3
          httpGet:
            path: /health
            port: 9080
            scheme: HTTP
          initialDelaySeconds: 20
          periodSeconds: 5
          successThreshold: 1
          timeoutSeconds: 1
```
Check the URL: `http://<node_external_ip>:<external_nodeport>/health` to verify current health status.

>Expected output: `{"checks":[],"outcome":"UP"}`


