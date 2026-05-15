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
import es.codeurjc.board.model.User;
import es.codeurjc.board.model.Entrada;
import es.codeurjc.board.repositories.UserRepository;
import es.codeurjc.board.repositories.EntradaRepository;

import javax.sql.rowset.serial.SerialBlob;

@Service
/**
 * Bootstrap service that preloads initial data into the database.
 * It runs only once when the application starts.
 */
public class DatabaseInitializer {

    @Autowired
    private DiscotecaService discotecaService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserRepository userRepository;

    // Direct repository used to create an initial sample ticket.
    @Autowired
    private EntradaRepository entradaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    // Method automatically executed after bean creation: generates users,
    // clubs, events and a demo ticket.
    public void init() throws IOException, SQLException {

        // 1) Initialization of base system users.

        User admin = new User(
                "Admin User",
                "admin@example.com",
                passwordEncoder.encode("Admin123!_SecurePath"),
                LocalDate.of(1990, 1, 10),
                null,
                "USER", "ADMIN");

        User juan = new User(
                "Juan García",
                "juan@example.com",
                passwordEncoder.encode("JuanP@ss_2026.Flow"),
                LocalDate.of(1995, 5, 15),
                null,
                "USER");

        User maria = new User(
                "María López",
                "maria@example.com",
                passwordEncoder.encode("Maria.Secure*98!App"),
                LocalDate.of(1998, 8, 20),
                null,
                "USER");

        setUserAvatar(admin, "/posts/avatar.png");
        setUserAvatar(juan, "/posts/avatar.png");
        setUserAvatar(maria, "/posts/avatar.png");

        userRepository.save(admin);
        userRepository.save(juan);
        userRepository.save(maria);

        // 2) Initialization of clubs and their associated events.

        Discoteca d1 = new Discoteca();
        d1.setName("Nuit");
        d1.setCalle("Calle Mayor 10");
        d1.setDescripcion(
                "<p><strong>Club de música electrónica de primer nivel</strong>.</p><p>Disfruta de los mejores <em>DJs internacionales</em> en un ambiente <span style=\"color: rgb(153, 51, 255);\">vibrante y único</span>.</p><ul><li>Aforo: 500 personas</li><li>Horario: 00:00 - 06:00</li></ul>");
        setDiscotecaImage(d1, "/posts/nuit.png");

        Evento e1 = new Evento();
        e1.setName("Noche Electrónica");
        e1.setDescripcion(
                "DJ internacional toda la noche con sesiones de techno, house y trance. Una experiencia sonora inolvidable.");
        e1.setEdadRequerida(18);
        e1.setDiscoteca(d1);
        setEventoImage(e1, "/posts/imagen1.avif");

        Evento e2 = new Evento();
        e2.setName("Fiesta de Luces");
        e2.setDescripcion(
                "Espectaculares shows de luces y láser. Incluye mapping 3D, espectáculo láser e iluminación LED.");
        e2.setEdadRequerida(18);
        e2.setDiscoteca(d1);
        setEventoImage(e2, "/posts/imagen2.avif");

        d1.getEventos().addAll(Arrays.asList(e1, e2));

        Discoteca d2 = new Discoteca();
        d2.setName("La Riviera");
        d2.setCalle("Avenida del Sol 25");
        d2.setDescripcion(
                "<p><strong>Ambiente tranquilo y cócteles premium</strong>.</p><p>El mejor lugar para disfrutar de una noche <em>relajada pero con estilo</em>.</p><ul><li>Terraza exterior</li><li>Carta de cócteles artesanales</li></ul>");
        setDiscotecaImage(d2, "/posts/lariviera.png");

        Evento e3 = new Evento();
        e3.setName("Noche Loca");
        e3.setDescripcion(
                "DJ Dembow - Los mejores ritmos urbanos. Una noche loca llena de energía.");
        e3.setEdadRequerida(16);
        e3.setDiscoteca(d2);
        setEventoImage(e3, "/posts/Event_3.jpg");

        Evento e4 = new Evento();
        e4.setName("Fiesta de Máscaras");
        e4.setDescripcion(
                "Baile de Máscaras - Una noche de misterio y elegancia. Código de vestimenta obligatorio.");
        e4.setEdadRequerida(23);
        e4.setDiscoteca(d2);
        setEventoImage(e4, "/posts/Event_4.jpg");

        d2.getEventos().addAll(Arrays.asList(e3, e4));

        Discoteca d3 = new Discoteca();
        d3.setName("Jowke");
        d3.setCalle("Calle Escaño");
        d3.setDescripcion(
                "<p><strong>Club con espectáculos únicos</strong>.</p><p>Un lugar donde la diversión no tiene límites, con <em>shows en directo</em> cada noche.</p>");
        setDiscotecaImage(d3, "/posts/jowke.webp");

        Evento e5 = new Evento();
        e5.setName("Noche Inolvidable");
        e5.setDescripcion(
                "DJ Hardcore - Sesión de música extrema. Solo para los más atrevidos.");
        e5.setEdadRequerida(16);
        e5.setDiscoteca(d3);
        setEventoImage(e5, "/posts/Event_5.jpg");

        Evento e6 = new Evento();
        e6.setName("Noche Final");
        e6.setDescripcion(
                "DJ Theo en una épica sesión de cierre de temporada. No te pierdas la noche final.");
        e6.setEdadRequerida(18);
        e6.setDiscoteca(d3);
        setEventoImage(e6, "/posts/Event_6.jpg");

        d3.getEventos().addAll(Arrays.asList(e5, e6));

        // When saving clubs, Hibernate also persists associated events via
        // cascade.
        discotecaService.save(d1);
        discotecaService.save(d2);
        discotecaService.save(d3);

        // 3) Initialization of a test ticket to facilitate manual testing.

        Entrada entradaPrueba = new Entrada(
                "Entrada General Anticipada",
                "NORMAL",
                "Incluye 1 consumición",
                15.50,
                e1);

        entradaRepository.save(entradaPrueba);
    }

    // Loads an image from resources and assigns it to a club.
    public void setDiscotecaImage(Discoteca discoteca, String classpathResource) throws IOException, SQLException {
        Resource resource = new ClassPathResource(classpathResource);
        byte[] bytes = resource.getInputStream().readAllBytes();
        Blob blob = new SerialBlob(bytes);
        Image img = new Image(blob);
        discoteca.setImage(img);
    }

    // Loads an image from resources and assigns it to an event.
    public void setEventoImage(Evento evento, String classpathResource) throws IOException, SQLException {
        Resource resource = new ClassPathResource(classpathResource);
        byte[] bytes = resource.getInputStream().readAllBytes();
        Blob blob = new SerialBlob(bytes);
        Image img = new Image(blob);
        evento.setImage(img);
    }

    // Loads an image from resources and assigns it as the user avatar.
    public void setUserAvatar(User user, String classpathResource) throws IOException, SQLException {
        Resource resource = new ClassPathResource(classpathResource);
        byte[] bytes = resource.getInputStream().readAllBytes();
        Blob blob = new SerialBlob(bytes);
        Image img = new Image(blob);
        user.setAvatar(img);
    }
}