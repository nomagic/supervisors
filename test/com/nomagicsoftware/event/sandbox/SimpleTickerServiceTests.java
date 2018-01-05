package com.nomagicsoftware.event.sandbox;

import com.nomagicsoftware.event.SupervisedService;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import org.junit.Test;
/**
 *
 * @author thurston
 */
public class SimpleTickerServiceTests 
{
    //@Test
    public void haveToTerminate()
    {
        SimpleTickerService ticker = new SimpleTickerService(450L);
        ticker.register(t -> System.err.println(t));
        ticker.setDeathCallback(td -> System.err.println(td));
        ticker.runLoop();
        
    }
    
    @Test
    public void runAwhile() throws Exception
    {
        SimpleTickerService ticker = new SimpleTickerService(1_500L);
        ticker.register(t -> System.err.println(t));
        ticker.setDeathCallback(td -> System.err.println(td));
        
        Thread stopper = new Thread(tickerStopper(ticker, 5_500L), "service stopper");
        stopper.setDaemon(true);
        stopper.start();
        
        ticker.runLoop();
    }
    
    
    @Test
    public void runInOwnThread() throws Exception
    {
        SimpleTickerService ticker = new SimpleTickerService(1_500L);
        ticker.register(t -> System.err.println(t));
        ticker.setDeathCallback(td -> System.err.println(td));
        
        Thread lwp = new Thread(ticker, "Ticker-Thread");
        lwp.setDaemon(true);
        lwp.start();
        
        tickerStopper(ticker, 8_000L).run();
        lwp.join();
        
    }
    
    @Test
    public void stopByInterruption() throws Exception
    {
        final Object[] tuple = new Object[1];
        
        SimpleTickerService ticker = new SimpleTickerService(10L);
        ticker.register(t -> System.err.println(t));
        ticker.setDeathCallback(td -> tuple[0] = td );
        
        Thread lwp = startService(ticker);
        lwp.interrupt();
        lwp.join();
        assertNotNull("", tuple[0]);
    }
    
    @Test
    public void stop() throws Exception
    {
        final Object[] tuple = new Object[1];
        
        SimpleTickerService ticker = new SimpleTickerService(900L);
        ticker.register(t -> System.err.println(t));
        ticker.setDeathCallback(td -> tuple[0] = td );
        
        Thread lwp = startService(ticker);
        //let it run awhile
        lwp.join(17_500L);
        ticker.stop();
        lwp.join();
        assertNotNull("", tuple[0]);
    }
    
    
    static Thread startService(Runnable service)
    {
        Thread lwp = new Thread(service, service.getClass().getCanonicalName());
        lwp.setDaemon(true);
        lwp.start();
        return lwp;
    }
    
    
    
    static Runnable tickerStopper(SupervisedService service, long delay)
    {
        return () -> 
        {
            try
            {
                Thread.sleep(delay);
                service.stop();
            }
            catch (InterruptedException ex)
            {
                
            }
        };
    }
}
