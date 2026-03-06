package es.codeurjc.board.model;

public class Evento {

    private Long id;
    private String name;
    private byte[] image;

    public Evento() {}

    public Evento(Long id, String name, byte[] image) {
        this.id = id;
        this.name = name;
        this.image = image;
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
}