package es.codeurjc.board.dto;

public class DiscotecaDTO {
    private Long id;
    private String name;
    private String street;
    private String description;
    private Long ownerId;
    private String imageURL;

    public DiscotecaDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getImageURL() { return imageURL; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }

}
