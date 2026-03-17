package es.codeurjc.board.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Discoteca {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String calle;
    private String descripcion;

    @OneToOne(cascade = CascadeType.ALL)
    private Image image;

    @OneToMany(mappedBy = "discoteca", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evento> eventos = new ArrayList<>();

    public Discoteca() {}

    public Discoteca(String name,String calle, String descripcion) {
        this.name = name;
        this.calle = calle;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public List<Evento> getEventos() { return eventos; }
    public void setEventos(List<Evento> eventos) { this.eventos = eventos; }
}