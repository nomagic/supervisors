package com.nomagicsoftware.event.sandbox;

import com.nomagicsoftware.event.DeathState;
import com.nomagicsoftware.event.Sink;
import com.nomagicsoftware.event.Source;
import com.nomagicsoftware.event.SupervisedService;
import com.nomagicsoftware.event.sandbox.SimpleTickerService.TickerDeath;
import com.nomagicsoftware.function.Functions;
import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 *
 * @author thurston
 */
public class SimpleTickerService implements Runnable, Source<Tick>, SupervisedService<TickerDeath>
{
    volatile boolean shutdown;

    volatile Consumer<? super TickerDeath> death = Functions.noopConsumer();
    
    final CopyOnWriteArrayList<Sink<? super Tick>> sinks = new CopyOnWriteArrayList<>();
    
    /*
       in milliseconds
    */
    final long period;

    public SimpleTickerService(long period)
    {
        this.period = period;
    }
    
    
    @Override
    public void stop()
    {
        this.shutdown = true;
    }

    @Override
    public void setDeathCallback(Consumer<? super TickerDeath> callback)
    {
        if (null == callback)
            callback = Functions.noopConsumer();
        this.death = callback;
    }

    @Override
    public void register(Sink<? super Tick> sink)
    {
        this.sinks.addIfAbsent(sink);
    }

    @Override
    public void deregister(Sink<? super Tick> sink)
    {
        this.sinks.remove(sink);
    }

    @Override
    public void clear()
    {
        this.sinks.clear();
    }
    
    void dying(Tick last)
    {
        System.err.println(Thread.currentThread());
        this.death.accept(new TickerDeath(last));
    }
    
    void runLoop()
    {
        
        long sequence = 0L;
        Tick last = null;
        
        try
        {
            for (; ! this.shutdown; sequence++, Thread.sleep(this.period))
            {
                Tick lambdaTick = last = new Tick(LocalDateTime.now(), sequence);
                this.sinks.forEach(sink -> sink.emit(lambdaTick));
                
            }
        }
        catch (Throwable ex)
        {
            System.err.println(ex);
        }
        finally
        {
            this.shutdown = true;
            dying(last);
        }
    }

    @Override
    public void run()
    {
        runLoop();
    }
    
    public static class TickerDeath implements DeathState
    {
        final Tick last;

        public TickerDeath(Tick last)
        {
            this.last = last;
        }

        
        @Override
        public String name()
        {
            return getClass().getName();
        }
        
        public Tick last()
        {
            return this.last;
        }

        @Override
        public String toString()
        {
            return "TickerDeath{" + "tick = " + this.last + '}';
        }
        
        
        
    }
}
