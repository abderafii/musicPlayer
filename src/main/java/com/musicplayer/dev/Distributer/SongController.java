package com.musicplayer.dev.Distributer;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }

    @ResponseBody
    @GetMapping("/songs")
    public List<String> getAllSongs() throws IOException {
        return songService.getAllSongs();
    }

    @ResponseBody
    @GetMapping("/songs/{fileName:.+}")
    public ResponseEntity<Resource> playSong(@PathVariable String fileName) {
        Resource resource = songService.getSong(fileName);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(resource);
    }
}