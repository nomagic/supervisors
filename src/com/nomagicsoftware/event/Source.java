package com.nomagicsoftware.event;

/**
 *
 * @author thurston
 * @param <T> the type of event
 */
public interface Source<T>
{

    void register(Sink<? super T> sink);

    default void register(Iterable<? extends Sink<? super T>> sinks)
    {
        for (Sink<? super T> sink : sinks)
            register(sink);
    }

    void deregister(Sink<? super T> sink);

    /**
     * deregisters all registered sinks
     */
    void clear();

}
