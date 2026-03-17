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
    public void init() throws IOException {

        // Crear discoteca
        Discoteca d1 = new Discoteca();
        d1.setName("Nuit");
        d1.setCalle("Calle Mayor 10");
        d1.setDescripcion("Discoteca con música electrónica");

        // Asignar imagen **sin guardarla antes**
        setDiscotecaImage(d1, "/posts/nuit.png");

        // Crear eventos
        Evento e1 = new Evento();
        e1.setName("Noche Electrónica");
        e1.setDescripcion("DJ internacional toda la noche");
        e1.setEdadRequerida(18);
        e1.setDiscoteca(d1); // asignar la discoteca
        setEventoImage(e1, "/posts/Evento_1.png");

        Evento e2 = new Evento();
        e2.setName("Fiesta de Luces");
        e2.setDescripcion("Shows de luces y láser");
        e2.setEdadRequerida(18);
        e2.setDiscoteca(d1);
        setEventoImage(e2, "/posts/Evento_2.jpg"); // CORRECTO: e2, no e1

        // Asignar eventos a la discoteca
        d1.getEventos().addAll(Arrays.asList(e1, e2));

        // Guardar la discoteca: Hibernate persiste todo junto
        discotecaService.save(d1);
    }

    public void setDiscotecaImage(Discoteca discoteca, String classpathResource) throws IOException {
        Resource image = new ClassPathResource(classpathResource);
        Image img = imageService.createImage(image.getInputStream());
        discoteca.setImage(img);
    }

    public void setEventoImage(Evento evento, String classpathResource) throws IOException {
        Resource image = new ClassPathResource(classpathResource);
        Image img = imageService.createImage(image.getInputStream());
        evento.setImage(img);
    }

    }