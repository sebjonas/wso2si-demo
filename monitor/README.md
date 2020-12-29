# Monitor
Elasticsearch sink implementation uses Elasticsearch indexing document for underlying data storage. The events that are published from the sink will be converted into elasticsearch index documents.

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

## Extra

If you would like to simulate data using Postman:

1. Add a new POST request for ```http://localhost:9200/frauds/_doc```
2. Define the body:
    ```
    {
        "timestamp": "{{timestamp}}",
        "creditCardNo": "{{creditCardNo}}",
        "suspiciousTrader": "{{suspiciousTrader}}",
        "coordinates": "{{coordinates}}",
        "amount": "{{amount}}",
        "currency": "{{currency}}"
    }
    ```
3. Define the pre-request script:
    ```
    var moment = require('moment');
    var d = new Date(_.random(2020,2020), _.random(0,11), _.random(1,28), _.random(1,12), _.random(1,60), _.random(1,60));

    var month1 = ("0" + (d.getMonth() + 1)).slice(-2); 
    var date1 = ("0" + d.getDate()).slice(-2); 
    var hour1 = ("0" + d.getHours()).slice(-2); 
    var minute1 = ("0" + d.getMinutes()).slice(-2); 
    var second1 = ("0" + d.getSeconds()).slice(-2); 

    var timestamp = d.getFullYear()  + "-" + month1 + "-" + date1 + " " + hour1 + ":" + minute1 + ":" + second1;

    pm.environment.set('timestamp', timestamp);

    var cards = ["143-90099-23431", 
                "143-90099-23432", 
                "143-90099-23433", 
                "143-90099-23434",
                "143-90099-23435"];
    pm.environment.set("creditCardNo", cards[_.random(0,4)]);

    var traders = ["1-234", 
                "1-433",
                "1-767",
                "1-167",
                "1-267",
                "1-367",
                "1-467",
                "1-567"];
    pm.environment.set("suspiciousTrader", traders[_.random(0,7)]);

    pm.sendRequest({
        url: "https://api.3geonames.org/?randomland=yes&json=1",
        method: "GET",
        header: {
            "content-type": "application/json",
            "accept": "application/json"
        },
        body: {}
    }, function (err, res) {
        console.log(res.json().nearest.latt + "," + res.json().nearest.longt);
        pm.environment.set("coordinates", res.json().nearest.latt + "," + res.json().nearest.longt);
    });

    pm.environment.set('amount', _.random(5,50)*100.0);

    var currencies = ["USD", "SEK", "CHF", "EUR", "CNY"];
    pm.environment.set("currency", currencies[_.random(0,4)]);
    ```
4. Use Runner to execute a few iteration.

## View
Open a **Kibana Dashboard** and monitor the result.

![kibana](/img/kibana.png)

Run this command if you would like to delete all data: 
```
curl --location --request POST 'http://localhost:9200/frauds/_delete_by_query' \
--header 'Content-Type: application/json' \
--data-raw '{
  "query": {
    "match_all":{}
  }
}'
```

## Stop
Stop the Feed Simulation, stop the Siddhi app and press ctrl-C in the terminal where you started Streaming Integrator Tooling. In a terminal window type in ```docker-compose down -v``` to stop Elasticsearch and Kibana.

[back to toc](../README.md)