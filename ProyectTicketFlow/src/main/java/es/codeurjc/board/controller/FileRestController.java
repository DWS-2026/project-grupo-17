package es.codeurjc.board.controller;

import java.io.IOException;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.board.model.Image;
import es.codeurjc.board.service.ImageService;

@RestController
@RequestMapping("/api/v1/files")
/**
 * Controlador REST para servir ficheros almacenados en disco.
 */
public class FileRestController {

    @Autowired
    private ImageService imageService;

    @GetMapping("/images/{id}")
    // Devuelve la imagen por ID (GET desde REST).
    public ResponseEntity<Resource> getImageFile(@PathVariable long id) throws IOException, SQLException {
        Resource imageFile = imageService.getImageFile(id);

        MediaType mediaType = MediaTypeFactory
                .getMediaType(imageFile)
                .orElse(MediaType.IMAGE_JPEG);

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .body(imageFile);
    }

    @GetMapping("/images/{id}/download")
    // Descarga la imagen con su nombre original.
    public ResponseEntity<Resource> downloadImageFile(@PathVariable long id) throws IOException, SQLException {
        Image image = imageService.getImage(id);
        Resource imageFile = imageService.getImageFile(id);

        String originalFileName = image.getOriginalFileName() != null 
            ? image.getOriginalFileName() 
            : "image_" + id;

        MediaType mediaType = MediaTypeFactory
                .getMediaType(imageFile)
                .orElse(MediaType.IMAGE_JPEG);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFileName + "\"")
                .contentType(mediaType)
                .body(imageFile);
    }
}
