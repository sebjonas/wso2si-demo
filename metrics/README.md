# Metrics
This section explains how WSO2 Streaming Integrator servers can be configured to publish data to Prometheus and display statistics in Grafana dashboards.

## Download
Download the **WSO2 Streaming Integrator Server** ZIP-archive distribution from [WSO2 Streaming Integrator site](https://wso2.com/integration/streaming-integrator/) (click **Download** button) and extract it to a location of your choice. Hereafter, the extracted location is referred to as <SI_HOME>.

Download and install **[Prometheus](https://prometheus.io/download/)** & **[Grafana](https://grafana.com/grafana/download)** and configure according to the [instruction](https://ei.docs.wso2.com/en/latest/streaming-integrator/admin/setting-up-grafana-dashboards/). Here's a [Youtube video tutorial](https://youtu.be/rhmtkPOx0Gw). Hereafter, the extracted Prometheus location is referred to as <PROM_HOME> and extracted Grafana location is referred to as <GRAF_HOME>.

Change port 9090 for WSO2 Carbon Server to avoid conflicts:
1. Open <SI_HOME>/conf/server/deployment.yaml 
2. Search for 9090 and change it to 9099

## Run
Start the **Streaming Integrator Server** by issuing one of the following commands from the <SI_HOME>/bin directory.

* For Windows: ```.\server.bat --run```
* For Linux: ```./server.sh```

Start the **Streaming Integrator Tooling** by issuing one of the following commands from the <SIT_HOME>/bin directory.

* For Windows: ```.\tooling.bat```
* For Linux: ```./tooling.sh```

Start the **Prometheus** by issuing one of the following commands from the <PROM_HOME> directory.

* For Windows: ```.\prometheus.exe```
* For Linux: ```./prometheus```

Start the **Grafana** by issuing one of the following commands from the <GRAF_HOME> directory.

* For Windows: ```.\bin\grafana-server.exe```
* For Linux: ```bin/grafana-server```

## Open
Access UIs:

1. **Streaming Integrator Tooling** via [http://localhost:9390/editor](http://localhost:9390/editor).

2. **Grafana** via [http://localhost:3000/](http://localhost:3000/) (admin/admin).

## App
From the menu click **New** then copy and paste the code below into the editor, then save it as 'FraudulentCardUse'.

```
@App:name("FraudulentCardUse")
@App:statistics(reporter = 'prometheus')
@App:description("Detect fraudulent use of a credit card.")

@Source(type = 'http',
        receiver.url='http://localhost:8006/tradeStream',
        basic.auth.enabled='false',
        @map(type='json'))
define stream TradeStream(creditCardNo string, amount double);

@sink(type='log')
define stream SuspiciousTradeStream(creditCardNo string, totalAmount double, totalCount long);

@info(name='SuspiciousTrade')
from TradeStream#window.time(20 sec)
select 
    creditCardNo, 
    sum(amount) as totalAmount,
    count(creditCardNo) as totalCount
group by creditCardNo 
having totalAmount > 10000.0 and totalCount > 3
insert into SuspiciousTradeStream;
```

## Simulate

Use REST endpoint at port 8006 to send in events to the Siddhi app.
1. Open a terminal window.
2. Send in a couple of events.
    For Windows: 
    ```
    Invoke-WebRequest -Uri http://localhost:8006/tradeStream -Method POST -Body '{"event": {"creditCardNo": "143-90099-23431","amount": 3000.0}}'
    ```
    For Linux: 
    ```
    curl --location --request POST 'http://localhost:8006/tradeStream' --header 'Content-Type: application/json' --data-raw '{"event": {"creditCardNo":"143-90099-23431","amount": 3000.0}}'
    ```

## View
Open a **Grafana Dashboard** and monitor the result.

![grafana](/img/grafana.png)

## Stop

Stop the running services:
1. Stop the Siddhi app and press ctrl-C in the terminal where you started Streaming Integrator Tooling
4. Press ctrl-C in the terminals running Grafana and Prometheus (in that order)

[back to toc](../README.md)