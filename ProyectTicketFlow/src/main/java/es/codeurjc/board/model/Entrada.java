package es.codeurjc.board.model;

import jakarta.persistence.*;

@Entity
/**
 * Entidad Entrada.
 * Define un tipo de ticket vendible asociado a un evento.
 */
public class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    // Identificador unico de entrada.
    private Long id;

    private String name;

    // Tipo de acceso (por ejemplo NORMAL o VIP).
    private String acceso;
    // Descripcion de lo que incluye esta entrada.
    private String incluye;
    private Double precio;

    // Muchas entradas pertenecen a un unico evento.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evento_id")
    private Evento evento;

    // Constructor vacio requerido por JPA.
    public Entrada() {}

    public Entrada(String name, String acceso, String incluye, Double precio, Evento evento) {
        this.name = name;
        this.acceso = acceso;
        this.incluye = incluye;
        this.precio = precio;
        this.evento = evento;
    }

    // Getters y setters usados por JPA y formularios.
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAcceso() { return acceso; }
    public void setAcceso(String acceso) { this.acceso = acceso; }

    public String getIncluye() { return incluye; }
    public void setIncluye(String incluye) { this.incluye = incluye; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Evento getEvento() { return evento; }
    public void setEvento(Evento evento) { this.evento = evento; }
}