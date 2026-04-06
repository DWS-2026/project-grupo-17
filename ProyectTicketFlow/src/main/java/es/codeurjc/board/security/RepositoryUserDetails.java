package es.codeurjc.board.security;

import es.codeurjc.board.model.User;
import es.codeurjc.board.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
/**
 * Adaptador entre la tabla de usuarios y Spring Security.
 * Carga el usuario por email y transforma sus roles al formato ROLE_ requerido.
 */
public class RepositoryUserDetails implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	// Metodo que Spring Security invoca durante el login.
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		// Convierte roles de dominio (ej: ADMIN) en authorities de Spring (ROLE_ADMIN).
		List<GrantedAuthority> roles = new ArrayList<>();
		if (user.getRoles() != null) {
			for (String role : user.getRoles()) {
				roles.add(new SimpleGrantedAuthority("ROLE_" + role));
			}
		}

		return new org.springframework.security.core.userdetails.User(user.getEmail(), 
				user.getEncodedPassword(), roles);

	}
}