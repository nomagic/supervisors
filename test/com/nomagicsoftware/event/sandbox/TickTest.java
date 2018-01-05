
package com.nomagicsoftware.event.sandbox;

import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author thurston
 */
public class TickTest
{
    
    

    @Test
    public void testToString()
    {
        System.err.println(new Tick(LocalDateTime.MIN, Long.MAX_VALUE));
        System.err.println(new Tick(LocalDateTime.now(), 0L));
    }
    @Test
    public void equality()
    {
        LocalDateTime base = LocalDateTime.now();
        Tick tick = new Tick(base, 0L);
        assertEquals("identity", tick, tick);
        assertEquals("", new Tick(base, 10L), new Tick(base, 10L));
        
        LocalDateTime copy = LocalDateTime.from(base);
        assertEquals("", new Tick(copy, 76L), new Tick(base, 76L));
        
        
    }
    
    @Test
    public void inequality()
    {
        LocalDateTime base = LocalDateTime.now();
        Tick tick = new Tick(base, 0L);
        assertNotEquals("", tick, null);
        assertNotEquals("", null, tick);
        assertNotEquals("", tick, new Tick(base, 10L));
        
        LocalDateTime copy = LocalDateTime.from(base);
        copy = copy.minus(Duration.ofMillis(1L));
        assertNotEquals("", tick, new Tick(copy, 0L));
        
        
    }
    
    @Test
    public void compare()
    {
        final Duration oneMs = Duration.ofMillis(1L);
        
        LocalDateTime base = LocalDateTime.now();
        Tick tick = new Tick(base, 0L);
        assertTrue("nulls go at end", tick.compareTo(null) < 0);
        assertEquals("", tick.compareTo(tick), 0);
        assertEquals("", tick.compareTo(new Tick(base, 0L)), 0);
        
        LocalDateTime copy = LocalDateTime.from(base);
        assertEquals("", tick.compareTo(new Tick(copy, 0L)), 0);
        assertEquals("", new Tick(copy, 0L).compareTo(tick), 0);
        
        assertTrue("", tick.compareTo(new Tick(copy.minus(oneMs), 0L)) > 0);
        assertTrue("", new Tick(copy.minus(oneMs), 0L).compareTo(tick) < 0);
        
        assertTrue("", tick.compareTo(new Tick(base, 1L)) < 0);
        assertTrue("", new Tick(base, 1L).compareTo(tick) > 0);
        
        assertTrue("", tick.compareTo(new Tick(copy.minus(oneMs), -1L)) > 0);
        
        //assertTrue("", tick.compareTo(new Tick(null, -1L)) > 0);
        
    }
}
