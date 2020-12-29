# Rules
The Business Rules Manager allows you to define templates and from them generate business rules that can easily be modified by the business.

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
{"event":{"creditCardNo":"143-90099-23431","totalAmount":12000.0,"totalCount":2}}
```

## Stop

Stop the running services:
1. Stop the Siddhi app and press ctrl-C in the terminal where you started Streaming Integrator Tooling
2. Press ctrl-C in the terminals running Kafka producer and consumer
3. Press ctrl-C in the terminals running Kafka and Zookeeper (in that order)

[back to toc](../README.md)

