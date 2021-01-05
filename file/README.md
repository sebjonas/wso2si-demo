# File
This sample demonstrates how to use external timestamps in a time window for a fraud detection use-case. In this sample, we look for two or more transactions done within a very short time period and send an alert immediately when such an occurrence is detected.

## Run

Start the **Streaming Integrator Tooling** by issuing one of the following commands from the <SIT_HOME>/bin directory.

* For Windows: ```.\tooling.bat```
* For Linux: ```./tooling.sh```

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
8. Click on Upload and select the '/file/events.csv'.
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

[back to toc](../README.md)