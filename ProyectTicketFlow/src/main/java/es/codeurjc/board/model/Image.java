package es.codeurjc.board.model;

import java.sql.Blob;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
/**
 * Entidad Image.
 * Guarda contenido binario (Blob) para avatars, carteles y fotos.
 */
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    // Identificador unico de la imagen.
    private Long id;

    @Lob
    // Binario de imagen persistido como objeto grande en BD.
    private Blob imageFile;

    // Constructor vacio requerido por JPA.
    public Image() {
    }

    public Image(Blob imageFile) {
        this.imageFile = imageFile;
    }

    // Getters y setters de la entidad.
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Blob getImageFile() {
        return imageFile;
    }

    public void setImageFile(Blob imageFile) {
        this.imageFile = imageFile;
    }

    @Override
    // toString reducido para no imprimir el Blob completo en logs.
    public String toString() {
        return "Image [id=" + id + "]";
    }
}