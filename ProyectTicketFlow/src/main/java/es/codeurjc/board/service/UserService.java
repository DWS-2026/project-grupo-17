package es.codeurjc.board.service;

import es.codeurjc.board.model.User;
import es.codeurjc.board.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@Service
public class UserService {

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

    public void save(String nombre, String email, String password, LocalDate fechaNacimiento, MultipartFile avatar) throws IOException {
        User user = new User();
        user.setNombre(nombre);
        user.setEmail(email);
        user.setPassword(password);
        user.setFechaNacimiento(fechaNacimiento);
        
        if (avatar != null && !avatar.isEmpty()) {
            user.setAvatar(avatar.getBytes());
        }

        userRepository.save(user);
    }

    public boolean authenticate(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return user.get().getPassword().equals(password);
        }
        return false;
    }

    public void update(Long id, String nombre, String email, LocalDate fechaNacimiento, MultipartFile avatar) throws IOException {
        User user = userRepository.findById(id).orElse(null);

        if (user != null) {
            user.setNombre(nombre);
            user.setEmail(email);
            user.setFechaNacimiento(fechaNacimiento);

            if (avatar != null && !avatar.isEmpty()) {
                user.setAvatar(avatar.getBytes());
            }

            userRepository.save(user);
        }
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
