package es.codeurjc.board.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

@Entity
/**
 * Entidad Discoteca.
 * Representa una sala/local y actua como agregador de sus eventos.
 */
public class Discoteca {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    // Identificador unico generado automaticamente.
    private Long id;

    private String name;

    private String calle;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String descripcion;

    private String flyer;

    // Imagen principal de la discoteca (logo o foto).
    @OneToOne(cascade = CascadeType.ALL)
    private Image image;

    // Usuario propietario de la discoteca (modelo de ownership).
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private User owner;

    // Lista de eventos de la discoteca. orphanRemoval borra hijos huerfanos.
    @OneToMany(mappedBy = "discoteca", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evento> eventos = new ArrayList<>();

    // Constructor vacio requerido por JPA.
    public Discoteca() {}

    public Discoteca(String name, String calle, String descripcion) {
        this.name = name;
        this.calle = calle;
        this.descripcion = descripcion != null ? Jsoup.clean(descripcion, Safelist.relaxed()) : null;
    }

    public Discoteca(String name, String calle, String descripcion, User owner) {
        this.name = name;
        this.calle = calle;
        this.descripcion = descripcion != null ? Jsoup.clean(descripcion, Safelist.relaxed()) : null;
        this.owner = owner;
    }

    // Getters y setters de persistencia y uso en formularios/controladores.
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
    public void setDescripcion(String descripcion) { this.descripcion = descripcion != null ? Jsoup.clean(descripcion, Safelist.relaxed()) : null; }

    public String getFlyer() { return flyer; }
    public void setFlyer(String flyer) { this.flyer = flyer; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public List<Evento> getEventos() { return eventos; }
    public void setEventos(List<Evento> eventos) { this.eventos = eventos; }
}