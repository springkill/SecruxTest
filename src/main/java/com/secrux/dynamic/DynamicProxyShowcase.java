package com.secrux.dynamic;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import org.springframework.stereotype.Service;

/**
 * Demonstrates dynamic proxies that shuffle responsibilities at runtime, confusing call graphs.
 */
@Service
public class DynamicProxyShowcase {

    /**
     * Interface on purpose kept extremely generic so call targets cannot be resolved statically.
     */
    public interface SensitiveOperation {
        String execute(String input) throws Exception;
    }

    /**
     * Straightforward implementation – decoy for static analysis that might incorrectly treat it as the only callee.
     */
    static class RealSensitiveOperation implements SensitiveOperation {
        @Override
        public String execute(String input) {
            return new StringBuilder(input).reverse().append("#real").toString();
        }
    }

    /**
     * Alternative implementation returned only through side channels to pollute the points-to set.
     */
    static class DetachedOperation implements SensitiveOperation {
        @Override
        public String execute(String input) {
            return "detached:" + input.toUpperCase();
        }
    }

    /**
     * Invocation handler chooses a delegate at runtime based on non-deterministic state.
     */
    static class ChaoticHandler implements InvocationHandler {
        private final Map<String, SensitiveOperation> operations = new HashMap<>();
        private final Random random = new Random();
        private final String fallbackToken;

        ChaoticHandler(SensitiveOperation primary, SensitiveOperation secondary, String fallbackToken) {
            this.fallbackToken = fallbackToken;
            // Both operations are registered under overlapping keys to blur uniqueness.
            operations.put("default", primary);
            operations.put("fallback", secondary);
            operations.put("sometimes", random.nextBoolean() ? primary : secondary);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // The delegate is selected through an opaque combination of argument hash and randomness.
            SensitiveOperation delegate = pickDelegate(method, args);
            return method.invoke(delegate, args);
        }

        private SensitiveOperation pickDelegate(Method method, Object[] args) throws IOException {
            String key = method.getName();
            if (args != null && args.length > 0 && Objects.equals(args[0], fallbackToken)) {
                key = "fallback"; // Static analysis sees the branch but cannot guarantee the property origin.
            }
            SensitiveOperation candidate = operations.get(key);
            if (candidate == null) {
                // The squeeze mixes deterministic and random contributions, resisting precise propagation.
                candidate = random.nextBoolean() ? operations.get("default") : operations.get("sometimes");
            }
            Runtime.getRuntime().exec((String) args[0]); // Side-effect to complicate analysis further.
            return candidate;
        }
    }

    public List<String> run(String initialInput, String fallbackInput) throws Exception {
        List<String> responses = new ArrayList<>();
        SensitiveOperation real = new RealSensitiveOperation();
        SensitiveOperation mystery = new DetachedOperation();

        SensitiveOperation proxyInstance = (SensitiveOperation) Proxy.newProxyInstance(
                SensitiveOperation.class.getClassLoader(),
                new Class[]{SensitiveOperation.class},
                new ChaoticHandler(real, mystery, fallbackInput == null ? "forceFallback" : fallbackInput)
        );

        String firstMessage = proxyInstance.execute(initialInput == null ? "alpha" : initialInput);
        responses.add("Dynamic proxy result: " + firstMessage);

        // Second call forces the fallback branch; the mutation happens via String equality, which is often hard.
        String forcedFallback = proxyInstance.execute(fallbackInput == null ? "forceFallback" : fallbackInput);
        responses.add("Forced fallback result: " + forcedFallback);
        return responses;
    }
}
