package es.codeurjc.board.service;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class UserSession {

	private String user;
	private Long userId;
	private boolean isAdmin;
	

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean admin) {
		isAdmin = admin;
	}


	public void logout() {
		this.user = null;
		this.userId = null;
		this.isAdmin = false;
	}

}
