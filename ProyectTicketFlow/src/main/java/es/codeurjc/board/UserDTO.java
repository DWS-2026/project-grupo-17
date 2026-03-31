package es.codeurjc.board;

import java.time.LocalDate;

public class UserDTO {

    private Long id;
    private String nombre;
    private String email;
    private LocalDate fechaNacimiento;
    private boolean admin;

    public UserDTO(Long id, String nombre, String email, LocalDate fechaNacimiento, boolean admin) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.fechaNacimiento = fechaNacimiento;
        this.admin = admin;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public boolean isAdmin() {
        return admin;
    }
}
