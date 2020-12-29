# Overview

This repository contains instructions on how to install and run WSO2 Streaming Integrator to make use of **Siddhi**.

For a more in-depth understanding of WSO2 Streaming Integrator: 
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
- [Kafka](kafka/README.md)
- [RDMS](#rdms)
- [Metrics](#metrics)
- [File](#file)
- [Monitor](#monitor)
- [Rules](#rules)

# Getting started

## Download
Download the **Streaming Integrator Tooling** ZIP-archive distribution from [WSO2 Streaming Integrator site](https://wso2.com/integration/streaming-integrator/) (click **Tooling** button) and extract it to a location of your choice. Hereafter, the extracted location is referred to as <SIT_HOME>.

## Run
Start the **Streaming Integrator Tooling** by issuing one of the following commands from the <SIT_HOME>/bin directory.

* For Windows: ```streaming-integrator-tooling.bat```
* For Linux: ```./streaming-integrator-tooling.sh```

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

# Kafka
Let's add Kafka as source and sink for the **TradeStream** and the **SuspiciousTradeStream**.

## Download
Download the Kafka broker from the [Apache site](https://ei.docs.wso2.com/en/latest/streaming-integrator/quick-start-guide/quick-start-guide/) and extract it. This directory is referred to as <KAFKA_HOME> from here on.

## Run
Let's start the Kafka server and create a Kafka topic so that the **FraudulentCardUse** Siddhi application you created can subscribe and publish its output to it.

To start Kafka:
1. Navigate to the <KAFKA_HOME> directory and start a zookeeper node by issuing the following command.
    ```sh bin/zookeeper-server-start.sh config/zookeeper.properties```
2. Navigate to the <KAFKA_HOME> directory and start Kafka server node by issuing the following command.
    ```sh bin/kafka-server-start.sh config/server.properties```

To create a Kafka topic named **trade-stream** and another named **suspicious-trade-stream**:
1. Navigate to <KAFKA_HOME> directory and issue the following command:
    ```bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic trade-stream```
2. Navigate to <KAFKA_HOME> directory and issue the following command:
    ```bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic suspicious-trade-stream```

Start the **Streaming Integrator Tooling** by issuing one of the following commands from the <SIT_HOME>/bin directory.

* For Windows: ```streaming-integrator-tooling.bat```
* For Linux: ```./streaming-integrator-tooling.sh```

## App
Open the **FraudulentCardUse** Siddhi app.

Add a **source** for the TradeStream definition:

```
@source(type='kafka',
        topic.list='trade-stream',
        partition.no.list='0',
        threading.option='single.thread',
        group.id="group",
        bootstrap.servers='localhost:9092',
        @map(type='json'))
```

Add a **sink** for the SuspiciousTradeStream definition:

```
@sink(type='kafka',
      topic='suspicious-trade-stream',
      bootstrap.servers='localhost:9092',
      partition.no='0',
      @map(type='json'))
```

The modified app looks like this:

```
@App:name("FraudulentCardUse")
@App:description("Detect fraudulent use of a credit card.")

@source(type='kafka',
        topic.list='trade-stream',
        partition.no.list='0',
        threading.option='single.thread',
        group.id="group",
        bootstrap.servers='localhost:9092',
        @map(type='json'))
define stream TradeStream(creditCardNo string, amount double);

@sink(type='kafka',
      topic='suspicious-trade-stream',
      bootstrap.servers='localhost:9092',
      partition.no='0',
      @map(type='json'))
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

## Execute
From the **Streaming Integrator Tooling** menu click **Run**.

## Consume
1. Navigate to <KAFKA_HOME> directory and issue the following command:
    ```bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic suspicious-trade-stream```

## Simulate

1. Navigate to <KAFKA_HOME> directory and issue the following command:
    ```bin/kafka-console-producer.sh --broker-list localhost:9092 --topic trade-stream```
2. Paste in the following sequence of events:
    - ```[{"event": {"creditCardNo":"143-90099-23431", "amount":5000.0}}]```
    - ```[{"event": {"creditCardNo":"143-90099-23431", "amount":4000.0}}]```
    - ```[{"event": {"creditCardNo":"143-90099-23431", "amount":3000.0}}]```

## View
The expected outcome in the **suspicious-trade-stream** topic:
```
{"event":{"creditCardNo":"143-90099-23431","totalAmount":12000.0,"totalCount":3}}
```
You can also run the [Feed Simulation](#simulate) again and view the results in the Kafka consumer terminal. 

## Stop

Stop the running services:
1. Stop the Siddhi app and press ctrl-C in the terminal where you started Streaming Integrator Tooling
2. Press ctrl-C in the terminals running Kafka producer and consumer
3. Press ctrl-C in the terminals running Kafka and Zookeeper (in that order)

[back to toc](#table-of-content)

#RDMS

Let's join the trade stream with data in a database table. This application demonstrates how to perform join on streaming data with the data stored in RDBMS. The sample depicts a scenario, where a transaction by a credit card with which fraudulent activity has been previously done. The credit card numbers, which were noted for fraudulent activities are stored in an RDBMS table.

## Download
**MySQL Server**:
1. Start Docker Desktop
2. Navigate to ```https://hub.docker.com/_/mysql``` to access documentation
3. Run ```docker pull mysql:latest```

**MySQLWorkbench**:
1. Navigate to ```https://dev.mysql.com/downloads/workbench/```
2. Follow instructions and install

## Run
Let's start the MySQL server, create a db, a schema and insert some rows to join with the stream.

**MySQL Server**:
1. Start Docker Desktop
2. Run ```docker run -p 3306:3306 --name some-mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw mysql:latest```

**MySQLWorkbench**:
1. Open MySQL Workbench
2. Connect to your running database instance
3. Open the file ```mysql/configure.sql``` and click Execute

**Streaming Integrator Tooling**:

* For Windows: ```streaming-integrator-tooling.bat```
* For Linux: ```./streaming-integrator-tooling.sh```

## App
Open **Streaming Integrator Tooling** and select **New** from the menu then copy and paste the code below into the editor and save it as 'BlockedCardUse'.

```
@App:name("BlockedCardUse")

@App:description('Join streaming data with data stored in an RDBMS table')

define stream TradeStream(creditCardNo string, amount double);

@sink(type='log')
define stream SuspiciousTradeStream(creditCardNo string, amount double);

@Store(type="rdbms",
       jdbc.url="jdbc:mysql://localhost:3306/fraudDB?useSSL=false",
       username="root",
       password="my-secret-pw" ,
       jdbc.driver.name="com.mysql.jdbc.Driver")
@PrimaryKey("creditCardNo")
define table FraudTable (creditCardNo string);

@info(name='BlockedCardTrade')
from TradeStream as ts join FraudTable as ft
    on ts.creditCardNo == ft.creditCardNo
select 
    ts.creditCardNo,
    ts.amount
insert into SuspiciousTradeStream;
```

## Execute
From the **Streaming Integrator Tooling** menu click **Run**.

## Simulate
Configure random event simulation as follows:
1. Click on **Event Simulator** (double arrows on left tab)
2. Click **Feed Simulation -> Create**
3. Give a name (Or the default name in the place holder will be used as its name)
4. Select **Random** as the Simulation Source
5. Click on **Add Simulation Source**
6. Select **BlockedCardUse** as Siddhi App Name
7. Select **TradesStream** as StreamName
8. For **creditCardNo** change config type to **Regex based** and give the pattern as **143-90099-2343[0-9]{1}**.
9. Keep **Primitive Based** as the config type for **amount** but change from **100** to less than **1000** with *1* decimal.
10. Save the simulator configuration
11. The newly created simulator would be listed under *Active Feed Simulations** of **Feed Simulation** tab
12. Click on the **start** button (Arrow symbol) next to the newly created simulator

## View
The output reflects the blocked card use trasactions.

## Stop
Stop the Feed Simulation, stop the Siddhi app and press ctrl-C in the terminal where you started Streaming Integrator Tooling.

[back to toc](#table-of-content)

# Metrics

## Download
Download the **WSO2 Streaming Integrator Server** ZIP-archive distribution from [WSO2 Streaming Integrator site](https://wso2.com/integration/streaming-integrator/) (click **Download** button) and extract it to a location of your choice. Hereafter, the extracted location is referred to as <SI_HOME>.

Download and install **Prometheus & Grafana** and configure according to the [instruction](https://ei.docs.wso2.com/en/latest/streaming-integrator/admin/setting-up-grafana-dashboards/). Here's a [Youtube video tutorial](https://youtu.be/rhmtkPOx0Gw). Hereafter, the extracted Prometheus location is referred to as <PROM_HOME> and extracted Grafan location is referred to as <GRAF_HOME>.

## Run
Start the **Streaming Integrator Server** by issuing one of the following commands from the <SI_HOME>/bin directory.

* For Windows: ```server.bat --run```
* For Linux: ```./server.sh```

Start the **Streaming Integrator Tooling** by issuing one of the following commands from the <SIT_HOME>/bin directory.

* For Windows: ```streaming-integrator-tooling.bat```
* For Linux: ```./streaming-integrator-tooling.sh```

Start the **Prometheus** by issuing one of the following commands from the <PROM_HOME> directory.

* For Linux: ```./prometheus```

Start the **Grafana** by issuing one of the following commands from the <GRAF_HOME> directory.

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

@source(type='kafka',
        topic.list='trade-stream',
        partition.no.list='0',
        threading.option='single.thread',
        group.id="group",
        bootstrap.servers='localhost:9092',
        @map(type='json'))
define stream TradeStream(creditCardNo string, amount double);

@sink(type='kafka',
      topic='suspicious-trade-stream',
      bootstrap.servers='localhost:9092',
      partition.no='0',
      @map(type='json'))
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

1. Navigate to <KAFKA_HOME> directory and issue the following command:
    ```bin/kafka-console-producer.sh --broker-list localhost:9092 --topic trade-stream```
2. Paste in the following event a number of times:
    ```[{"event": {"creditCardNo":"143-90099-23431", "amount":5000.0}}]```

## View
Open a **Grafana Dashboard** and monitor the result.

![grafana](/img/grafana.png)

## Stop

Stop the running services:
1. Stop the Siddhi app and press ctrl-C in the terminal where you started Streaming Integrator Tooling
2. Press ctrl-C in the terminals running Kafka producer
3. Press ctrl-C in the terminals running Kafka and Zookeeper (in that order)
4. Press ctrl-C in the terminals running Grafana and Prometheus (in that order)

[back to toc](#table-of-content)

# File
This sample demonstrates how to use external time windows for a fraud detection use-case. In this sample, we look for two or more transactions done within a very short period of time and send an alert immediately when such an occurrence is detected.

## Run

Start the **Streaming Integrator Tooling** by issuing one of the following commands from the <SIT_HOME>/bin directory.

* For Windows: ```streaming-integrator-tooling.bat```
* For Linux: ```./streaming-integrator-tooling.sh```

## Open
Access UIs:

1. **Streaming Integrator Tooling** via [http://localhost:9390/editor](http://localhost:9390/editor).

## App
Open **Streaming Integrator Tooling** and select **New** from the menu then copy and paste the code below into the editor and save it as 'CSVFileExternalTime'.

```
@App:name("CSVFileExternalTime")
@App:description("Load transactions from file to detect fraudulent use of a credit cards.")

define window TradeStreamWindow (eventTime long, creditCardNo string, amount double) externalTime(eventTime, 20 sec);

define stream TradeFile(timestamp string, creditCardNo string, amount double);
define stream TradeStream(eventTime long, creditCardNo string, amount double);

@sink(type='log')
define stream SuspiciousTradeStream(creditCardNo string, totalAmount double, totalCount long);

@info(name='TradeFile')
from TradeFile
select 
    time:timestampInMilliseconds(timestamp) as eventTime, 
    creditCardNo, 
    amount
insert into TradeStream;

@info(name='TradeWindow')
from TradeStream
insert into TradeStreamWindow;

@info(name='SuspiciousTradeStream')
from TradeStreamWindow
select 
    creditCardNo, 
    sum(amount) as totalAmount,
    count(creditCardNo) as totalCount
group by creditCardNo
having totalAmount > 10000.0 and totalCount > 1
insert into SuspiciousTradeStream;
```

## Execute
From the **Streaming Integrator Tooling** menu click **Run**.

## Simulate
Configure random event simulation as follows:
1. Click on **Event Simulator** (double arrows on left tab)
2. Click **Feed Simulation -> Create**
3. Give a name (Or the default name in the place holder will be used as its name)
4. Select **CSV** as the Simulation Source
5. Click on **Add Simulation Source**
6. Select **CSVFileExternalTime** as Siddhi App Name
7. Select **TradesFile** as StreamName
8. Click on Upload and select the '/csv/trades.csv'.
9. Save the simulator configuration
10. The newly created simulator would be listed under *Active Feed Simulations** of **Feed Simulation** tab
11. Click on the **start** button (Arrow symbol) next to the newly created simulator

## View
The expected outcome in the **SuspiciousTradeStream** topic:
```
CSVFileExternalTime.siddhi -  Started Successfully!
Feed Simulation 1 simulation started Successfully!
[2020-12-28_17-51-27_649] INFO {io.siddhi.core.stream.output.sink.LogSink} - CSVFileExternalTime : SuspiciousTradeStream : Event{timestamp=1609174287629, data=[143-90099-23433, 15000.0, 3], isExpired=false} 
Event Simulation finished for "Feed Simulation 1".
```

## Stop

Stop the Siddhi app and press ctrl-C in the terminal where you started Streaming Integrator Tooling

[back to toc](#table-of-content)

# Monitor

## Download
**MySQL Server**:
1. Start Docker Desktop
2. Navigate to ```https://hub.docker.com/_/mysql``` to access documentation
3. Run ```docker pull mysql:latest```

**MySQLWorkbench**:
1. Navigate to ```https://dev.mysql.com/downloads/workbench/```
2. Follow instructions and install

## Run

1. **MySQL Server**:
    * Start Docker Desktop
    * Run ```docker run -p 3306:3306 --name some-mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw mysql:latest```
2. **MySQLWorkbench**:
    * Open MySQL Workbench
    * Connect to your running database instance
    * Open the file ```mysql/configure.sql``` and click Execute
3. Start the **Streaming Integrator Tooling** by issuing one of the following commands from the <SIT_HOME>/bin directory.
    * For Windows: ```streaming-integrator-tooling.bat```
    * For Linux: ```./streaming-integrator-tooling.sh```
4. Start **Elasticsearch** and **Kibana** by running docker-compose.
    * ```cd elasticsearch-kibana```
    * ```docker-compose up -d```

## Open
Access UIs:

1. **Streaming Integrator Tooling** via [http://localhost:9390/editor](http://localhost:9390/editor).
2. **Kibana** via [http://localhost:5601/](http://localhost:5601/).

## App

Configure **Kibana**:

1.  Create an index:
    ```
    curl --location --request PUT 'http://localhost:9200/frauds' \
    --header 'Content-Type: application/json' \
    --data-raw ''
    ```

2. Create a mapping:
    ```
    curl --location --request PUT 'http://localhost:9200/frauds/_mapping' \
    --header 'Content-Type: application/json' \
    --data-raw '{
        "properties": {
            "timestamp": {
                "type": "date",
                "format": "yyyy-MM-dd HH:mm:ss"
            },
            "creditCardNo": {
                "type": "keyword"
            },
            "suspiciousTrader": {
                "type": "keyword"
            },
            "coordinates": {
                "type": "geo_point"
            },
            "amount": {
                "type": "double"
            },
            "currency": {
                "type": "keyword"
            }
        }
    }'
    ```
3. Import the Dashboard and visualization
    * Click on Manage
    * Click on Kibana->Saved Objects
    * Click on Import and select the file ```/kibana/export.ndjson```

Open **Streaming Integrator Tooling** and select **New** from the menu then copy and paste the code below into the editor and save it as 'CSVFileExternalTime'.

```
@App:name("EnrichedSuspiciousTrade")
@App:description('')

@sink(type='log')
define stream SuspiciousTradeStream(creditCardNo string, traderId string, amount double, currency string);

@Store(type="elasticsearch", hostname="localhost", username="elastic", password="changeme", port='9200', index.name = 'frauds', index.type='_doc') 
@sink(type='log')
define table PersistenceTable (timestamp string, creditCardNo string, suspiciousTrader string, coordinates string, amount double, currency string);

@Store(type="rdbms",
       jdbc.url="jdbc:mysql://localhost:3306/traderDB?useSSL=false",
       username="root",
       password="my-secret-pw" ,
       jdbc.driver.name="com.mysql.jdbc.Driver")
@PrimaryKey("traderId")
define table TraderTable (traderId string, trader string, coord string);

@info(name='EnrichSuspiciousTrade')
from SuspiciousTradeStream as st join TraderTable as tt
    on st.traderId == tt.traderId
select 
    time:dateFormat(eventTimestamp(), 'yyyy-MM-dd hh:mm:ss') as timestamp,  
--    time:currentTimestamp() as timestamp, 
    st.creditCardNo, 
    tt.trader as suspiciousTrader, 
    tt.coord as coordinates, 
    st.amount, 
    st.currency
insert into PersistenceTable;
```
## Execute
From the **Streaming Integrator Tooling** menu click **Run**.

## Simulate
Configure random event simulation as follows:
1. Click on **Event Simulator** (double arrows on left tab)
2. Click **Feed Simulation -> Create**
3. Give a name (Or the default name in the place holder will be used as its name)
4. Select **Random** as the Simulation Source
5. Click on **Add Simulation Source**
6. Select **EnrichedSuspiciousTrade** as Siddhi App Name
7. Select **SuspiciousTradeStream** as StreamName
8. For **creditCardNo** change config type to **Regex based** and give the pattern as **143-90099-2343[0-9]{1}**.
9. For **traderId** change config type to **Regex based** and give the pattern as **(1-234|1-433|1-767|1-167|1-267|1-367|1-467|1-567)**.
9. Keep **Primitive Based** as the config type for **amount** but change from **100** to less than **1000** with **1** decimal.
10. Save the simulator configuration
11. The newly created simulator would be listed under **Active Feed Simulations** of **Feed Simulation** tab
12. Click on the **start** button (Arrow symbol) next to the newly created simulator
 
## View
Open a **Kibana Dashboard** and monitor the result.

![kibana](/img/kibana.png)

## Stop
Stop the Feed Simulation, stop the Siddhi app and press ctrl-C in the terminal where you started Streaming Integrator Tooling. In a terminal window type in ```docker-compose down -v``` to stop Elasticsearch and Kibana.

[back to toc](#table-of-content)

# Rules

## Download
Download the **WSO2 Streaming Integrator Server** ZIP-archive distribution from [WSO2 Streaming Integrator site](https://wso2.com/integration/streaming-integrator/) (click **Download** button) and extract it to a location of your choice. Hereafter, the extracted location is referred to as <SI_HOME>.

## Run
Start the **Streaming Integrator Server** by issuing one of the following commands from the <SI_HOME>/bin directory.

* For Windows: ```server.bat --run```
* For Linux: ```./server.sh```

Start the **Streaming Integrator Tooling** by issuing one of the following commands from the <SIT_HOME>/bin directory.

* For Windows: ```streaming-integrator-tooling.bat```
* For Linux: ```./streaming-integrator-tooling.sh```

# Open
Access UIs:

1. **Business Rules Template Editor** via [http://192.168.86.23:9390/template-editor](http://localhost:9390/template-editor)
2. **Business Rules Manager** via https://localhost:9743/business-rules (admin/admin)

## App
In the **Business Rules Template Editor** :

1. Enter **fraud-template-group** as UUID.
2. Enter **Fraud Template Group** as Name.
3. Click the Plus sign.
4. Expand the Rule Template section.
5. Enter **fraud-template** as UUID.
6. Enter **Fraud Template** as Name.
7. Select **Template** as Type.
8. Select **Many** as Instance Count.
9. Expand the Siddhi App section.
10. Copy and paste what is below:
    ```
    @App:name("FraudulentCardUse")
    @App:description("Detect fraudulent use of a credit card.")

    @source(type='kafka',
            topic.list='trade-stream',
            partition.no.list='0',
            threading.option='single.thread',
            group.id="group",
            bootstrap.servers='localhost:9092',
            @map(type='json'))
    define stream TradeStream(creditCardNo string, amount double);

    @sink(type='kafka',
        topic='suspicious-trade-stream',
        bootstrap.servers='localhost:9092',
        partition.no='0',
        @map(type='json'))
    @sink(type='log')
    define stream SuspiciousTradeStream(creditCardNo string, totalAmount double, totalCount long);

    @info(name='SuspiciousTrade')
    from TradeStream#window.time(${window} sec)
    select 
        creditCardNo, 
        sum(amount) as totalAmount,
        count(creditCardNo) as totalCount
    group by creditCardNo 
    having totalAmount > ${totalAmount} and totalCount > ${totalCount}
    insert into SuspiciousTradeStream;
    ```
11. Click **Generate** button.
12. Expand **window**
    * Enter **Time Window** as Field Name
    * Enter **Time Window in seconds** as Field Description
    * Enter **20** as Default Value
13. Expand **totalAmount**
    * Enter **Total Amount** as Field Name
    * Enter **Total Amount (greater than)** as Field Description
    * Enter **1000.0** as Default Value
14. Expand **totalCount**
    * Enter **Total Count** as Field Name
    * Enter **Total Count (greater than)** as Field Description
    * Enter **3** as Default Value
15. Click Save icon in the upp right corner.
16. Copy the saved .json file to the template folder:
    ```<WSO2_SI>/wso2/server/resources/businessRules/templates/[filename].json```
17. Restart the **WSO2 Streaming Integrator**

In the **Business Rules Manager** :

1. Click the plus icon.
2. Click **From Template**
3. Click **Fraud Template Group**
4. Select **Fraud Template**
5. Enter **FraudulentCardUse** as Business rule name
6.

## Simulate

1. Navigate to <KAFKA_HOME> directory and issue the following command:
    ```bin/kafka-console-producer.sh --broker-list localhost:9092 --topic trade-stream```
2. Paste in the following event a number of times:
    ```[{"event": {"creditCardNo":"143-90099-23431", "amount":5000.0}}]```

Now change the business rule configuration:
1. Open **Business Rules Manager**
2. Click on the pen to edit
3. Change the **Total Count** from 3 to 1
4. Run the simulation again

## View
The expected outcome in the **suspicious-trade-stream** topic:
```
{"event":{"creditCardNo":"143-90099-23431","totalAmount":12000.0,"totalCount":3}}
```

## Stop

Stop the running services:
1. Stop the Siddhi app and press ctrl-C in the terminal where you started Streaming Integrator Tooling
2. Press ctrl-C in the terminals running Kafka producer and consumer
3. Press ctrl-C in the terminals running Kafka and Zookeeper (in that order)

[back to toc](#table-of-content)

