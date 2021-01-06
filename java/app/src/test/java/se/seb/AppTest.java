package se.seb;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.stream.output.StreamCallback;
import io.siddhi.core.util.EventPrinter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit test for simple App.
 */
public class AppTest 
{

    @Test
    public void filterTest() throws InterruptedException {

        final AtomicInteger outputs = new AtomicInteger();

        // Create Siddhi Manager
        SiddhiManager siddhiManager = new SiddhiManager();

        //Siddhi Application
        String siddhiApp = "" +
                "define stream TradeStream (creditCardNo string, amount float); " +
                "" +
                "@info(name = 'SuspiciousTrade') " +
                "from TradeStream[amount > 2000] " +
                "select creditCardNo, amount " +
                "insert into SuspiciousTradeStream;";

        //Generate runtime
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        //Adding callback to retrieve output events from stream
        siddhiAppRuntime.addCallback("SuspiciousTradeStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                //Count number of outputs
                outputs.addAndGet(events.length);

                for (Event event : events) {
                    //Assert output
                    Assert.assertTrue((Float) event.getData(1) > 2000);
                }
            }
        });

        //Get InputHandler to push events into Siddhi
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("TradeStream");

        //Start processing
        siddhiAppRuntime.start();

        //Sending events to Siddhi
        inputHandler.send(new Object[]{"143-90099-23431", 500f});
        inputHandler.send(new Object[]{"143-90099-23431", 1500f});
        inputHandler.send(new Object[]{"143-90099-23431", 2500f});
        Thread.sleep(500);

        //Shutdown runtime
        siddhiAppRuntime.shutdown();

        //Shutdown Siddhi Manager
        siddhiManager.shutdown();

        //Assert output event count
        Assert.assertEquals(1, outputs.get());

    }
}
