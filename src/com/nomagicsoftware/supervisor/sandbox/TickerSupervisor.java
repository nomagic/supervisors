package com.nomagicsoftware.supervisor.sandbox;

import com.nomagicsoftware.event.Sink;
import com.nomagicsoftware.event.sandbox.SimpleTickerService;
import com.nomagicsoftware.event.sandbox.SimpleTickerService.TickerDeath;
import com.nomagicsoftware.event.sandbox.Tick;
import com.nomagicsoftware.supervisor.Supervisor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thurston
 */
public class TickerSupervisor implements Supervisor
{
    final long delay;
    Sink<? super Tick> sink = ignore -> { };
    volatile SimpleTickerService ticker;
    volatile Thread tickerLWP;
    volatile CompletableFuture<TickerDeath> cf = new CompletableFuture<>();
    volatile boolean started;
    
    public TickerSupervisor(long delay)
    {
        this.delay = delay;
        this.ticker = new SimpleTickerService(delay);
    }
    
    public TickerSupervisor(long delay, Sink<? super Tick> sink)
    {
        this.delay = delay;
        SimpleTickerService _ticker = new SimpleTickerService(delay);
        _ticker.register(sink);
        this.ticker = _ticker;
        this.sink = sink;
    }

    @Override
    public void start()
    {
        Thread daemonThread = Supervisor.daemonThread(this.ticker);
        
        //this.ticker.setDeathCallback(tdeath -> System.err.println(tdeath));
        this.ticker.setDeathCallback(tdeath -> this.cf.complete(tdeath));
        daemonThread.start();
        this.tickerLWP = daemonThread;
        this.started = true;
    }

    @Override
    public void stop()
    {
        this.tickerLWP.interrupt();
        this.ticker.stop();
        this.started = false;
        //this.cf.cancel(true);
    }

    void restart(TickerDeath death)
    {
        SimpleTickerService newTicker = new SimpleTickerService(this.delay);
        Thread daemonThread = Supervisor.daemonThread(newTicker);
        this.cf = new CompletableFuture<>();
        newTicker.setDeathCallback(tdeath -> this.cf.complete(tdeath));
        this.ticker = newTicker;
        this.tickerLWP = daemonThread;
        newTicker.register(this.sink);
        daemonThread.start();
        
    }
    
    
    @Override
    public void await()
    {
        waitLoop();
    }
    
    /**
     * wait until this supervisor is explicitly stopped, blocking current thread
     */
    void waitLoop()
    {
        for (; this.started ; )
        {
            try
            {
                TickerDeath state = this.cf.get();
                /*
                  If here, then the ticker has died, with its state defined by #state:
                  check #started, as we don't want to start a new ticker, if the supervisor has been stopped
                */
                if (this.started) //comeon, this is a race condition
                {
                    restart(state);
                }
            }
            catch (InterruptedException ex)
            {

            }
            catch (ExecutionException | CompletionException ex)
            {
                System.err.println("was cancelled by " + ex);
            }
            catch (Throwable unexpected)
            {
                
            }
        }
    }
    
    void deathCallback(TickerDeath death)
    {
        
    }

}
