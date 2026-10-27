package com.matedevs.homeserver_backend.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DockerService {
    private final DockerClient dockerClient;

    public DockerService(){
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();

        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    public List<String> getRunningContainers() {
        List<Container> containers = dockerClient.listContainersCmd().exec();

        return containers.stream()
                .map(c -> {
                    String name = (c.getNames() != null && c.getNames().length > 0) ? c.getNames()[0] : "unknown";
                    return name + " (" + c.getImage() + ") - " + c.getStatus();
                })
                .collect(Collectors.toList());
    }
}
