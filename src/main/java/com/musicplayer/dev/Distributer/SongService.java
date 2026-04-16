package com.musicplayer.dev.Distributer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class SongService {

    @Value("${music.folder.path:music}")
    private String musicFolderPath;

    public List<String> getFolders() throws IOException {
        Path musicFolder = Paths.get(musicFolderPath);

        if (!Files.exists(musicFolder) || !Files.isDirectory(musicFolder)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.list(musicFolder)) {
            return paths
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    public List<String> getSongsByFolder(String folderName) throws IOException {
        Path folderPath = Paths.get(musicFolderPath).resolve(folderName).normalize();

        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.list(folderPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.toLowerCase().endsWith(".mp3"))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    public Resource getSong(String folderName, String fileName) {
        Path filePath = Paths.get(musicFolderPath)
                .resolve(folderName)
                .resolve(fileName)
                .normalize();

        return new FileSystemResource(filePath);
    }
}