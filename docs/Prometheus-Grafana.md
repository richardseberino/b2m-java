### Deploy a local Prometheus and Grafana stack with Docker Compose

>Step 1 is already done for the lab VM and `prometheus` repo is located in `/root/prometheus`.

1). Clone the `prometheus` docker compose repository from Github:

```
git clone https://github.com/vegasbrianc/prometheus
```

2). Add scraping job definition to the Prometheus configuration file `prometheus/prometheus/prometheus.yml` by adding (uncommenting in the lab VM) the following code within `scrape_config` section:

```
  - job_name: 'btm-java'
    scrape_interval: 5s
    static_configs:
    - targets: ['xxx.xxx.xxx.xxx:9080']
      labels:
        service: 'b2m-java'
        group: 'production'

```
replace xxx.xxx.xxx.xxx with your own host machine's IP.

3). Start Prometheus & Grafana stack:
   
```
cd ~/prometheus
docker-compose up -d
```
Expected output:
```
Creating network "prometheus_back-tier" with the default driver
Creating network "prometheus_front-tier" with the default driver
Creating prometheus_cadvisor_1      ... done
Creating prometheus_alertmanager_1  ... done
Creating prometheus_node-exporter_1 ... done
Creating prometheus_prometheus_1    ... done
Creating prometheus_grafana_1       ... done

```

Verify that Prometheus started via: [http://localhost:9090](http://localhost:9090/graph)


## Set the Prometheus datasource in Grafana

Verify the prometheus datasource configuration in Grafana. If it was not already configured, [create](http://docs.grafana.org/features/datasources/prometheus/#adding-the-data-source-to-grafana) a Grafana datasource with this settings:

+ name: prometheus
+ type: prometheus
+ url: http://localhost:9090
+ access: browser


## Configure dashboard

Grafana Dashboard to [import](http://docs.grafana.org/reference/export_import/#importing-a-dashboard): `ibm-open-liberty-grafana-dashboard.json`

Expected views:
CPU & Memory utilization for Libety Profile:
![](images/prometheus-liberty1.png)

Servlet requests volume and response time:
![](images/prometheus-liberty2.png)