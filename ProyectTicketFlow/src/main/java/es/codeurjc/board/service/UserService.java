package es.codeurjc.board.service;

import es.codeurjc.board.model.Image;
import es.codeurjc.board.model.User;
import es.codeurjc.board.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
/**
 * Servicio de usuarios:
 * centraliza la logica de consulta, alta, edicion y borrado de usuarios,
 * junto con la gestion de avatar por defecto o subido por formulario.
 */
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    // Devuelve todos los usuarios persistidos.
    public Collection<User> findAll() {
        return userRepository.findAll();
    }

    // Busca usuario por id; si no existe devuelve null.
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // Busca usuario por email (clave principal de login funcional).
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Crea y guarda usuario nuevo: cifra password, asigna rol USER y avatar (subido o por defecto).
    public void save(String nombre, String email, String password, LocalDate fechaNacimiento, MultipartFile avatar) throws IOException, SQLException {
        User user = new User();
        user.setNombre(nombre);
        user.setEmail(email);
        user.setEncodedPassword(passwordEncoder.encode(password));
        user.setFechaNacimiento(fechaNacimiento);
        user.setRoles(List.of("USER"));

        if (avatar != null && !avatar.isEmpty()) {
            Image img = createImageFromMultipart(avatar);
            user.setAvatar(img);
        } else {
            user.setAvatar(getDefaultImage("/posts/avatar.png"));
        }

        userRepository.save(user);
    }

    // Actualiza campos de perfil de un usuario existente y reemplaza avatar si llega uno nuevo.
    public void update(Long id, String nombre, String email, LocalDate fechaNacimiento, MultipartFile avatar) throws IOException, SQLException {
        User user = userRepository.findById(id).orElse(null);

        if (user != null) {
            user.setNombre(nombre);
            user.setEmail(email);
            user.setFechaNacimiento(fechaNacimiento);

            if (avatar != null && !avatar.isEmpty()) {
                Image img = createImageFromMultipart(avatar);
                user.setAvatar(img);
            }

            userRepository.save(user);
        }
    }

    // Convierte archivo multipart en entidad Image para persistirla en BD.
    public Image createImageFromMultipart(MultipartFile file) throws IOException, SQLException {
        byte[] bytes = file.getBytes();
        Blob blob = new SerialBlob(bytes);
        return new Image(blob);
    }

    // Carga una imagen por defecto desde classpath para usuarios sin avatar propio.
    public Image getDefaultImage(String classpathResource) throws IOException, SQLException {
        Resource resource = new ClassPathResource(classpathResource);
        byte[] bytes = resource.getInputStream().readAllBytes();
        Blob blob = new SerialBlob(bytes);
        return new Image(blob);
    }

    // Guarda una entidad User ya construida o modificada.
    public void saveUser(User user) {
        userRepository.save(user);
    }

    // Elimina usuario por id.
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}