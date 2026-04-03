package es.codeurjc.board.service;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import es.codeurjc.board.model.Discoteca;
import es.codeurjc.board.model.Image;
import es.codeurjc.board.model.Evento;
import es.codeurjc.board.model.User; // Asegúrate de que esta ruta sea correcta
import es.codeurjc.board.repositories.UserRepository; // Asegúrate de que esta ruta sea correcta

import javax.sql.rowset.serial.SerialBlob;

@Service
public class DatabaseInitializer {

    @Autowired
    private DiscotecaService discotecaService;

    @Autowired
    private ImageService imageService;

    // 1. Inyectamos los servicios necesarios para la creación de usuarios
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @PostConstruct
    public void init() throws IOException, SQLException {

        // ==========================================
        // INICIALIZACIÓN DE USUARIOS
        // ==========================================
        
        // Crear usuario admin (Codificamos la password y le asignamos los roles "USER" y "ADMIN")
        User admin= new User(
            "Admin User", 
            "admin@example.com", 
            passwordEncoder.encode("admin"), 
            LocalDate.of(1990, 1, 10), 
            null, 
            "USER", "ADMIN"
        );


        
        // Crear usuarios de prueba normales (Codificamos la password y le asignamos solo el rol "USER")
        User juan= new User(
            "Juan García", 
            "juan@example.com", 
            passwordEncoder.encode("12345"), 
            LocalDate.of(1995, 5, 15), 
            null, 
            "USER"
        );

        User maria= new User(
            "María López", 
            "maria@example.com", 
            passwordEncoder.encode("password123"), 
            LocalDate.of(1998, 8, 20), 
            null, 
            "USER"
        );

        setUserAvatar(admin,"/posts/avatar.png");
        setUserAvatar(juan,"/posts/avatar.png");
        setUserAvatar(maria,"/posts/avatar.png");

        userRepository.save(admin);
        userRepository.save(juan);
        userRepository.save(maria);

        // ==========================================
        // INICIALIZACIÓN DE DISCOTECAS Y EVENTOS
        // ==========================================

        // Crear discoteca Nuit
        Discoteca d1 = new Discoteca();
        d1.setName("Nuit");
        d1.setCalle("Calle Mayor 10");
        d1.setDescripcion("Discoteca con música electrónica");

        // Asignar imagen sin guardarla antes
        setDiscotecaImage(d1, "/posts/nuit.png");

        // Crear eventos
        Evento e1 = new Evento();
        e1.setName("Noche Electrónica");
        e1.setDescripcion("DJ internacional toda la noche");
        e1.setEdadRequerida(18);
        e1.setDiscoteca(d1); // asignar la discoteca
        setEventoImage(e1, "/posts/imagen1.avif");

        Evento e2 = new Evento();
        e2.setName("Fiesta de Luces");
        e2.setDescripcion("Shows de luces y láser");
        e2.setEdadRequerida(18);
        e2.setDiscoteca(d1);
        setEventoImage(e2, "/posts/imagen2.avif"); 

        // Asignar eventos a la discoteca
        d1.getEventos().addAll(Arrays.asList(e1, e2));

        // Crear discoteca La Riviera
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
        setEventoImage(e4, "/posts/Event_4.jpg");

        // Asignar eventos a la discoteca
        d2.getEventos().addAll(Arrays.asList(e3, e4));

        Discoteca d3 = new Discoteca();
        d3.setName("Jowke");
        d3.setCalle("Calle Escaño");
        d3.setDescripcion("Discoteca con enanos");
        setDiscotecaImage(d3, "/posts/jowke.webp");

        Evento e5 = new Evento();
        e5.setName("Noche inolvidable");
        e5.setDescripcion("DJ Hardcore");
        e5.setEdadRequerida(16);
        e5.setDiscoteca(d3); // asignar la discoteca
        setEventoImage(e5, "/posts/Event_5.jpg");

        Evento e6 = new Evento();
        e6.setName("Noche final");
        e6.setDescripcion("DJ Theo");
        e6.setEdadRequerida(18);
        e6.setDiscoteca(d3); // asignar la discoteca
        setEventoImage(e6, "/posts/Event_6.jpg");

        d3.getEventos().addAll(Arrays.asList(e5,e6));

        // Guardar la discoteca: Hibernate persiste todo junto
        discotecaService.save(d1);
        discotecaService.save(d2);
        discotecaService.save(d3);
    }

    public void setDiscotecaImage(Discoteca discoteca, String classpathResource) throws IOException, SQLException {
        Resource resource = new ClassPathResource(classpathResource);
        byte[] bytes = resource.getInputStream().readAllBytes();
        Blob blob = new SerialBlob(bytes);
        Image img = new Image(blob);
        discoteca.setImage(img); 
    }

    public void setEventoImage(Evento evento, String classpathResource) throws IOException, SQLException {
        Resource resource = new ClassPathResource(classpathResource);
        byte[] bytes = resource.getInputStream().readAllBytes();
        Blob blob = new SerialBlob(bytes);
        Image img = new Image(blob);
        evento.setImage(img);
    }
    public void setUserAvatar(User user, String classpathResource) throws IOException, SQLException {
        Resource resource = new ClassPathResource(classpathResource);
        byte[] bytes = resource.getInputStream().readAllBytes();
        Blob blob = new SerialBlob(bytes);
        Image img = new Image(blob);
        user.setAvatar(img); // o setAvatar dependiendo de tu modelo
    }
}