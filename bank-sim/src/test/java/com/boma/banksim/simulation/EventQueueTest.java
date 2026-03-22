package com.boma.banksim.simulation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EventQueueTest {

    private EventQueue queue;

    @BeforeEach
    void setUp() {
        queue = new EventQueue();
    }

    @Test
    void schedule_nullDate_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> queue.schedule(null, "test", () -> {}));
    }

    @Test
    void schedule_nullAction_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> queue.schedule(LocalDate.now(), "test", null));
    }

    @Test
    void hasEvents_emptyQueue_false() {
        assertFalse(queue.hasEvents());
    }

    @Test
    void hasEvents_afterSchedule_true() {
        queue.schedule(LocalDate.of(2024, 1, 1), "test", () -> {});
        assertTrue(queue.hasEvents());
    }

    @Test
    void pendingCount_incrementsWithSchedule() {
        queue.schedule(LocalDate.of(2024, 1, 1), "e1", () -> {});
        queue.schedule(LocalDate.of(2024, 2, 1), "e2", () -> {});
        assertEquals(2, queue.pendingCount());
    }

    @Test
    void processUpTo_executesEventOnExactDate() {
        boolean[] fired = {false};
        LocalDate jan15 = LocalDate.of(2024, 1, 15);
        queue.schedule(jan15, "test", () -> fired[0] = true);
        queue.processUpTo(jan15);
        assertTrue(fired[0]);
    }

    @Test
    void processUpTo_doesNotExecuteFutureEvent() {
        boolean[] fired = {false};
        LocalDate feb1 = LocalDate.of(2024, 2, 1);
        queue.schedule(feb1, "future", () -> fired[0] = true);
        queue.processUpTo(LocalDate.of(2024, 1, 31));
        assertFalse(fired[0]);
    }

    @Test
    void processUpTo_executesAllPastAndCurrentEvents() {
        List<String> executed = new ArrayList<>();
        queue.schedule(LocalDate.of(2024, 1, 1), "a", () -> executed.add("a"));
        queue.schedule(LocalDate.of(2024, 2, 1), "b", () -> executed.add("b"));
        queue.schedule(LocalDate.of(2024, 3, 1), "c", () -> executed.add("c")); // future

        queue.processUpTo(LocalDate.of(2024, 2, 1));
        assertEquals(2, executed.size());
        assertTrue(executed.contains("a") && executed.contains("b"));
    }

    @Test
    void processUpTo_executesInChronologicalOrder() {
        List<String> order = new ArrayList<>();
        queue.schedule(LocalDate.of(2024, 3, 1), "third", () -> order.add("third"));
        queue.schedule(LocalDate.of(2024, 1, 1), "first", () -> order.add("first"));
        queue.schedule(LocalDate.of(2024, 2, 1), "second", () -> order.add("second"));

        queue.processUpTo(LocalDate.of(2024, 12, 31));
        assertEquals(List.of("first", "second", "third"), order);
    }

    @Test
    void processUpTo_returnsCountExecuted() {
        queue.schedule(LocalDate.of(2024, 1, 1), "e1", () -> {});
        queue.schedule(LocalDate.of(2024, 2, 1), "e2", () -> {});
        int count = queue.processUpTo(LocalDate.of(2024, 2, 1));
        assertEquals(2, count);
    }

    @Test
    void processUpTo_removesExecutedEvents() {
        queue.schedule(LocalDate.of(2024, 1, 1), "e1", () -> {});
        queue.schedule(LocalDate.of(2024, 6, 1), "e2", () -> {});
        queue.processUpTo(LocalDate.of(2024, 1, 31));
        assertEquals(1, queue.pendingCount()); // e2 still pending
    }

    @Test
    void nextEventDate_returnsEarliestScheduled() {
        queue.schedule(LocalDate.of(2024, 6, 1), "later", () -> {});
        queue.schedule(LocalDate.of(2024, 1, 15), "earlier", () -> {});
        assertEquals(LocalDate.of(2024, 1, 15), queue.nextEventDate());
    }

    @Test
    void nextEventDate_emptyQueue_returnsNull() {
        assertNull(queue.nextEventDate());
    }
}
