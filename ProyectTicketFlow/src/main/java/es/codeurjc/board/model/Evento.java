package es.codeurjc.board.model;

import jakarta.persistence.*;

@Entity
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @Lob
    private byte[] image;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "discoteca_id")
    private Discoteca discoteca;

    private String descripcion;

    private Integer edadRequerida;

    public Evento() {}

    public Evento(String name, Discoteca discoteca, String descripcion, byte[] image, Integer edadRequerida) {
        this.name = name;
        this.discoteca = discoteca;
        this.descripcion = descripcion;
        this.image = image;
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

    public Discoteca getDiscoteca() {
        return discoteca;
    }

    public void setDiscoteca(Discoteca discoteca) {
        this.discoteca = discoteca;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getEdadRequerida() {
        return edadRequerida;
    }

    public void setEdadRequerida(Integer edadRequerida) {
        this.edadRequerida = edadRequerida;
    }
}