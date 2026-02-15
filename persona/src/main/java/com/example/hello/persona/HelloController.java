package com.example.hello.persona;

import com.example.hello.common.RegionService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/seller-center")
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    private Map<String, RegionService> regionServices;

    private final Counter helloCounter;
    private final Counter regionCounter;
    private final Timer regionTimer;

    public HelloController(MeterRegistry meterRegistry) {
        this.helloCounter = Counter.builder("seller_center.hello.requests")
                .description("Number of hello endpoint requests")
                .register(meterRegistry);
        this.regionCounter = Counter.builder("seller_center.region.requests")
                .description("Number of region endpoint requests")
                .register(meterRegistry);
        this.regionTimer = Timer.builder("seller_center.region.duration")
                .description("Time taken to process region requests")
                .register(meterRegistry);
    }

    @GetMapping("/")
    public String hello() {
        helloCounter.increment();
        log.info("GET /seller-center/ called");
        return "Hello, Spring Boot!";
    }

    @GetMapping("/hello2")
    public String hello2() {
        helloCounter.increment();
        log.info("GET /seller-center/hello2 called");
        return "Hello 2nd";
    }

    @GetMapping("/hello3")
    public String hello3() {
        helloCounter.increment();
        log.info("GET /seller-center/hello3 called");
        return "Hello 3rd";
    }

    @GetMapping("/hello4")
    public String hello4() {
        helloCounter.increment();
        log.info("GET /seller-center/hello4 called");
        return "Hello 4th";
    }

    @GetMapping("/hello5")
    public String hello5() {
        helloCounter.increment();
        log.info("GET /seller-center/hello5 called");
        return "Hello 5th";
    }

    @GetMapping("/region")
    public String regionRequest(@RequestParam String region, @RequestParam String request) {
        regionCounter.increment();
        log.info("GET /seller-center/region called with region={}, request={}", region, request);
        return regionTimer.record(() -> {
            RegionService regionService = regionServices.get(region + "RegionService");
            if (regionService == null) {
                return "Unknown region: " + region;
            }
            return regionService.processRegionRequest(request);
        });
    }
}
