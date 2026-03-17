package es.codeurjc.board.model;

import jakarta.persistence.*;

@Entity
public class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String acceso;     // NORMAL o VIP
    private String incluye;    // lo que incluye la entrada
    private Double precio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evento_id")
    private Evento evento;

    public Entrada() {}

    public Entrada(String name, String acceso, String incluye, Double precio, Evento evento) {
        this.name = name;
        this.acceso = acceso;
        this.incluye = incluye;
        this.precio = precio;
        this.evento = evento;
    }

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