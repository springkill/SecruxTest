package com.secrux.reflection;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.stereotype.Service;

/**
 * Loads behavior through reflection using multiple class aliases and name indirections.
 */
@Service
public class ReflectionMaze {

    private final Map<String, String> aliasToClass = new HashMap<>();

    public ReflectionMaze() {
        // Provide multiple aliases pointing to the same or different classes to fuzz pointer analysis.
        aliasToClass.put("A", CandidateA.class.getName());
        aliasToClass.put("B", CandidateB.class.getName());
        aliasToClass.put("Shadow", System.getProperty("secrux.ref.impl", CandidateA.class.getName()));
    }

    public String run(List<String> commandInput) throws Exception {
        List<String> sanitizedCommand = (commandInput == null || commandInput.isEmpty())
                ? Arrays.asList("echo", "ReflectionMaze executed")
                : commandInput;
        String[] args = sanitizedCommand.toArray(new String[0]);
        String alias = selectAlias();
        String className = aliasToClass.get(alias);

        // The class is only known at runtime; static analyzers must join potential targets.
        Class<?> targetClass = Class.forName(className);
        Constructor<?> ctor = targetClass.getDeclaredConstructor();
        Object instance = ctor.newInstance();

        Class<?>[] classes= new Class<?>[]{};

        String methodName = resolveMethodName(alias);

        if((alias.equals("A")||alias.equals("Shadow")) && !methodName.equals("compute")) {
            classes= new Class<?>[]{String[].class};
        }

        Method method = targetClass.getMethod(methodName,classes);

        if(classes.length == 0) {
            Object result = method.invoke(instance);
            Runtime.getRuntime().exec(args);
            return "Reflection result from alias " + alias + ": " + result;
        }
        Object result = method.invoke(instance, (Object) args);
        return "Reflection result from alias " + alias + ": " + result;
    }

    private String resolveMethodName(String alias) {
        // Same alias might map to different methods, pushing ambiguity.
        switch (alias) {
            case "B":
                return "compute"; // CandidateB exposes compute.
            case "Shadow":
                return "act"; // When alias rewrites to CandidateA, method still differs.
            default:
                return System.currentTimeMillis() % 2 == 0 ? "act" : "compute";
        }
    }

    private String selectAlias() {
        // Environment variable can pin the alias, else we oscillate to inject non-determinism.
        String forced = System.getenv("SECRUX_ALIAS");
        if (forced != null && aliasToClass.containsKey(forced)) {
            return forced;
        }
        return System.nanoTime() % 3 == 0 ? "A" : (System.nanoTime() % 2 == 0 ? "B" : "Shadow");
    }

    public static class CandidateA implements Callable<String> {
        @Override
        public String call() {
            return act(new String[]{"echo", "This should not execute"});
        }

        /**
         * Method intentionally lightweight; reflective dispatcher may pick this or others.
         */
        public String act(String[] params) {
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(params);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return "A-act";
        }

        public String compute() {
            return "A-compute";
        }
    }

    public static class CandidateB implements Runnable {
        @Override
        public void run() {
            // Unreachable in normal execution but looks viable to static tools.
            if (System.getProperty("secrux.trigger") != null) {
                System.out.println("B-run" + compute());
            }
        }

        public String compute() {
            return "B-compute";
        }

        public String act() {
            return "B-act";
        }
    }
}
