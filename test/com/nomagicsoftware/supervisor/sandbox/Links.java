package com.nomagicsoftware.supervisor.sandbox;

import com.nomagicsoftware.supervisor.Link;
import com.nomagicsoftware.event.DeathState;
import com.nomagicsoftware.event.SupervisedService;
import com.nomagicsoftware.function.Functions;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;
import static org.junit.Assert.*;
import org.junit.Test;
/**
 *
 * @author thurston
 */
public class Links 
{
    
    public void foo()
    {
        CompletableFuture<Void> link = new CompletableFuture<>();
        
        
        link.handle((BiFunction<Void, Throwable, Void>) (a, b) -> { System.err.println("callback executed"); return null;});
        link.complete(null);
        link.complete(null); // not a problem
        
        
    }
    
    @Deprecated
    public void singleService() throws Exception
    {
        MockSupervisedService service = new MockSupervisedService();
        CompletableFuture<Boolean> finito = new CompletableFuture<>();
        service.setDeathCallback(ignore -> finito.complete(true));
        List<SupervisedService> services = Arrays.asList(service);
        
        CompletableFuture<Void> allStopped = finito.thenAcceptAsync(ignore -> 
                
                    services.forEach(SupervisedService::stop)
                );
        service.stop();
        allStopped.get();
        assertTrue("", allStopped.isDone());
    }
    
    
    
    @Test
    public void unmanagedLink() throws Exception
    {
        /*
            so each TDC should do the following:
            call all.stop()
            if (AI.incrementAndGet == services.length)
               cf.complete(services.length)
        */
        
        final Link link = new Link(2);
        //we'll assume two services
        final SupervisedService<?>[] services = new SupervisedService[2];
        
        //final AtomicInteger count = new AtomicInteger();
        
        //Arrays.stream(services).forEach(SupervisedService::stop);
        services[0] = new MockSupervisedService();
        services[1] = new MockSupervisedService();
        
        for (SupervisedService<?> service : services)
        {
            service.setDeathCallback((DeathState _ds) -> 
            {
                //for now we ignore the death-state since we're only implementing link
                System.err.println(String.format("In death callback on thread [%s]", Thread.currentThread()));
                //yes, we call stop() on ourselves, but that shouldn't be a problem?
                Arrays.spliterator(services).forEachRemaining(SupervisedService::stop);
                link.update();
            });
        }
        Thread[] threads = new Thread[] { start((Runnable) services[0]),  start((Runnable) services[1]) };
        
        Thread.sleep(1_000L);
        services[1].stop(); //0 or 1 don't matter
        
        
        Integer done = link.get();
        //threads may not be dead yet, but they should die soon
        for (Thread thread : threads)
        {
            thread.join(10L);
            assertFalse("", thread.isAlive());
        }
        
        
        
    }
    
    @Test
    public void managedLink() throws Exception
    {
        
        //we'll assume two services
        final SupervisedService<?>[] services = new SupervisedService[2];
        services[0] = new MockSupervisedService();
        services[1] = new MockSupervisedService();
        final Link link = new Link(services);
        Thread[] threads = new Thread[] { start((Runnable) services[0]),  start((Runnable) services[1]) };
        
        Thread.sleep(3_000L);
        services[0].stop(); //0 or 1 don't matter
        
        
        Integer done = link.get();
        assertEquals("", 0, (int) done);
        
        //Executor sameThread = Runnable::run;
        //Timer t;
        
    }
    
    static Thread start(Runnable service)
    {
        Thread thread = new Thread(service, service.toString());
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
    
    static class MockSupervisedService implements Runnable, SupervisedService<DeathState>
    {
        volatile Consumer<DeathState> tdc;
        volatile boolean shutdown;

        @Override
        public void stop()
        {
//            if (! this.shutdown) 
//            {
//                this.shutdown = true; //crucial to avoid infinite loop
//                
//            }
            this.shutdown = true;
            
        }

        @Override
        @SuppressWarnings("unchecked")
        public void setDeathCallback(Consumer<? super DeathState> callback)
        {
            this.tdc = (Consumer<DeathState>) callback;
        }

        @Override
        public void run()
        {
            while (! this.shutdown)
                Thread.yield();
            this.tdc.accept(() -> "anonymous service");
            this.tdc = Functions.noopConsumer();
        }
        
    }
    
    abstract static class NoState implements DeathState
    {
        
        
    };
}
