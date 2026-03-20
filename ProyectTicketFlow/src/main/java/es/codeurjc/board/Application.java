package es.codeurjc.board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import es.codeurjc.board.model.User;
import es.codeurjc.board.repositories.UserRepository;
import java.time.LocalDate;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return args -> {
        // Crear usuario admin (Codificamos la password y le asignamos los roles "USER" y "ADMIN")
        userRepository.save(new User(
            "Admin User", 
            "admin@example.com", 
            passwordEncoder.encode("admin"), 
            LocalDate.of(1990, 1, 10), 
            null, 
            "USER", "ADMIN"
        ));
        
        // Crear usuarios de prueba normales (Codificamos la password y le asignamos solo el rol "USER")
        userRepository.save(new User(
            "Juan García", 
            "juan@example.com", 
            passwordEncoder.encode("12345"), 
            LocalDate.of(1995, 5, 15), 
            null, 
            "USER"
        ));
        
        userRepository.save(new User(
            "María López", 
            "maria@example.com", 
            passwordEncoder.encode("password123"), 
            LocalDate.of(1998, 8, 20), 
            null, 
            "USER"
        ));
    };
	}
}

