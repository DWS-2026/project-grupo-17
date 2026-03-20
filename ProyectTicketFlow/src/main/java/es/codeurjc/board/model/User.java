package es.codeurjc.board.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nombre;
    private String email;
    private String encodedPassword;
    private LocalDate fechaNacimiento;
    
    @Lob
    private byte[] avatar;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles;

    public User() {}

    public User(String nombre, String email, String encodedPassword, LocalDate fechaNacimiento, byte[] avatar, String... roles) {
        this.nombre = nombre;
        this.email = email;
        this.encodedPassword = encodedPassword;
        this.fechaNacimiento = fechaNacimiento;
        this.avatar = avatar;
        this.roles = List.of(roles);
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }

    public void setEncodedPassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public byte[] getAvatar() {
        return avatar;
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}