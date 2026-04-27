package es.codeurjc.board.dto;

public class EntradaDTO {
    private Long id;
    private String name;
    private String accessType;
    private String includes;
    private Double price;
    private Long eventId;

    public EntradaDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAccessType() { return accessType; }
    public void setAccessType(String accessType) { this.accessType = accessType; }

    public String getIncludes() { return includes; }
    public void setIncludes(String includes) { this.includes = includes; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
}
