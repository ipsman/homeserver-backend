package com.matedevs.homeserver_backend.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DockerService {

    private final DockerClient dockerClient;

    public DockerService() {
        // Explicit módon beállítjuk a környezetet, hogy felismerje a Windowst / alapértelmezett socketet
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                // Windows esetén a Docker Desktop pipe címét is megadhatjuk, ha szükséges:
                // .withDockerHost("npipe:////./pipe/docker_engine")
                .build();

        DockerHttpClient httpClient = new ZerodepDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();

        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    public List<String> getRunningContainers() {
        // Egy gyors teszt log, hogy lássuk, eljut-e ide a kérés
        System.out.println("Lekérdezés indítása a Docker felé...");
        List<Container> containers = dockerClient.listContainersCmd().exec();
        System.out.println("Lekérdezés kész, konténerek száma: " + containers.size());

        return containers.stream()
                .map(c -> {
                    String name = (c.getNames() != null && c.getNames().length > 0) ? c.getNames()[0] : "unknown";
                    return name + " (" + c.getImage() + ") - " + c.getStatus();
                })
                .collect(Collectors.toList());
    }

    public void startContainer(String containerId) {
        dockerClient.startContainerCmd(containerId).exec();
    }

    public void stopContainer(String containerId) {
        dockerClient.stopContainerCmd(containerId).exec();
    }
}