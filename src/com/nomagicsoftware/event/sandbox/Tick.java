package com.nomagicsoftware.event.sandbox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import static java.time.temporal.ChronoField.*;
import java.util.Comparator;

import java.util.Objects;

/**
 *
 * @author thurston
 */
public class Tick implements Comparable<Tick>
{
    static final Comparator<Tick> TIMESTAMP_COMPARATOR = Comparator.nullsLast(Comparator.comparing(Tick::getTimestamp).
                                                                              thenComparingLong(Tick::getSequence));
    static final DateTimeFormatter DATE_TIME_FORMATTER;
    static
    {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.append(DateTimeFormatter.ISO_DATE).appendLiteral(' ');
        builder.appendValue(HOUR_OF_DAY, 2).appendLiteral(':').appendValue(MINUTE_OF_HOUR, 2);
        builder.appendLiteral(':').appendValue(SECOND_OF_MINUTE, 2).appendFraction(NANO_OF_SECOND, 
                                                                                   3, 3, true);
        //builder.appendLiteral(' ');
        DATE_TIME_FORMATTER = builder.toFormatter();
    }
    
    private final LocalDateTime timestamp;
    private final long sequence;

    public Tick(LocalDateTime timestamp, long sequence)
    {
        this.timestamp = timestamp;
        this.sequence = sequence;
    }

    public LocalDateTime getTimestamp()
    {
        return this.timestamp;
    }

    public long getSequence()
    {
        return this.sequence;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.timestamp);
        hash = 41 * hash + (int) (this.sequence ^ (this.sequence >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Tick other = (Tick) obj;
        if (this.sequence != other.sequence)
            return false;
        if (! Objects.equals(this.timestamp, other.timestamp))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("Tick{timestamp = ");
        builder.append(this.timestamp.format(DATE_TIME_FORMATTER)).append(',');
        builder.append(" sequence = ").append(String.format("%,d", this.sequence)).append('}');
        //"Tick{" + "timestamp=" + timestamp + ", sequence=" + this.sequence + '}';
        return builder.toString();
    }

    @Override
    public int compareTo(Tick o)
    {
//        Comparator<Tick> nullsLast = Comparator.comparing(Tick::getTimestamp).thenComparingLong(Tick::getSequence);
//        nullsLast.thenComparingLong(Tick::getSequence);
        return TIMESTAMP_COMPARATOR.compare(this, o);
    }
    
    
    
    
}
