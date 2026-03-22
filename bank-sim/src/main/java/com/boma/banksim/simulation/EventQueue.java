package com.boma.banksim.simulation;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * A time-ordered queue of simulation events. Events are processed when the
 * simulation clock reaches or passes their scheduled date.
 */
public class EventQueue {

    private record ScheduledEvent(LocalDate date, String description, Runnable action)
            implements Comparable<ScheduledEvent> {
        @Override
        public int compareTo(ScheduledEvent other) {
            return this.date.compareTo(other.date);
        }
    }

    private final PriorityQueue<ScheduledEvent> queue;

    public EventQueue() {
        this.queue = new PriorityQueue<>();
    }

    /** Schedules an action to execute on or after the given date. */
    public void schedule(LocalDate date, String description, Runnable action) {
        if (date == null) throw new IllegalArgumentException("Date cannot be null.");
        if (action == null) throw new IllegalArgumentException("Action cannot be null.");
        queue.add(new ScheduledEvent(date, description, action));
    }

    /**
     * Executes all events whose scheduled date is on or before {@code upTo}.
     * Events are executed in chronological order.
     */
    public int processUpTo(LocalDate upTo) {
        int processed = 0;
        while (!queue.isEmpty() && !queue.peek().date().isAfter(upTo)) {
            ScheduledEvent event = queue.poll();
            event.action().run();
            processed++;
        }
        return processed;
    }

    public boolean hasEvents() {
        return !queue.isEmpty();
    }

    public int pendingCount() {
        return queue.size();
    }

    public LocalDate nextEventDate() {
        if (queue.isEmpty()) return null;
        return queue.peek().date();
    }
}
