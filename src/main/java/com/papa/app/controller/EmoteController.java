package com.papa.app.controller;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.papa.app.service.DatabaseService;

@RestController
@RequestMapping("/api/emotes")
public class EmoteController {
    private final DatabaseService databaseService;
    private static final int THUMBNAIL_SIZE = 128;

    public EmoteController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadEmote(
            @RequestParam() MultipartFile file,
            @RequestParam("name") String emoteName) {
        
        // Validate emote name (alphanumeric and underscores only)
        if (!emoteName.matches("^[a-zA-Z0-9_]+$")) {
            return ResponseEntity.badRequest()
                .body("Emote name must contain only letters, numbers, and underscores");
        }

        try {
            // Read the original image
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                return ResponseEntity.badRequest().body("Invalid image file");
            }
            
            // Create thumbnail
            BufferedImage thumbnail = new BufferedImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = thumbnail.createGraphics();
            g2d.drawImage(originalImage.getScaledInstance(THUMBNAIL_SIZE, THUMBNAIL_SIZE, Image.SCALE_SMOOTH), 0, 0, null);
            g2d.dispose();

            // Convert to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, "jpeg", baos);
            byte[] imageData = baos.toByteArray();

            // Save to database
            int result = databaseService.getConnection().saveEmote(emoteName, imageData);
            switch (result) {
                case 0:
                    return ResponseEntity.ok("Emote created successfully");
                case 1:
                    return ResponseEntity.ok("Emote updated successfully");
                default:
                    return ResponseEntity.internalServerError().body("Failed to save emote");
            }
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to process image: " + e.getMessage());
        }
    }

    @DeleteMapping("/{emoteName}")
    public ResponseEntity<String> deleteEmote(@PathVariable String emoteName) {
        if (databaseService.getConnection().deleteEmote(emoteName)) {
            return ResponseEntity.ok("Emote deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{emoteName}")
    public ResponseEntity<byte[]> getEmote(@PathVariable String emoteName) {
        byte[] emoteData = databaseService.getConnection().getEmote(emoteName);
        if (emoteData != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(emoteData);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<String>> listEmotes() {
        List<String> emotes = databaseService.getConnection().getAllEmoteNames();
        return ResponseEntity.ok(emotes);
    }
}