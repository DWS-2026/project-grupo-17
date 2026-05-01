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
 * Servicio de arranque que precarga datos iniciales en la base de datos.
 * Se ejecuta una sola vez al levantar la aplicacion.
 */
public class DatabaseInitializer {

    @Autowired
    private DiscotecaService discotecaService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserRepository userRepository;

    // Se usa repositorio directo para crear una entrada de ejemplo inicial.
    @Autowired
    private EntradaRepository entradaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    // Metodo ejecutado automaticamente tras crear el bean: genera usuarios,
    // discotecas, eventos y entrada demo.
    public void init() throws IOException, SQLException {

        // 1) Inicializacion de usuarios base del sistema.

        User admin = new User(
                "Admin User",
                "admin@example.com",
                passwordEncoder.encode("admin"),
                LocalDate.of(1990, 1, 10),
                null,
                "USER", "ADMIN");

        User juan = new User(
                "Juan García",
                "juan@example.com",
                passwordEncoder.encode("12345"),
                LocalDate.of(1995, 5, 15),
                null,
                "USER");

        User maria = new User(
                "María López",
                "maria@example.com",
                passwordEncoder.encode("password123"),
                LocalDate.of(1998, 8, 20),
                null,
                "USER");

        setUserAvatar(admin, "/posts/avatar.png");
        setUserAvatar(juan, "/posts/avatar.png");
        setUserAvatar(maria, "/posts/avatar.png");

        userRepository.save(admin);
        userRepository.save(juan);
        userRepository.save(maria);

        // 2) Inicializacion de discotecas y sus eventos asociados.

        Discoteca d1 = new Discoteca();
        d1.setName("Nuit");
        d1.setCalle("Calle Mayor 10");
        d1.setDescripcion(
                "<p><strong>Discoteca con música electrónica</strong> de primer nivel.</p><p>Disfruta de los mejores <em>DJs internacionales</em> en un ambiente <span style=\"color: rgb(153, 51, 255);\">vibrante y único</span>.</p><ul><li>Aforo: 500 personas</li><li>Horario: 00:00 - 06:00</li></ul>");
        setDiscotecaImage(d1, "/posts/nuit.png");

        Evento e1 = new Evento();
        e1.setName("Noche Electrónica");
        e1.setDescripcion(
                "<p><strong>DJ internacional</strong> toda la noche con sets de <em>techno, house y trance</em>.</p><p>Una experiencia sonora <span style=\"color: rgb(255, 153, 0);\">inolvidable</span>.</p>");
        e1.setEdadRequerida(18);
        e1.setDiscoteca(d1);
        setEventoImage(e1, "/posts/imagen1.avif");

        Evento e2 = new Evento();
        e2.setName("Fiesta de Luces");
        e2.setDescripcion(
                "<p><strong>Shows de luces y láser</strong> espectaculares.</p><ul><li>Mapping 3D</li><li>Láser show</li><li>Iluminación LED</li></ul>");
        e2.setEdadRequerida(18);
        e2.setDiscoteca(d1);
        setEventoImage(e2, "/posts/imagen2.avif");

        d1.getEventos().addAll(Arrays.asList(e1, e2));

        Discoteca d2 = new Discoteca();
        d2.setName("La Riviera");
        d2.setCalle("Avenida del Sol 25");
        d2.setDescripcion(
                "<p><strong>Ambiente chill y cocktails</strong> premium.</p><p>El mejor lugar para disfrutar de una noche <em>relajada pero con estilo</em>.</p><ul><li>Terraza exterior</li><li>Carta de cócteles artesanales</li></ul>");
        setDiscotecaImage(d2, "/posts/lariviera.png");

        Evento e3 = new Evento();
        e3.setName("Noche Loca");
        e3.setDescripcion(
                "<p><strong>DJ Dembow</strong> - Los mejores ritmos urbanos.</p><p>Una noche <em>loca</em> con <span style=\"color: rgb(255, 0, 0);\">mucha energía</span>.</p>");
        e3.setEdadRequerida(16);
        e3.setDiscoteca(d2);
        setEventoImage(e3, "/posts/Event_3.jpg");

        Evento e4 = new Evento();
        e4.setName("Fiesta Masónica");
        e4.setDescripcion(
                "<p><strong>Bailes de Máscaras</strong> - Una noche de misterio y elegancia.</p><p><em>Dress code obligatorio</em>.</p>");
        e4.setEdadRequerida(23);
        e4.setDiscoteca(d2);
        setEventoImage(e4, "/posts/Event_4.jpg");

        d2.getEventos().addAll(Arrays.asList(e3, e4));

        Discoteca d3 = new Discoteca();
        d3.setName("Jowke");
        d3.setCalle("Calle Escaño");
        d3.setDescripcion(
                "<p><strong>Discoteca con espectáculos únicos</strong>.</p><p>Un lugar donde la diversión no tiene límites, con <em>shows en vivo</em> todas las noches.</p>");
        setDiscotecaImage(d3, "/posts/jowke.webp");

        Evento e5 = new Evento();
        e5.setName("Noche inolvidable");
        e5.setDescripcion(
                "<p><strong>DJ Hardcore</strong> - Sesión de música <span style=\"color: rgb(255, 0, 0);\">extrema</span>.</p><p>Solo para los más <em>atrevidos</em>.</p>");
        e5.setEdadRequerida(16);
        e5.setDiscoteca(d3);
        setEventoImage(e5, "/posts/Event_5.jpg");

        Evento e6 = new Evento();
        e6.setName("Noche final");
        e6.setDescripcion(
                "<p><strong>DJ Theo</strong> en una sesión <em>épica</em> de cierre de temporada.</p><p>No te pierdas la <span style=\"color: rgb(0, 102, 204);\">noche final</span>.</p>");
        e6.setEdadRequerida(18);
        e6.setDiscoteca(d3);
        setEventoImage(e6, "/posts/Event_6.jpg");

        d3.getEventos().addAll(Arrays.asList(e5, e6));

        // Al guardar discotecas, Hibernate persiste tambien eventos asociados por
        // cascada.
        discotecaService.save(d1);
        discotecaService.save(d2);
        discotecaService.save(d3);

        // 3) Inicializacion de una entrada de prueba para facilitar tests manuales.

        Entrada entradaPrueba = new Entrada(
                "Entrada General Anticipada",
                "NORMAL",
                "Incluye 1 consumición",
                15.50,
                e1);

        entradaRepository.save(entradaPrueba);
    }

    // Carga imagen desde recursos y la asigna a una discoteca.
    public void setDiscotecaImage(Discoteca discoteca, String classpathResource) throws IOException, SQLException {
        Resource resource = new ClassPathResource(classpathResource);
        byte[] bytes = resource.getInputStream().readAllBytes();
        Blob blob = new SerialBlob(bytes);
        Image img = new Image(blob);
        discoteca.setImage(img);
    }

    // Carga imagen desde recursos y la asigna a un evento.
    public void setEventoImage(Evento evento, String classpathResource) throws IOException, SQLException {
        Resource resource = new ClassPathResource(classpathResource);
        byte[] bytes = resource.getInputStream().readAllBytes();
        Blob blob = new SerialBlob(bytes);
        Image img = new Image(blob);
        evento.setImage(img);
    }

    // Carga imagen desde recursos y la asigna como avatar de usuario.
    public void setUserAvatar(User user, String classpathResource) throws IOException, SQLException {
        Resource resource = new ClassPathResource(classpathResource);
        byte[] bytes = resource.getInputStream().readAllBytes();
        Blob blob = new SerialBlob(bytes);
        Image img = new Image(blob);
        user.setAvatar(img);
    }
}