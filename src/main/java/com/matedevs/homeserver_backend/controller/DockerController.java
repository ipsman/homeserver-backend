package com.matedevs.homeserver_backend.controller;

import com.matedevs.homeserver_backend.service.DockerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docker")
public class DockerController {

    private final DockerService dockerService;

    public DockerController(DockerService dockerService){
        this.dockerService = dockerService;
    }

    @GetMapping("/containers")
    public List<String> listContainers (){
        return dockerService.getRunningContainers();
    }

    @PostMapping("/containers/{id}/start")
    public String startContainer(@PathVariable String id){
        dockerService.startContainer(id);
        return "Container " + id + " started successfully.";
    }

    @PostMapping("/containers/{id}/stop")
    public String stopContainer(@PathVariable String id){
        dockerService.stopContainer(id);
        return "Container " + id + " stopped successfully.";
    }

}
