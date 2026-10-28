package com.matedevs.homeserver_backend.service;

import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.FileSystem;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OSFileStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemStatsService {

    private final SystemInfo systemInfo = new SystemInfo();

    private long prevBytesRecv = -1;
    private long prevBytesSent = -1;
    private long prevTime = System.currentTimeMillis();

    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            HardwareAbstractionLayer hardware = systemInfo.getHardware();
            CentralProcessor processor = hardware.getProcessor();
            OperatingSystem os = systemInfo.getOperatingSystem();
            GlobalMemory memory = hardware.getMemory();

            long totalMemory = memory.getTotal();
            long availableMemory = memory.getAvailable();
            long usedMemory = totalMemory - availableMemory;
            double memoryUsagePercent = totalMemory > 0 ? (double) usedMemory / totalMemory * 100 : 0.0;

            double cpuLoad = processor.getSystemCpuLoad(1000);
            Sensors sensors = hardware.getSensors();
            double cpuTemp = sensors.getCpuTemperature();
            long[] currentFreqs = processor.getCurrentFreq();
            double cpuCores = processor.getPhysicalProcessorCount();

            double currentFreqGHz = 0.0;
            if (currentFreqs != null && currentFreqs.length > 0) {
                long sum = 0;
                for (long freq : currentFreqs) {
                    sum += freq;
                }
                currentFreqGHz = (double) sum / currentFreqs.length / (1024.0 * 1024.0 * 1024.0);
            }

            String osName = os.getFamily() + " " + os.getVersionInfo();
            String serverName = os.getNetworkParams().getHostName();

            long totalSeconds = os.getSystemUptime();
            long days = totalSeconds / (24 * 3600);
            long hours = (totalSeconds % (24 * 3600)) / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;

            String uptimeFormatted = (days > 0)
                    ? String.format("%d nap, %02d:%02d:%02d", days, hours, minutes, seconds)
                    : String.format("%02d:%02d:%02d", hours, minutes, seconds);

            long totalBytesRecv = 0;
            long totalBytesSent = 0;
            long maxLinkSpeedBps = 0;

            List<NetworkIF> networkIFs = hardware.getNetworkIFs();
            for (NetworkIF net : networkIFs) {
                net.updateAttributes();
                totalBytesRecv += net.getBytesRecv();
                totalBytesSent += net.getBytesSent();

                if (net.getSpeed() > 0) {
                    maxLinkSpeedBps += net.getSpeed();
                }
            }

            long currentTime = System.currentTimeMillis();
            double timeElapsedSeconds = (currentTime - prevTime) / 1000.0;

            double downloadSpeedKBps = 0.0;
            double uploadSpeedKBps = 0.0;
            double networkUsagePercent = 0.0;

            if (prevBytesRecv != -1 && timeElapsedSeconds > 0) {
                long diffRecv = totalBytesRecv - prevBytesRecv;
                long diffSent = totalBytesSent - prevBytesSent;

                if (diffRecv >= 0) downloadSpeedKBps = (diffRecv / timeElapsedSeconds) / 1024.0;
                if (diffSent >= 0) uploadSpeedKBps = (diffSent / timeElapsedSeconds) / 1024.0;

                long totalBytesDiff = (diffRecv > 0 ? diffRecv : 0) + (diffSent > 0 ? diffSent : 0);
                double currentBytesPerSec = totalBytesDiff / timeElapsedSeconds;
                double currentBitsPerSec = currentBytesPerSec * 8;

                if (maxLinkSpeedBps > 0) {
                    networkUsagePercent = (currentBitsPerSec / maxLinkSpeedBps) * 100.0;
                    if (networkUsagePercent > 100.0) {
                        networkUsagePercent = 100.0;
                    }
                }
            }

            prevBytesRecv = totalBytesRecv;
            prevBytesSent = totalBytesSent;
            prevTime = currentTime;

            FileSystem fileSystem = os.getFileSystem();
            List<OSFileStore> fileStores = fileSystem.getFileStores();
            List<Map<String, Object>> storageList = new ArrayList<>();

            for (OSFileStore store : fileStores) {
                long totalSpace = store.getTotalSpace();
                long usableSpace = store.getUsableSpace();
                long usedSpace = totalSpace - usableSpace;
                double usagePercent = totalSpace > 0 ? (double) usedSpace / totalSpace * 100 : 0.0;

                storageList.add(Map.of(
                        "name", store.getName() != null ? store.getName() : "",
                        "mount", store.getMount() != null ? store.getMount() : "",
                        "type", store.getType() != null ? store.getType() : "",
                        "totalSpaceGB", Math.round((totalSpace / (1024.0 * 1024.0 * 1024.0)) * 100.0) / 100.0,
                        "usableSpaceGB", Math.round((usableSpace / (1024.0 * 1024.0 * 1024.0)) * 100.0) / 100.0,
                        "usedSpaceGB", Math.round((usedSpace / (1024.0 * 1024.0 * 1024.0)) * 100.0) / 100.0,
                        "usagePercent", Math.round(usagePercent * 100.0) / 100.0
                ));
            }

            if (cpuLoad < 0) {
                cpuLoad = 0.0;
            }

            double cpuUsagePercent = cpuLoad * 100;
            double totalSystemUsage = (cpuUsagePercent * 0.6) + (memoryUsagePercent * 0.4);

            stats.put("cpuUsagePercent", Math.round(cpuUsagePercent * 100.0) / 100.0);
            stats.put("totalMemoryMB", totalMemory / (1024 * 1024));
            stats.put("usedMemoryMB", usedMemory / (1024 * 1024));
            stats.put("availableMemoryMB", availableMemory / (1024 * 1024));
            stats.put("memoryUsagePercent", Math.round(memoryUsagePercent * 100.0) / 100.0);
            stats.put("osName", osName);
            stats.put("uptimeFormatted", uptimeFormatted);
            stats.put("uptimeDays", days);
            stats.put("cpuTemp", cpuTemp);
            stats.put("storage", storageList);
            stats.put("downloadSpeedKBps", Math.round(downloadSpeedKBps * 100.0) / 100.0);
            stats.put("networkUsagePercent", Math.round(networkUsagePercent * 100.0) / 100.0);
            stats.put("uploadSpeedKBps", Math.round(uploadSpeedKBps * 100.0) / 100.0);
            stats.put("Hostname", serverName);
            stats.put("cpuCurrentFreqGHz", Math.round(currentFreqGHz * 100.0) / 100.0);
            stats.put("cpuCores", cpuCores);
            stats.put("totalSystemUsagePercent", Math.round(totalSystemUsage * 100.0) / 100.0);

        } catch (Exception e) {
            System.err.println("Hiba a rendszerstatisztika lekérdezésekor: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }
}