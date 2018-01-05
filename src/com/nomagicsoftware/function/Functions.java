package com.nomagicsoftware.function;

import java.util.function.Consumer;

/**
 *
 * @author thurston
 */
public abstract class Functions 
{

    private Functions()
    {
    }
    
    
    public static <T> Consumer<T> noopConsumer()
    {
        return t -> { };
    }
}
