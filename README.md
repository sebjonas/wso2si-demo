# Overview

This repository contains instructions on how to install and run WSO2 Streaming Integrator to make use of **Siddhi**.

For a more in-depth understanding of WSO2 Streaming Integrator: 
- [Architecture](https://ei.docs.wso2.com/en/latest/streaming-integrator/overview/architecture/)
- [Product documentation](https://ei.docs.wso2.com/en/latest/streaming-integrator/overview/overview/)
- [Siddhi documentation](https://siddhi.io)

If you have any questions about this repository, feel free to contact [Jonas Ekström](mailto:jonas.ekstrom@seb.se).

# Usage
Clone this repository to your local machine: ```git clone https://github.com/sebjonas/wso2si-demo.git```

# Prerequisites

1. [Git](https://git-scm.com/downloads)
2. [Java 8](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
3. [Docker for Desktop](https://www.docker.com/products/docker-desktop)

# Table of Content

- [Overview](#overview)
- [Usage](#usage)
- [Prerequisites](#prerequisites)
- [Getting started](#getting-started)
  * [Download](#download)
  * [Run](#run)
  * [Open](#open)
  * [App](#app)
  * [Execute](#execute)
  * [View](#view)
  * [Stop](#stop)
- [Kafka as Source & Sink](kafka)
- [RDMS featuring MySQL](rdms)
- [File as Source](file)
- [Monitor using Elasticsearch and Kibana](monitor)
- [Rules for Business Agility](rules)
- [Metrics using Prometheus and Grafana](metrics)
- [Docker as hosting service](docker)
- [Java as hosting service](java)

# Getting started

## Download
Download the **Streaming Integrator Tooling** ZIP-archive distribution from [WSO2 Streaming Integrator site](https://wso2.com/integration/streaming-integrator/) (click **Tooling** button) and extract it to a location of your choice. Hereafter, the extracted location is referred to as <SIT_HOME>.

## Run
Start the **Streaming Integrator Tooling** by issuing one of the following commands from the <SIT_HOME>/bin directory.

* For Windows: ```.\tooling.bat```
* For Linux: ```./tooling.sh```

## Open
Access the **Streaming Integrator Tooling** via [http://localhost:9390/editor](http://localhost:9390/editor). The Streaming Integrator Tooling opens as shown below.

![streaming-integrator-tooling-welcome-page](/img/streaming-integrator-tooling-welcome-page.png)

## App
On the Welcome page click **New** then copy and paste the code below into the editor, then save it as 'FraudulentCardUse'.

```
@App:name("FraudulentCardUse")
@App:description("Detect fraudulent use of a credit card.")

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

Events subscribed to from the trade stream where they will be grouped by credit card number and if the total amount is more than 1000 and total number of events are more than 3 - all within a 20 seconds time period - then a new event will be created and inserted to the suspicious trade stream.

<img src="/img/fraudulentcarduse.png" width="400">

## Execute
From the **Streaming Integrator Tooling** menu click **Run**.

## Simulate
Configure random event simulation as follows:
1. Click on **Event Simulator** (double arrows on left tab)
2. Click **Feed Simulation -> Create**
3. Give a name (Or the default name in the place holder will be used as its name)
4. Select **Random** as the Simulation Source
5. Click on **Add Simulation Source**
6. Select **FraudulentCardUse** as Siddhi App Name
7. Select **TradesStream** as StreamName
8. For **creditCardNo** change config type to **Regex based** and give the pattern as **143-90099-2343[0-9]{1}**.
  Sites to try out regex (https://regex101.com and https://regexr.com)
9. Keep **Primitive Based** as the config type for **amount** but change from **100** to less than **1000** with *1* decimal.
10. Save the simulator configuration
11. The newly created simulator would be listed under *Active Feed Simulations** of **Feed Simulation** tab
12. Click on the **start** button (Arrow symbol) next to the newly created simulator

## View
See the input and respective output on the console. The output reflects the fraudulent transactions, aggregated over the last 20 seconds.

<img src="/img/sim.gif" width="150">

## Stop
Stop the Feed Simulation, stop the Siddhi app and press ctrl-C in the terminal where you started Streaming Integrator Tooling.

[back to toc](#table-of-content)