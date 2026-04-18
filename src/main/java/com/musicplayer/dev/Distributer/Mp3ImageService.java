package com.musicplayer.dev.Distributer;

import org.springframework.stereotype.Service;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

@Service
public class Mp3ImageService {

    public byte[] extractImage(String filePath) throws Exception {
        Mp3File mp3 = new Mp3File(filePath);

        if (mp3.hasId3v2Tag()) {
            ID3v2 tag = mp3.getId3v2Tag();
            return tag.getAlbumImage(); // may be null
        }

        return new byte[0]; // Return empty byte array if no image is found
    }

    public String getImageMimeType(String filePath) throws Exception {
        Mp3File mp3 = new Mp3File(filePath);

        if (mp3.hasId3v2Tag()) {
            ID3v2 tag = mp3.getId3v2Tag();
            return tag.getAlbumImageMimeType();
        }

        return null;
    }
}