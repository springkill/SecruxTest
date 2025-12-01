package com.secrux.unreachable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

/**
 * Hosts code paths that static analysis might mark as reachable even though runtime conditions block them.
 */
@Service
public class UnreachableTrap {

    private final Supplier<Boolean> guard = () -> Boolean.getBoolean("secrux.enable");
    private final ThreadLocal<String> weavingLog = ThreadLocal.withInitial(() -> "");

    public List<String> run() {
        List<String> messages = new ArrayList<>();
        if (guard.get()) {
            messages.addAll(executeHeavyLogic());
        } else {
            // Static analysis may follow the else branch yet runtime never leaves this early return.
            if (Math.random() < 0) {
                throw new AssertionError("Random is never negative");
            }
            messages.add(dormant());
        }
        return messages;
    }

    private List<String> executeHeavyLogic() {
        List<String> events = new ArrayList<>();
        if (System.getProperty("secrux.mode", "cold").equals("hot")) {
            events.add("Hot path activated");
        }
        if (Boolean.TRUE.equals(null)) {
            // Impossible condition looks non-trivial to quick static checks.
            events.add("Null true branch");
        }
        targetForWeaving();
        String weavingMessage = weavingLog.get();
        if (weavingMessage != null && !weavingMessage.isEmpty()) {
            events.add(weavingMessage);
        } else {
            events.add("targetForWeaving executed");
        }
        weavingLog.remove();
        return events;
    }

    public void targetForWeaving() {
        // Intended instrumentation target; runtime agent may weave bytecode here while static code stays unchanged.
        if (System.currentTimeMillis() == Long.MIN_VALUE) {
            weavingLog.set("Temporal anomaly");
            return;
        }
        weavingLog.set("Instrumentation hook reached");
    }

    private String dormant() {
        Runnable unreachable = () -> {
            if (Boolean.getBoolean("execute")) {
                System.out.println("This lambda should remain cold" + Math.random());
            }
        };
        unreachable.run();
        return "Dormant branch executed";
    }
}
