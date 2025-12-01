package com.secrux.lambda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

/**
 * Chains lambdas that capture mutable state and method references, obscuring actual control/data flow.
 */
@Service
public class LambdaLabyrinth {

    private final ThreadLocal<String> threadLocal = ThreadLocal.withInitial(() -> "seed");

    public List<String> run(String optionalInput) throws Exception {
        List<String> messages = new ArrayList<>();
        List<Function<String, String>> pipeline = buildPipeline();

        // Compose the pipeline dynamically; static analysis must reason about each possible ordering.
        Callable<String> composed = compose(pipeline, () -> threadLocal.get() + "::origin");
        messages.add("Lambda pipeline: " + composed.call());

        // Another execution path mutates shared state, only occurring when optional value is present at runtime.
        Optional<String> optional = Optional.ofNullable(optionalInput != null ? optionalInput : System.getenv("SECRUX_LAMBDA"));
        optional.map(this::reassignThreadLocal)
                .map(v -> pipeline.get(v.length() % pipeline.size()))
                .ifPresent(func -> messages.add("Optional path: " + func.apply("drift")));
        return messages;
    }

    private List<Function<String, String>> buildPipeline() {
        List<Function<String, String>> steps = new ArrayList<>();
        steps.add(String::trim); // Simple method reference – analyzers may overfit to this benign case.
        steps.add(this::decorate); // Captures instance method with thread-local side effects.
        steps.add(LambdaLabyrinth::uppercaseWithTwist); // Static method that branches on hidden state.
        steps.add(input -> input + "::" + threadLocal.get());
        steps.add(input -> {
            if (input.hashCode() == 0) {
                return panicBranch(input); // Executed rarely yet visible to static tools.
            }
            return input;
        });
        return steps;
    }

    private Callable<String> compose(List<Function<String, String>> steps, Supplier<String> seedSupplier) {
        return () -> {
            String value = seedSupplier.get();
            for (Function<String, String> step : maybeShuffle(steps)) {
                value = step.apply(value);
            }
            return value;
        };
    }

    private List<Function<String, String>> maybeShuffle(List<Function<String, String>> steps) {
        if (System.nanoTime() % 5 == 0) {
            List<Function<String, String>> copy = new ArrayList<>(steps);
            // Swap entries conditionally to introduce runtime-only permutations.
            Function<String, String> temp = copy.get(0);
            copy.set(0, copy.get(copy.size() - 1));
            copy.set(copy.size() - 1, temp);
            return copy;
        }
        return steps;
    }

    private String reassignThreadLocal(String value) {
        threadLocal.set(value + System.nanoTime());
        return value;
    }

    private String decorate(String text) {
        threadLocal.set(text + "*" + threadLocal.get());
        return text + "-decorated";
    }

    private static String uppercaseWithTwist(String text) {
        if (text.length() % 2 == 0) {
            return text.toUpperCase();
        }
        return Arrays.stream(text.split(":"))
                .map(String::toLowerCase)
                .reduce((a, b) -> b + a)
                .orElse(text + "?twist");
    }

    private String panicBranch(String untouched) {
        // The branch is practically unreachable but entices static analyzers to believe it is.
        if (System.clearProperty("neverSet") != null) {
            throw new IllegalStateException("This should never happen at runtime");
        }
        return untouched + "::cold";
    }
}
