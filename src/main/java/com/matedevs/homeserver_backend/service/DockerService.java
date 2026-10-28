package com.matedevs.homeserver_backend.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DockerService {

    private final DockerClient dockerClient;

    public DockerService() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                // Windows esetén, ha pipe-ot használnál:
                // .withDockerHost("npipe:////./pipe/docker_engine")
                .build();

        DockerHttpClient httpClient = new ZerodepDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();

        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    public List<Map<String, Object>> getAllContainers() {
        System.out.println("Lekérdezés indítása a Docker felé...");

        // .withShowAll(true) -> Így a leállított konténerek is látszanak (docker ps -a)
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();

        System.out.println("Lekérdezés kész, konténerek száma: " + containers.size());

        List<Map<String, Object>> containerList = new ArrayList<>();

        for (Container c : containers) {
            Map<String, Object> containerMap = new HashMap<>();

            // Egyedi ID (ez kell majd a Start/Stop parancsokhoz)
            containerMap.put("id", c.getId());

            // Név (A Docker tömbben adja vissza, az elsőt tisztítjuk)
            String name = (c.getNames() != null && c.getNames().length > 0) ? c.getNames()[0] : "unknown";
            containerMap.put("name", name);

            // Kép neve
            containerMap.put("image", c.getImage());

            // Státusz szöveg (pl. "Up 2 hours")
            containerMap.put("status", c.getStatus());

            // Állapot ("running", "exited", stb.)
            containerMap.put("state", c.getState() != null ? c.getState().toLowerCase() : "unknown");

            containerList.add(containerMap);
        }

        return containerList;
    }

    public void startContainer(String containerId) {
        System.out.println("Kontindító parancs: " + containerId);
        dockerClient.startContainerCmd(containerId).exec();
    }

    public void stopContainer(String containerId) {
        System.out.println("Konténer leállító parancs: " + containerId);
        dockerClient.stopContainerCmd(containerId).exec();
    }

    public void restartContainer(String containerId) {
        System.out.println("Konténer ujraindit parancs: " + containerId);
        dockerClient.restartContainerCmd(containerId).exec();
    }
}