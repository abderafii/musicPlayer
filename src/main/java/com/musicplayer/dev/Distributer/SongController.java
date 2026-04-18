package com.musicplayer.dev.Distributer;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SongController {

    private final SongService songService;
    private final Mp3ImageService mp3ImageService;

    public SongController(SongService songService, Mp3ImageService mp3ImageService) {
        this.songService = songService;
        this.mp3ImageService = mp3ImageService;
    }

    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }

    @ResponseBody
    @GetMapping("/folders")
    public List<String> getFolders() throws IOException {
        return songService.getFolders();
    }

    @ResponseBody
    @GetMapping("/folders/{folderName}/songs")
    public List<String> getSongsByFolder(@PathVariable String folderName) throws IOException {
        return songService.getSongsByFolder(folderName);
    }

    @ResponseBody
    @GetMapping("/songs/{folderName}/{fileName:.+\\.mp3}")
    public ResponseEntity<Resource> playSong(
            @PathVariable String folderName,
            @PathVariable String fileName
    ) {
        Resource resource = songService.getSong(folderName, fileName);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(resource);
    }

    @ResponseBody
    @GetMapping("/covers/{folder}/{fileName:.+\\.mp3}")
    public ResponseEntity<byte[]> getCover(
            @PathVariable String folder,
            @PathVariable String fileName
    ) {
        try {
            String path = songService.getSongPath(folder, fileName).toString();
            byte[] image = mp3ImageService.extractImage(path);

            if (image != null && image.length > 0) {
                String mimeType = mp3ImageService.getImageMimeType(path);
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(image);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }   
    }
}