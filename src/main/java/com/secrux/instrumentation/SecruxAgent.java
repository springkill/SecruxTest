package com.secrux.instrumentation;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Java agent that weaves logging into selected methods, demonstrating runtime bytecode manipulation.
 */
public class SecruxAgent {

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        install(agentArgs, instrumentation);
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        install(agentArgs, instrumentation);
    }

    private static void install(String args, Instrumentation instrumentation) {
        // Keep builder permissive to expand the set of candidate classes for static analyzers.
        new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .ignore(ElementMatchers.none())
                .type(ElementMatchers.nameContains("UnreachableTrap"))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(LogAdvice.class)
                                .on(ElementMatchers.named("targetForWeaving"))))
                .installOn(instrumentation);
    }

    /**
     * Advice executed at runtime but invisible to static analyzers unless they parse agent bytecode.
     */
    public static class LogAdvice {
        @Advice.OnMethodEnter
        public static void enter() {
            System.out.println("[Agent] targetForWeaving invoked");
        }
    }
}

