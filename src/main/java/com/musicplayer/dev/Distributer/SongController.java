package com.musicplayer.dev.Distributer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
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
    ) throws IOException {
        Resource resource = songService.getSong(folderName, fileName);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        Path path = songService.getSongPath(folderName, fileName);
        long size = Files.size(path);
        long lastModified = Files.getLastModifiedTime(path).toMillis();
        String etag = "\"" + size + "-" + lastModified + "\"";

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("audio/mpeg"))
            .contentLength(size)
            .lastModified(lastModified)
            .eTag(etag)
            .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
            .body(resource);
    }

    @ResponseBody
    @GetMapping("/covers/{folderName}/{fileName:.+\\.mp3}")
    public ResponseEntity<byte[]> getCover(
            @PathVariable String folderName,
            @PathVariable String fileName
    ) {
        try {
            Path path = songService.getSongPath(folderName, fileName);
            String stringPath = path.toString();
            byte[] image = mp3ImageService.extractImage(stringPath);

            long lastModified = Files.getLastModifiedTime(path).toMillis();
            long size = Files.size(path);
            String etag = "\"" + size + "-" + lastModified + "\"";

            if (image != null && image.length > 0) {
                String mimeType = mp3ImageService.getImageMimeType(stringPath);
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .contentLength(size)
                    .lastModified(lastModified)
                    .eTag(etag)
                    .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                    .body(image);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }   
    }
}