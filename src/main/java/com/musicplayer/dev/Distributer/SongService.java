package com.musicplayer.dev.Distributer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SongService {

    @Value("${music.folder.path:music}")
    private String musicFolderPath;

    public List<String> getAllSongs() throws IOException {
        Path musicFolder = Paths.get(musicFolderPath);

        if (!Files.exists(musicFolder) || !Files.isDirectory(musicFolder)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.list(musicFolder)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(".mp3"))
                    .collect(Collectors.toList());
        }
    }

    public Resource getSong(String fileName) {
        Path filePath = Paths.get(musicFolderPath).resolve(fileName).normalize();
        return new FileSystemResource(filePath);
    }
}
