package es.codeurjc.board.service;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
/**
 * Bean de sesion HTTP.
 * Permite guardar datos del usuario actual mientras dure su sesion.
 */
public class UserSession {


	private Long userId;

	// Devuelve el id del usuario guardado en la sesion actual.
	public Long getUserId() {
		return userId;
	}

	// Actualiza el id de usuario asociado a la sesion actual.
	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
