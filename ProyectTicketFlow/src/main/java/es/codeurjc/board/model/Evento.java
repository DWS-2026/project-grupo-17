package es.codeurjc.board.model;

public class Evento {

    private Long id;
    private String name;
    private byte[] image;
    
    // --- CAMBIO CLAVE: Relacionamos con el objeto Discoteca ---
    private Discoteca discoteca; 
    
    // --- Otros atributos del evento ---
    private String descripcion;
    private Integer edadRequerida;

    public Evento() {}

    // Constructor actualizado recibiendo el objeto Discoteca completo
    public Evento(Long id, String name, Discoteca discoteca, String descripcion, byte[] image, Integer edadRequerida) {
        this.id = id;
        this.name = name;
        this.discoteca = discoteca;
        this.descripcion = descripcion;
        this.image = image;
        this.edadRequerida = edadRequerida;
    }

    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
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

    // --- GETTER Y SETTER DE LA DISCOTECA ---
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