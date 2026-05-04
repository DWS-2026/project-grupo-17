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
 * Almacena ficheros en disco con su nombre único.
 * Mantiene compatibilidad con Blob para imágenes existentes en BD.
 */
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    // Identificador unico de la imagen.
    private Long id;

    @Lob
    // Binario de imagen persistido como objeto grande en BD (backwards compatibility).
    private Blob imageFile;

    // Nombre único del fichero guardado en disco.
    private String fileName;
    
    // Nombre original del fichero (para preservar extensión y referencia).
    private String originalFileName;

    // Constructor vacio requerido por JPA.
    public Image() {
    }

    public Image(Blob imageFile) {
        this.imageFile = imageFile;
    }

    // Constructor para nuevas imágenes almacenadas en disco.
    public Image(String fileName, String originalFileName) {
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.imageFile = null;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    @Override
    // toString reducido para no imprimir el Blob completo en logs.
    public String toString() {
        return "Image [id=" + id + ", fileName=" + fileName + "]";
    }
}