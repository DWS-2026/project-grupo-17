package es.codeurjc.board.service;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import es.codeurjc.board.model.Discoteca;
import es.codeurjc.board.model.Image;
import es.codeurjc.board.model.Evento;

@Service
public class DatabaseInitializer {

    @Autowired
    private DiscotecaService discotecaService;


    @Autowired
    private ImageService imageService;


    @PostConstruct
    public void init() throws IOException, URISyntaxException {

        Discoteca d1 = new Discoteca();
        d1.setName("Nuit");
        d1.setCalle("Calle Mayor 10");
        d1.setDescripcion("Discoteca con música electrónica");
        setDiscotecaImage(d1, "/posts/nuit.png");


        Discoteca d2 = new Discoteca();
        d2.setName("La Riviera");
        d2.setCalle("Avenida del Sol 25");
        d2.setDescripcion("Ambiente chill y cocktails");
        setDiscotecaImage(d2, "/posts/lariviera.png");

        discotecaService.save(d1);
        discotecaService.save(d2);
    }

    public void setDiscotecaImage(Discoteca discoteca, String classpathResource) throws IOException {
        Resource image = new ClassPathResource(classpathResource);

        Image createdImage = imageService.createImage(image.getInputStream());
        discoteca.setImage(createdImage);
    }
}