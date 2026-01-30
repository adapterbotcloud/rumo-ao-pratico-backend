package com.rumoaopratico.controller;

import com.rumoaopratico.dto.stats.DashboardStatsResponse;
import com.rumoaopratico.security.SecurityUser;
import com.rumoaopratico.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
@Tag(name = "Stats", description = "Dashboard statistics endpoints")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<DashboardStatsResponse> getDashboard(@AuthenticationPrincipal SecurityUser user) {
        DashboardStatsResponse response = statsService.getDashboardStats(user.getId());
        return ResponseEntity.ok(response);
    }
}
