package com.nomagicsoftware.supervisor.sandbox;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import static org.junit.Assert.*;
import org.junit.Test;
/**
 *
 * @author thurston
 */
public class TickerSupervisorTests 
{
    @Test
    public void foo() throws Exception
    {
        TickerSupervisor supervisor = new TickerSupervisor(150L, tick -> System.err.println(tick));
        supervisor.start();
        Thread.sleep(5_000L);
        supervisor.stop();
        supervisor.await();
    }
    
    
    @Test
    public void runtimeTimeout() throws Exception
    {
        ScheduledExecutorService ses =  Executors.newScheduledThreadPool(0);
    }
}
