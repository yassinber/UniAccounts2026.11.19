package dz.univ.uniaccounts.observer;

import java.util.EnumMap;
import java.util.Map;

/**
 * Pattern: OBSERVER — Role: ConcreteObserver
 * Counts the events by type and prints them.
 */
public class AdminDashboard implements AccountObserver {

    private final Map<EventType, Integer> counts = new EnumMap<EventType, Integer>(EventType.class);

    public AdminDashboard() {
        for (EventType t : EventType.values()) {
            counts.put(t, 0);
        }
    }

    @Override
    public void update(AccountEvent event) {
        int n = counts.get(event.getType()) + 1;
        counts.put(event.getType(), n);
        System.out.println("    [DASHBOARD] " + event.getType() + " count = " + n);
    }

    public int getCount(EventType type) {
        return counts.get(type);
    }

    public int getTotal() {
        int total = 0;
        for (int n : counts.values()) {
            total += n;
        }
        return total;
    }

    public void printSummary() {
        System.out.println("---- Admin dashboard: events by type ----");
        for (Map.Entry<EventType, Integer> e : counts.entrySet()) {
            System.out.println(String.format("  %-10s %3d", e.getKey(), e.getValue()));
        }
        System.out.println(String.format("  %-10s %3d", "TOTAL", getTotal()));
    }
}
