package es.codeurjc.board.model;

public class Discoteca {

    private Long id;
    private String name;
    private byte[] image;
    
    // Nuevos atributos
    private String calle;
    private String descripcion;

    public Discoteca() {}

    // Constructor actualizado
    public Discoteca(Long id, String name, byte[] image, String calle, String descripcion) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.calle = calle;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }

    // Nuevos Getters y Setters
    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}