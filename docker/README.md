# Steps to Run Siddhi Docker Microservice

1. Pull the the latest Siddhi Runner image from Siddhiio Docker Hub.
    ```docker pull siddhiio/siddhi-runner-alpine:latest```
2. Start SiddhiApps with the runner config by executing the following docker command.

    For Windows:
    
    ```docker run -it -p 8008:8008 -v <repo-local-path>\docker\siddhi-apps:/apps -v repo-local-path\docker\siddhi-configs:/configs siddhiio/siddhi-runner-alpine:latest -Dapps=/apps/FraudulentCardUse.siddhi```

    For Linux:
    
    ```docker run -it -p 8008:8008 -v <repo-local-path>/docker/siddhi-apps:/apps -v repo-local-path/docker/siddhi-configs:/configs siddhiio/siddhi-runner-alpine:latest -Dapps=/apps/FraudulentCardUse.siddhi```    

## Simulate

Use REST endpoint at port 8008 to send in events to the Siddhi app.
1. Open a terminal window.
2. Send in a couple of events.
    For Windows: 
    ```
    Invoke-WebRequest -Uri http://localhost:8008/tradeStream -Method POST -Body '{"event": {"creditCardNo": "143-90099-23431","amount": 3000.0}}'
    ```
    For Linux: 
    ```
    curl --location --request POST 'http://localhost:8008/tradeStream' --header 'Content-Type: application/json' --data-raw '{"event": {"creditCardNo":"143-90099-23431","amount": 3000.0}}'
    ```

## View
Logging will be done in the terminal running docker.

## Stop

Stop the running services:
1. Press ctrl-C in the terminals running docker container.

[back to toc](../README.md#table-of-content)