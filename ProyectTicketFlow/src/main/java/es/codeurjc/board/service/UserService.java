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
import org.springframework.security.core.Authentication;


@Service
/**
 * User service:
 * centralizes the logic for querying, creating, editing and deleting users,
 * along with the management of the default or form-uploaded avatar.
 */
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    // Returns all persisted users.
    public Collection<User> findAll() {
        return userRepository.findAll();
    }

    // Finds a user by id; returns null if not found.
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // Finds a user by email (main functional login key).
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Converts a multipart file into an Image entity to persist it in DB.
    public Image createImageFromMultipart(MultipartFile file) throws IOException, SQLException {
        byte[] bytes = file.getBytes();
        Blob blob = new SerialBlob(bytes);
        return new Image(blob);
    }

    // Loads a default image from classpath for users without their own avatar.
    public Image getDefaultImage(String classpathResource) throws IOException, SQLException {
        Resource resource = new ClassPathResource(classpathResource);
        byte[] bytes = resource.getInputStream().readAllBytes();
        Blob blob = new SerialBlob(bytes);
        return new Image(blob);
    }

    // Saves an already built or modified User entity.
    public void saveUser(User user) {
        userRepository.save(user);
    }

    // Deletes a user by id.
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    // Validates registration data
    public String validarRegistro(String nombre, String email, String password, String fechaNacimiento) {
        if (isBlank(nombre)) {
            return "Name is required";
        }

        if (isBlank(email)) {
            return "Email is required";
        }

        if (!email.contains("@")) {
            return "Email is not valid";
        }

        if (password == null || password.length() < 4) {
            return "Password must be at least 4 characters long";
        }

        if (isBlank(fechaNacimiento)) {
            return "Date of birth is required";
        }

        // Validate that the email does not already exist
        if (findByEmail(email).isPresent()) {
            return "Email is already registered";
        }

        return null;
    }

    // Registers a new user with full validation
    public User registroConValidacion(String nombre,
                                      String email,
                                      String password,
                                      String fechaNacimiento,
                                      MultipartFile avatar)
            throws IOException, SQLException {

        return crearUsuario(
                nombre,
                email,
                password,
                fechaNacimiento,
                avatar,
                List.of("USER")
        );
    }

    // Validates profile update data
    public String validarActualizacionPerfil(String nombre, String email, String fechaNacimiento) {
        if (isBlank(nombre)) {
            return "Name is required";
        }

        if (isBlank(email) || !email.contains("@")) {
            return "Email is not valid";
        }

        if (isBlank(fechaNacimiento)) {
            return "Date of birth is required";
        }

        return null;
    }

    public User crearUsuario(String nombre,
                             String email,
                             String password,
                             String fechaNacimiento,
                             MultipartFile avatar,
                             List<String> roles)
            throws IOException, SQLException {

        String error = validarRegistro(nombre, email, password, fechaNacimiento);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate fecha = LocalDate.parse(fechaNacimiento, formatter);

        User user = new User();
        user.setNombre(nombre);
        user.setEmail(email);
        user.setEncodedPassword(passwordEncoder.encode(password));
        user.setFechaNacimiento(fecha);
        user.setRoles(roles != null ? roles : List.of("USER"));

        if (avatar != null && !avatar.isEmpty()) {
            Image img = createImageFromMultipart(avatar);
            user.setAvatar(img);
        } else {
            user.setAvatar(getDefaultImage("/posts/avatar.png"));
        }

        return userRepository.save(user);
    }

    public Optional<User> actualizarUsuario(Long id,
                                            String nombre,
                                            String email,
                                            String fechaNacimiento,
                                            MultipartFile avatar,
                                            List<String> roles)
            throws IOException, SQLException {

        User user = findById(id);

        if (user == null) {
            return Optional.empty();
        }

        String error = validarActualizacionPerfil(nombre, email, fechaNacimiento);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate fecha = LocalDate.parse(fechaNacimiento, formatter);

        user.setNombre(nombre);
        user.setEmail(email);
        user.setFechaNacimiento(fecha);

        if (roles != null) {
            user.setRoles(roles);
        }

        if (avatar != null && !avatar.isEmpty()) {
            Image img = createImageFromMultipart(avatar);
            user.setAvatar(img);
        }

        return Optional.of(userRepository.save(user));
    }

    // Updates the user profile with validation
    public Optional<User> actualizarPerfilConValidacion(Long userId,
                                                        String nombre,
                                                        String email,
                                                        String fechaNacimiento,
                                                        MultipartFile avatar)
            throws IOException, SQLException {

        return actualizarUsuario(
                userId,
                nombre,
                email,
                fechaNacimiento,
                avatar,
                null
        );
    }

    // Private utility to validate required text fields
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // Get the list of users (except the current one) for the administration panel
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

        try {
            User user = crearUsuario(
                    userDTO.getName(),
                    userDTO.getEmail(),
                    "default123",
                    userDTO.getBirthDate() != null ? userDTO.getBirthDate().toString() : null,
                    null,
                    userDTO.getRoles() != null ? userDTO.getRoles() : List.of("USER")
            );

            return toDTO(user);

        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public Optional<UserDTO> updateUser(Long id, UserDTO userDTO) {

        try {
            return actualizarUsuario(
                    id,
                    userDTO.getName(),
                    userDTO.getEmail(),
                    userDTO.getBirthDate() != null ? userDTO.getBirthDate().toString() : null,
                    null,
                    userDTO.getRoles()
            ).map(this::toDTO);

        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public byte[] getUserAvatar(Long id) throws SQLException {

        User user = findById(id);

        if (user == null || user.getAvatar() == null) {
            return null;
        }

        Blob blob = user.getAvatar().getImageFile();

        return blob.getBytes(1, (int) blob.length());
    }

    public boolean canAccessUser(Long targetId, Authentication auth) {

        String email = auth.getName();

        Optional<User> currentUserOpt = userRepository.findByEmail(email);

        if (currentUserOpt.isEmpty()) {
            return false;
        }

        User currentUser = currentUserOpt.get();

        boolean esAdmin = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return esAdmin || currentUser.getId().equals(targetId);
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