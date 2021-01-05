# RDMS

Let's join the trade stream with data in a database table. This application demonstrates how to perform join on streaming data with the data stored in RDBMS. The sample depicts a scenario, where a transaction by a credit card with which fraudulent activity has been previously done. The credit card numbers, which were noted for fraudulent activities are stored in an RDBMS table.

## Download
**MySQL Server**:
1. Start Docker Desktop
2. Navigate to ```https://hub.docker.com/_/mysql``` to access documentation
3. Run ```docker pull mysql:latest```
    * To download images you have to change network from CORP1 due to restrictions.

**MySQLWorkbench**:
1. Navigate to ```https://dev.mysql.com/downloads/workbench/```
2. Follow instructions and install

## Run
Let's start the MySQL server, create a db, a schema and insert some rows to join with the stream.

**MySQL Server**:
1. Start Docker Desktop
2. Run ```docker run -p 3306:3306 --name some-mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw mysql:latest mysqld --default-authentication-plugin=mysql_native_password```

**MySQL Driver**
1. Download the MySQL JDBC driver from the [MySQL site](https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.45.tar.gz).
2. Unzip the archive.
3. Copy the ```mysql-connector-java-5.1.45-bin.jar``` to the <SIT_HOME>/lib directory.

**MySQLWorkbench**:
1. Open MySQL Workbench
2. Connect to your running database instance
3. Open the file ```mysql/configure.sql``` and click Execute

**Streaming Integrator Tooling**:

* For Windows: ```.\tooling.bat```
* For Linux: ```./tooling.sh```

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

If the **Streaming Integrator Tooling** is missing the MySQL extension, follow this:
1. Click Tools->Extension Installer
2. Type in MySQL
3. Click Install button and then confirm
4. Close dialog and Restart **Streaming Integrator Tooling**

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

[back to toc](../README.md#table-of-content)