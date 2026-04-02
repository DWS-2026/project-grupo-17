package es.codeurjc.board.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

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

    @OneToOne(cascade = CascadeType.ALL)
    private Image avatar;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles;

    @ManyToMany
    @JoinTable(
            name = "user_entradas",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "entrada_id")
    )
    private List<Entrada> entradasCompradas = new ArrayList<>();

    public User() {}

    public User(String nombre, String email, String encodedPassword, LocalDate fechaNacimiento,  Image avatar, String... roles) {
        this.nombre = nombre;
        this.email = email;
        this.encodedPassword = encodedPassword;
        this.fechaNacimiento = fechaNacimiento;
        this.avatar = avatar;
        this.roles = List.of(roles);
        this.entradasCompradas = new ArrayList<>();
    }

    // ===== GETTERS Y SETTERS =====

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

    public Image getAvatar() {
        return avatar;
    }

    public void setAvatar(Image avatar) {
        this.avatar = avatar;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    // 🔥 NUEVOS GETTERS/SETTERS

    public List<Entrada> getEntradasCompradas() {
        return entradasCompradas;
    }

    public void setEntradasCompradas(List<Entrada> entradasCompradas) {
        this.entradasCompradas = entradasCompradas;
    }
}