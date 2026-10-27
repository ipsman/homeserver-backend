package com.matedevs.homeserver_backend.service;

import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.Map;

@Service
public class SystemStatsService {
    private final SystemInfo systemInfo = new SystemInfo();
    private final HardwareAbstractionLayer hardware = systemInfo.getHardware();
    private final CentralProcessor processor = hardware.getProcessor();

    private long[] prevTicks = processor.getSystemCpuLoadTicks();

    public Map<String, Object> getSystemStats() {
        GlobalMemory memory = hardware.getMemory();
        long totalMemory = memory.getTotal();
        long availableMemory = memory.getAvailable();
        long usedMemory = totalMemory - availableMemory;

        double memoryUsagePercent = (double) usedMemory / totalMemory * 100;

        double cpuLoad = processor.getSystemCpuLoad(1000);

        if(cpuLoad < 0){
            cpuLoad = 0.0;
        }

        double cpuUsagePercent = cpuLoad * 100;

        return Map.of(
                "cpuUsagePercent", Math.round(cpuUsagePercent * 100.0) / 100.0,
                "totalMemoryMB", totalMemory / (1024 * 1024),
                "usedMemoryMB", usedMemory / (1024 * 1024),
                "availableMemoryMB", availableMemory / (1024 * 1024),
                "memoryUsagePercent", Math.round(memoryUsagePercent * 100.0) / 100.0
        );
    }
}