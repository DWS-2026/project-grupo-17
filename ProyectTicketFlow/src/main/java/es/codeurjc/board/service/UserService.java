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
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import es.codeurjc.board.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    // Valida los datos de registro
    public String validarRegistro(String nombre, String email, String password, String fechaNacimiento) {
        if (isBlank(nombre)) {
            return "El nombre es obligatorio";
        }

        if (isBlank(email)) {
            return "El email es obligatorio";
        }

        if (!email.contains("@")) {
            return "El email no es válido";
        }

        if (password == null || password.length() < 4) {
            return "La contraseña debe tener al menos 4 caracteres";
        }

        if (isBlank(fechaNacimiento)) {
            return "La fecha de nacimiento es obligatoria";
        }

        // Validar que el email no existe ya
        if (findByEmail(email).isPresent()) {
            return "El email ya está registrado";
        }

        return null;
    }

    // Registra un usuario nuevo con validación completa
    public void registroConValidacion(String nombre, String email, String password, String fechaNacimiento, MultipartFile avatar) throws IOException, SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate fecha = LocalDate.parse(fechaNacimiento, formatter);
        save(nombre, email, password, fecha, avatar);
    }

    // Valida los datos de actualización de perfil
    public String validarActualizacionPerfil(String nombre, String email, String fechaNacimiento) {
        if (isBlank(nombre)) {
            return "El nombre es obligatorio";
        }

        if (isBlank(email) || !email.contains("@")) {
            return "El email no es valido";
        }

        if (isBlank(fechaNacimiento)) {
            return "La fecha de nacimiento es obligatoria";
        }

        return null;
    }

    // Actualiza el perfil del usuario con validación
    public void actualizarPerfilConValidacion(Long userId, String nombre, String email, String fechaNacimiento, MultipartFile avatar) throws IOException, SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate fecha = LocalDate.parse(fechaNacimiento, formatter);
        update(userId, nombre, email, fecha, avatar);
    }

    // Utilidad privada para validar campos de texto obligatorios
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // Obtener la lista de usuarios (excepto el actual) para el panel de administración
    public List<es.codeurjc.board.dto.UserDTO> findAllOtherUsersAsDTO(String emailActual) {
        return userRepository.findAll().stream()
                .filter(u -> !u.getEmail().equals(emailActual))
                .map(this::toDTO)
                .toList();
    }

    // REST API methods
    public Page<UserDTO> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toDTO);
    }

    public Optional<UserDTO> findUserById(Long id) {
        return userRepository.findById(id).map(this::toDTO);
    }

    public UserDTO createUser(UserDTO userDTO) {
        User user = new User();
        user.setNombre(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setFechaNacimiento(userDTO.getBirthDate());
        user.setRoles(userDTO.getRoles() != null ? userDTO.getRoles() : List.of("USER"));
        user.setEncodedPassword(passwordEncoder.encode("default123")); // Default password for API created users
        userRepository.save(user);
        return toDTO(user);
    }

    public Optional<UserDTO> updateUser(Long id, UserDTO userDTO) {
        return userRepository.findById(id).map(user -> {
            user.setNombre(userDTO.getName());
            user.setEmail(userDTO.getEmail());
            user.setFechaNacimiento(userDTO.getBirthDate());
            if (userDTO.getRoles() != null) user.setRoles(userDTO.getRoles());
            userRepository.save(user);
            return toDTO(user);
        });
    }


    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getNombre());
        dto.setEmail(user.getEmail());
        dto.setBirthDate(user.getFechaNacimiento());
        dto.setRoles(user.getRoles());
        return dto;
    }
}