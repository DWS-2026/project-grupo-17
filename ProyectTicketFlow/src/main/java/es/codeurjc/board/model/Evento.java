package es.codeurjc.board.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    // Imagen del evento
    @OneToOne(cascade = CascadeType.ALL)
    private Image image;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "discoteca_id")
    private Discoteca discoteca;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private User owner;

    private String descripcion;
    private Integer edadRequerida;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Entrada> entradas = new ArrayList<>();

    public Evento() {}

    public Evento(String name, Discoteca discoteca, String descripcion, Image image, Integer edadRequerida) {
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

    public Image getImage() { return image; }

    public void setImage(Image image) { this.image = image; }

    public Discoteca getDiscoteca() {
        return discoteca;
    }

    public void setDiscoteca(Discoteca discoteca) {
        this.discoteca = discoteca;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
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

    public List<Entrada> getEntradas() {
        return entradas;
    }

    public void setEntradas(List<Entrada> entradas) {
        this.entradas = entradas;
    }
}