## mpMetrics and monitor features of Liberty Profile

There are a number of ways to instrument the java microservice code for monitoring metrics collection. In this lab we will use the WLP provided features: `mpMetrics-1.1` and `monitor-1.0` These features provides a `/metrics` REST interface that conforms to the or MicroProfile metrics 1.1 specification. Application developers can add their own custom metrics, by using the MicroProfile metrics API, alongside the metrics provided by Liberty. More information [here](https://www.ibm.com/support/knowledgecenter/en/SSEQTP_liberty/com.ibm.websphere.wlp.doc/ae/twlp_mp_metrics_monitor.html) 


## Enable Prometheus metrics for the WLP application

Go to the directory where our `b2m-java` application has been cloned.

```
cd ~/b2m-java
```

and edit the application server configuration file `src/main/liberty/config/server.xml`.

Uncomment these three lines:

```xml
        <feature>mpMetrics-1.1</feature>
        <feature>monitor-1.0</feature>
        <feature>mpHealth-1.0</feature>
```

Test the application locally:

```
cd ~/b2m-java
mvn clean install
mvn liberty:run-server
```

Access this URL via Firefox or `curl`: `http://localhost:9080/metrics`.


The output should be similar to:

```
 TYPE base:classloader_total_loaded_class_count counter
# HELP base:classloader_total_loaded_class_count Displays the total number of classes that have been loaded since the Java virtual machine has started execution.
base:classloader_total_loaded_class_count 7890
# TYPE base:gc_global_count counter
# HELP base:gc_global_count Displays the total number of collections that have occurred. This attribute lists -1 if the collection count is undefined for this collector.
base:gc_global_count 6
# TYPE base:cpu_system_load_average gauge
# HELP base:cpu_system_load_average Displays the system load average for the last minute. The system load average is the sum of the number of runnable entities queued to the available processors and the number of runnable entities running on the available processors averaged over a period of time. The way in which the load average is calculated is operating system specific but is typically a damped time-dependent average. If the load average is not available, a negative value is displayed. This attribute is designed to provide a hint about the system load and may be queried frequently. The load average may be unavailable on some platform where it is expensive to implement this method.
base:cpu_system_load_average 0.15
# TYPE base:thread_count counter
# HELP base:thread_count Displays the current number of live threads including both daemon and non-daemon threads.
base:thread_count 48
# TYPE base:classloader_current_loaded_class_count counter
# HELP base:classloader_current_loaded_class_count Displays the number of classes that are currently loaded in the Java virtual machine.
base:classloader_current_loaded_class_count 7890
# TYPE base:gc_scavenge_time_seconds gauge
# HELP base:gc_scavenge_time_seconds Displays the approximate accumulated collection elapsed time in milliseconds. This attribute displays -1 if the collection elapsed time is undefined for this collector. The Java virtual machine implementation may use a high resolution timer to measure the elapsed time. This attribute may display the same value even if the collection count has been incremented if the collection elapsed time is very short.
base:gc_scavenge_time_seconds 0.463
(...)
```
Stop the WLP server with `ctrl-c` and build the docker image using provided `Dockerfile`:

```
docker stop btm-java
docker rm btm-java
docker build -t b2m-java .
docker run --name btm-java -d -p 9080:9080 --log-driver=gelf \
--log-opt gelf-address=udp://localhost:5000 b2m-java
```

Commit the changes to your GiHub repository:

```
cd ~/b2m-java
git commit -am "I added monitoring instrumentation to my app!"
git push
```
