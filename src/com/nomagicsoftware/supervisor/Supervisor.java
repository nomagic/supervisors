package com.nomagicsoftware.supervisor;

/**
 *
 * @author thurston
 */
public interface Supervisor 
{
    void start();
    
    void stop();
    
    void await();
    
    /**
     *
     * @param service
     * @return an {@link Thread daemon}, not started
     */
    static Thread daemonThread(Runnable service)
    {
        Thread thread = new Thread(service, service.getClass().getCanonicalName());
        thread.setDaemon(true);
        return thread;
    }

}
