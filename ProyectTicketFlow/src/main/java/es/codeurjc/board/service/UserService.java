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
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public Collection<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Guardar un usuario con avatar opcional
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

    // Actualizar usuario
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

    // Crear Image desde MultipartFile
    public Image createImageFromMultipart(MultipartFile file) throws IOException, SQLException {
        byte[] bytes = file.getBytes();
        Blob blob = new SerialBlob(bytes);
        return new Image(blob);
    }

    // Obtener imagen por defecto
    public Image getDefaultImage(String classpathResource) throws IOException, SQLException {
        Resource resource = new ClassPathResource(classpathResource);
        byte[] bytes = resource.getInputStream().readAllBytes();
        Blob blob = new SerialBlob(bytes);
        return new Image(blob);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}