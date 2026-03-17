package es.codeurjc.board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import es.codeurjc.board.model.User;
import es.codeurjc.board.repositories.UserRepository;
import java.time.LocalDate;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(UserRepository userRepository) {
		return args -> {
			// Crear usuario admin
			userRepository.save(new User("Admin User", "admin@example.com", "admin", LocalDate.of(1990, 1, 10), null, true));
			
			// Crear usuarios de prueba normales
			userRepository.save(new User("Juan García", "juan@example.com", "12345", LocalDate.of(1995, 5, 15), null, false));
			userRepository.save(new User("María López", "maria@example.com", "password123", LocalDate.of(1998, 8, 20), null, false));
		};
	}
}

