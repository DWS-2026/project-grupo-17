package es.codeurjc.board.model;

import jakarta.persistence.*;

@Entity
public class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @Lob
    private byte[] image;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evento_id")
    private Evento evento;

    private String descripcion;

    private Double precio;

    private Integer edadRequerida;

    public Entrada() {}

    public Entrada(String name, Evento evento, String descripcion, byte[] image, Double precio, Integer edadRequerida) {
        this.name = name;
        this.evento = evento;
        this.descripcion = descripcion;
        this.image = image;
        this.precio = precio;
        this.edadRequerida = edadRequerida;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getEdadRequerida() {
        return edadRequerida;
    }

    public void setEdadRequerida(Integer edadRequerida) {
        this.edadRequerida = edadRequerida;
    }
}