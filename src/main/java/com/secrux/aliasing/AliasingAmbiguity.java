package com.secrux.aliasing;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

/**
 * Produces aliasing scenarios where multiple references might or might not share the same object.
 */
@Service
public class AliasingAmbiguity {

    private final Map<String, Object[]> pools = new HashMap<>();

    public AliasingAmbiguity() {
        Object critical = new CriticalResource("shared");
        // Store the same object in multiple containers to expand alias possibilities.
        pools.put("direct", new Object[]{critical});
        pools.put("wrapped", new Object[]{new WeakReference<>(critical)});
        pools.put("mirrored", new Object[]{cloneIfPossible(critical)});
    }

    public List<String> run() {
        List<String> messages = new ArrayList<>();
        // Choose path in an opaque way – static analysis must assume every pool is reachable.
        Object[] bucket = selectBucket(System.nanoTime());
        Object candidate = unwrap(bucket[0]);
        messages.add("Aliasing candidate hash: " + candidate.hashCode());

        // Enqueue into multiple structures; actual runtime aliasing is subtle here.
        Deque<Object> deque = new ArrayDeque<>();
        deque.add(candidate);
        deque.add(new CriticalResource("shadow"));
        deque.add(unwrap(pools.get("direct")[0]));

        Object maybeSame = deque.stream()
                .filter(obj -> Objects.equals(obj.toString(), candidate.toString()))
                .findFirst()
                .orElse(null);

        if (maybeSame == candidate) {
            messages.add("Alias resolved to shared instance");
        } else if (maybeSame == null) {
            messages.add("Analyzer might believe alias missing, but runtime sees new instance.");
        } else {
            messages.add("Indistinguishable but distinct object present");
        }
        return messages;
    }

    private Object[] selectBucket(long seed) {
        if (seed % 2 == 0) {
            return pools.get("direct");
        }
        if (seed % 3 == 0) {
            return pools.get("wrapped");
        }
        return pools.get("mirrored");
    }

    private Object unwrap(Object value) {
        if (value instanceof WeakReference) {
            Object referent = ((WeakReference<?>) value).get();
            return referent != null ? referent : new CriticalResource("resurrected");
        }
        return value;
    }

    private Object cloneIfPossible(Object obj) {
        try {
            return obj.getClass().getMethod("clone").invoke(obj);
        } catch (Exception ignored) {
            // Swallowing makes static analysis assume both original and clone may exist, though at runtime we fall back.
            return obj;
        }
    }

    /**
     * Resource whose equality intentionally collides across separate instances.
     */
    static class CriticalResource {
        private final String name;

        CriticalResource(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "CriticalResource:" + name;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof CriticalResource) {
                return Objects.equals(name, ((CriticalResource) obj).name);
            }
            return false;
        }
    }
}
