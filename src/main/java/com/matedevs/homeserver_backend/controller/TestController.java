package com.matedevs.homeserver_backend.controller;


import com.matedevs.homeserver_backend.service.SystemStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    private final SystemStatsService systemStatsService;

    public TestController(SystemStatsService systemStatsService) {
        this.systemStatsService = systemStatsService;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return systemStatsService.getSystemStats();
    }


}

@RestController
class HomeController {
    @GetMapping("/")
    public String home() {
        return "HomeServer Backend is up and running! 🚀";
    }
}