package com.matedevs.homeserver_backend.controller;

import com.matedevs.homeserver_backend.service.DockerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/docker")
@CrossOrigin(origins = "*")
public class DockerController {

    @Autowired
    private DockerService dockerService;

    @GetMapping("/containers")
    public List<Map<String, Object>> getContainers() {
        return dockerService.getAllContainers();
    }

    @PostMapping("/container/{id}/start")
    public void startContainer(@PathVariable String id) {
        dockerService.startContainer(id);
    }

    @PostMapping("/container/{id}/stop")
    public void stopContainer(@PathVariable String id) {
        dockerService.stopContainer(id);
    }

    @PostMapping("/container/{id}/restart")
    public void restartContainer(@PathVariable String id) {
        dockerService.restartContainer(id);
    }

}
