package com.nomagicsoftware.event;

/**
 *
 * @author thurston
 * @param <T>
 */
public interface Sink<T> 
{
    void emit(T event);
}
