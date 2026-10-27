package com.matedevs.homeserver_backend.controller;

import com.matedevs.homeserver_backend.service.DockerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
