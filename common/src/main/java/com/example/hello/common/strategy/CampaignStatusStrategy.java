package com.example.hello.common.strategy;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Set;

@Component
public class CampaignStatusStrategy implements StatusTransitionStrategy {

    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
            "DRAFT", Set.of("OPEN"),
            "OPEN", Set.of("LIVE"),
            "LIVE", Set.of("COMPLETED")
    );

    @Override
    public Map<String, Set<String>> getAllowedTransitions() {
        return TRANSITIONS;
    }
}
