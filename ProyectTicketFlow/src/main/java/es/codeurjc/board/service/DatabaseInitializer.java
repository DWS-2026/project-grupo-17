package es.codeurjc.board.service;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Blob;
import java.sql.SQLException;
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

import javax.sql.rowset.serial.SerialBlob;

@Service
public class DatabaseInitializer {

    @Autowired
    private DiscotecaService discotecaService;


    @Autowired
    private ImageService imageService;


    @PostConstruct
    public void init() throws IOException, SQLException {

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

        Discoteca d2 = new Discoteca();
        d2.setName("La Riviera");
        d2.setCalle("Avenida del Sol 25");
        d2.setDescripcion("Ambiente chill y cocktails");
        setDiscotecaImage(d2, "/posts/lariviera.png");

        // Crear eventos
        Evento e3 = new Evento();
        e3.setName("Noche Loca");
        e3.setDescripcion("DJ Dembow");
        e3.setEdadRequerida(16);
        e3.setDiscoteca(d2); // asignar la discoteca
        setEventoImage(e3, "/posts/Event_3.jpg");

        Evento e4 = new Evento();
        e4.setName("Fiesta Masónica");
        e4.setDescripcion("Bailes de Máscaras");
        e4.setEdadRequerida(23);
        e4.setDiscoteca(d2);
        setEventoImage(e4, "/posts/Event_4.jpg"); // CORRECTO: e2, no e1

        // Asignar eventos a la discoteca
        d2.getEventos().addAll(Arrays.asList(e3, e4));

        // Guardar la discoteca: Hibernate persiste todo junto
        discotecaService.save(d1);
        discotecaService.save(d2);
    }

    public void setDiscotecaImage(Discoteca discoteca, String classpathResource) throws IOException, SQLException {
        Resource resource = new ClassPathResource(classpathResource);
        byte[] bytes = resource.getInputStream().readAllBytes();
        Blob blob = new SerialBlob(bytes);
        Image img = new Image(blob);
        discoteca.setImage(img); // Hibernate lo persistirá automáticamente
    }

    public void setEventoImage(Evento evento, String classpathResource) throws IOException, SQLException {
        Resource resource = new ClassPathResource(classpathResource);
        byte[] bytes = resource.getInputStream().readAllBytes();
        Blob blob = new SerialBlob(bytes);
        Image img = new Image(blob);
        evento.setImage(img);
    }

    }