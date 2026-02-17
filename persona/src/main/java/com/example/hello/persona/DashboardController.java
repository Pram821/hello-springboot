package com.example.hello.persona;

import com.example.hello.common.DashboardService;
import io.micrometer.core.annotation.Timed;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    @Timed(value = "dashboard.stats", description = "Time taken to get dashboard stats")
    public CompletableFuture<ResponseEntity<Map<String, Integer>>> getDashboardStats() {
        return dashboardService.getDashboardStats()
                .thenApply(ResponseEntity::ok);
    }
}
