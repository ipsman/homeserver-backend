package com.matedevs.homeserver_backend.controller;

import com.matedevs.homeserver_backend.service.QbittorrentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/torrents")
@CrossOrigin(origins = "*")
public class QbittorrentController {

    @Autowired
    private QbittorrentService qbittorrentService;

    // GET /api/torrents -> Az összes torrent lekérése JSON-ben
    @GetMapping
    public ResponseEntity<String> getTorrents() {
        String torrentsJson = qbittorrentService.getTorrents();
        return ResponseEntity.ok(torrentsJson);
    }

    // POST /api/torrents/add -> Új torrent hozzáadása magnet linkkel
    @PostMapping("/add")
    public ResponseEntity<Void> addTorrent(@RequestBody Map<String, String> request) {
        String magnet = request.get("magnet");
        if (magnet != null && !magnet.isEmpty()) {
            qbittorrentService.addTorrent(magnet);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    // POST /api/torrents/{hash}/pause -> Megállítás
    @PostMapping("/{hash}/pause")
    public ResponseEntity<Void> pauseTorrent(@PathVariable String hash) {
        qbittorrentService.pauseTorrent(hash);
        return ResponseEntity.ok().build();
    }

    // POST /api/torrents/{hash}/resume -> Folytatás / Indítás
    @PostMapping("/{hash}/resume")
    public ResponseEntity<Void> resumeTorrent(@PathVariable String hash) {
        qbittorrentService.resumeTorrent(hash);
        return ResponseEntity.ok().build();
    }

    // DELETE /api/torrents/{hash} -> Törlés (opcionálisan fájlokkal együtt)
    @DeleteMapping("/{hash}")
    public ResponseEntity<Void> deleteTorrent(
            @PathVariable String hash,
            @RequestParam(defaultValue = "false") boolean deleteFiles) {
        qbittorrentService.deleteTorrent(hash, deleteFiles);
        return ResponseEntity.ok().build();
    }
}