package es.codeurjc.board.dto;

public class EventoDTO {
    private Long id;
    private String name;
    private String description;
    private Integer requiredAge;
    private Long discotecaId;
    private Long ownerId;

    public EventoDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getRequiredAge() { return requiredAge; }
    public void setRequiredAge(Integer requiredAge) { this.requiredAge = requiredAge; }

    public Long getDiscotecaId() { return discotecaId; }
    public void setDiscotecaId(Long discotecaId) { this.discotecaId = discotecaId; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
}
