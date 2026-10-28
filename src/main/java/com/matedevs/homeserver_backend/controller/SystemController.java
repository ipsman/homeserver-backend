package com.matedevs.homeserver_backend.controller;

import com.matedevs.homeserver_backend.service.SystemStatsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController // 👈 EZ FONTOS: enélkül a Spring nem látja vezérlőként!
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")// 👈 EZ IS: az útvonal előtagja
public class SystemController {

    private final SystemStatsService systemStatsService;

    public SystemController(SystemStatsService systemStatsService) {
        this.systemStatsService = systemStatsService;
    }

    @GetMapping("/stats") // 👈 Pontos végpont: /api/system/stats
    public Map<String, Object> getStats() {
        return systemStatsService.getSystemStats();
    }
}