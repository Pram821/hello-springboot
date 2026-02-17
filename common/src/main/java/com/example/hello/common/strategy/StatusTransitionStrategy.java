package com.example.hello.common.strategy;

import java.util.Map;
import java.util.Set;

public interface StatusTransitionStrategy {
    Map<String, Set<String>> getAllowedTransitions();

    default boolean isValidTransition(String from, String to) {
        if ("CANCELLED".equals(to)) return true;
        Set<String> allowed = getAllowedTransitions().get(from);
        return allowed != null && allowed.contains(to);
    }
}
