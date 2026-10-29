package com.matedevs.homeserver_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class QbittorrentService {

    @Value("${qbittorrent.url}")
    private String qbUrl;

    @Value("${qbittorrent.username}")
    private String qbUsername;

    @Value("${qbittorrent.password}")
    private String qbPassword;

    private String sessionCookie = null;
    private final RestTemplate restTemplate = new RestTemplate();

    // 1. Bejelentkezés a qBittorrentbe a SID sütik megszerzéséért
    public synchronized void login() {
        try {
            String loginUrl = qbUrl + "/api/v2/auth/login";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("username", qbUsername);
            map.add("password", qbPassword);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(loginUrl, request, String.class);

            // Kivesszük a SID sütit a válaszból
            String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
            if (setCookie != null && setCookie.contains("SID=")) {
                for (String part : setCookie.split(";")) {
                    if (part.trim().startsWith("SID=")) {
                        this.sessionCookie = part.trim();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Hiba a qBittorrent bejelentkezés során: " + e.getMessage());
        }
    }

    // Segédmetódus a hitelesített kérésekhez
    private HttpEntity<MultiValueMap<String, String>> createAuthEntity(MultiValueMap<String, String> body) {
        if (sessionCookie == null) {
            login();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        if (sessionCookie != null) {
            headers.add(HttpHeaders.COOKIE, sessionCookie);
        }
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Void> createAuthEntityGet() {
        if (sessionCookie == null) {
            login();
        }
        HttpHeaders headers = new HttpHeaders();
        if (sessionCookie != null) {
            headers.add(HttpHeaders.COOKIE, sessionCookie);
        }
        return new HttpEntity<>(headers);
    }

    // 2. Torrentek listázása
    public String getTorrents() {
        String url = qbUrl + "/api/v2/torrents/info";
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, createAuthEntityGet(), String.class);
            return response.getBody();
        } catch (Exception e) {
            // Ha lejárna a süti, újrapróbáljuk egyszer
            login();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, createAuthEntityGet(), String.class);
            return response.getBody();
        }
    }

    // 3. Torrent hozzáadása Magnet link alapján
    public void addTorrent(String magnetLink) {
        String url = qbUrl + "/api/v2/torrents/add";
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("urls", magnetLink);
        restTemplate.postForEntity(url, createAuthEntity(map), String.class);
    }

    // 4. Torrent szüneteltetése (Pause)
    public void pauseTorrent(String hash) {
        String url = qbUrl + "/api/v2/torrents/pause";
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("hashes", hash);
        restTemplate.postForEntity(url, createAuthEntity(map), String.class);
    }

    // 5. Torrent indítása (Resume)
    public void resumeTorrent(String hash) {
        String url = qbUrl + "/api/v2/torrents/resume";
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("hashes", hash);
        restTemplate.postForEntity(url, createAuthEntity(map), String.class);
    }

    // 6. Torrent törlése
    public void deleteTorrent(String hash, boolean deleteFiles) {
        String url = qbUrl + "/api/v2/torrents/delete";
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("hashes", hash);
        map.add("deleteFiles", String.valueOf(deleteFiles));
        restTemplate.postForEntity(url, createAuthEntity(map), String.class);
    }
}