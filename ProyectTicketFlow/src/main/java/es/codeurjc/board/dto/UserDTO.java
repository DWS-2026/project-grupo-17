package es.codeurjc.board.dto;

import java.time.LocalDate;
import java.util.List;

public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private LocalDate birthDate;
    private List<String> roles;

    public UserDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public boolean isAdmin() {
        return roles != null && roles.contains("ADMIN");
    }
}