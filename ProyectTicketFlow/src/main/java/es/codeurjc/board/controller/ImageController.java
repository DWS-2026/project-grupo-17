package es.codeurjc.board.controller;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import es.codeurjc.board.service.ImageService;


@Controller
/**
 * Controlador auxiliar para servir imagenes por id.
 */
public class ImageController {

    @Autowired
    private ImageService imageService;

    @GetMapping("/images/{id}")
    // Devuelve el recurso binario detectando su tipo MIME de forma automatica.
    public ResponseEntity<Object> getImageFile(@PathVariable long id) throws SQLException {

        Resource imageFile = imageService.getImageFile(id);

        MediaType mediaType = MediaTypeFactory
                .getMediaType(imageFile)
                .orElse(MediaType.IMAGE_JPEG);

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .body(imageFile);
    }
}
