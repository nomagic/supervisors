package com.nomagicsoftware.event;

import java.util.function.Consumer;

/**
 * A service that is supervised by a supervisor, and guarantees to emit its death state before
 * dying
 * @author thurston
 * @param <T>
 */
public interface SupervisedService<T extends DeathState> 
{
    void stop();
    
    /**
     * Unless expressly otherwise, implementations should assume that the callback is executed
     * sequentially in this service's runloop thread<br>
     * {@code callback} Should only be executed *once*, and by *this*
     * @param callback
     */
    void setDeathCallback(Consumer<? super T> callback);
    
    /**
     * this really is "friend"-scoped, as death callbacks are designed to only be invoked by
     * {@link SupervisedService services} themselves <br>
     * But it's very useful when building generic, flexible supervisors, that they can compose
     * new death-callbacks from existing ones
     * @return this service's death-callback or null if none has been set
     * @see Link
     */
    default Consumer<T> getDeathCallback()
    {
        return null;
    }
}
