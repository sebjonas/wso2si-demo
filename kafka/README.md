# Kafka
Let's add Kafka as source and sink for the **TradeStream** and the **SuspiciousTradeStream**.

## Download
Download the Kafka broker from the [Apache site](https://www.apache.org/dyn/closer.cgi?path=/kafka/2.3.0/kafka_2.12-2.3.0.tgz) and extract it. This directory is referred to as <KAFKA_HOME> from here on.

## Run
Let's start the Kafka server and create a Kafka topic so that the **FraudulentCardUse** Siddhi application you created can subscribe and publish its output to it.

To start Kafka:
1. Navigate to the <KAFKA_HOME> directory and start a zookeeper node by issuing the following command.
    * For Windows: ```.\bin\windows\zookeeper-server-start.bat config\zookeeper.properties```
    * For Linux: ```sh bin/zookeeper-server-start.sh config/zookeeper.properties```
2. Navigate to the <KAFKA_HOME> directory and start Kafka server node by issuing the following command.
    * For Windows: ```.\bin\windows\kafka-server-start.bat config\server.properties```
    * For Linux: ```sh bin/kafka-server-start.sh config/server.properties```

To create a Kafka topic named **trade-stream** and another named **suspicious-trade-stream**:
1. Navigate to <KAFKA_HOME> directory and issue the following command:
    * For Windows: ```.\bin\windows\kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic trade-stream```
    * For Linux: ```bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic trade-stream```
2. Navigate to <KAFKA_HOME> directory and issue the following command:
    * For Windows: ```.\bin\windows\kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic suspicious-trade-stream```
    * For Linux: ```bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic suspicious-trade-stream```

Start the **Streaming Integrator Tooling** by issuing one of the following commands from the <SIT_HOME>/bin directory.

* For Windows: ```.\tooling.bat```
* For Linux: ```./tooling.sh```

## App
Open **Streaming Integrator Tooling** and select **New** from the menu then copy and paste the code below into the editor and save it as **FraudulentCardUse**.

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
having totalAmount > 10000.0 and totalCount > 2
insert into SuspiciousTradeStream;
```

## Execute
If the **Streaming Integrator Tooling** is missing the Kafka extension, follow this:
1. Click Tools->Extension Installer
2. Type in Kafka
3. Click Install button and then confirm
4. Close dialog and Restart **Streaming Integrator Tooling**

From the **Streaming Integrator Tooling** menu click **Run**.

## Consume
1. Navigate to <KAFKA_HOME> directory and issue the following command:
    * For Windows: ```.\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic suspicious-trade-stream```
    * For Linux: ```bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic suspicious-trade-stream```

## Simulate

1. Navigate to <KAFKA_HOME> directory and issue the following command:
    * For Windows: ```.\bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic trade-stream```
    * For Linux: ```bin/kafka-console-producer.sh --broker-list localhost:9092 --topic trade-stream```
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

[back to toc](../README.md#table-of-content)
