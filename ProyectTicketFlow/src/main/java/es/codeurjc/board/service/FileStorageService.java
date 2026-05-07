package es.codeurjc.board.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Guarda un fichero en disco y devuelve su nombre único (incluyendo nombre
     * original).
     * 
     * @param file Fichero a guardar
     * @return Nombre único del fichero guardado
     */
    public String storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Crear directorio si no existe (normalizado a ruta absoluta)
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        // Obtener nombre original exacto
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IOException("Invalid file name");
        }

        // 1. Limpiar de path traversal internamente
        originalFileName = org.springframework.util.StringUtils.cleanPath(originalFileName);

        if (originalFileName.contains("..")) {
            throw new IOException("Invalid file name (Path Traversal detected)");
        }

        // Limpiar espacios para evitar problemas en URLs
        originalFileName = originalFileName.replaceAll("\\s+", "_");

        Path filePath = uploadPath.resolve(originalFileName).normalize();

        // 2. Doble protección final contra Path Traversal
        if (!filePath.startsWith(uploadPath)) {
            throw new IOException("Cannot store file outside current directory.");
        }

        // 3. Guardar fichero usando el nombre original (Se sobrescribe si ya existe
        // para mantener el nombre exacto)
        Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        return originalFileName; // Devolvemos el nombre original para guardarlo en la entidad y recuperarlo
                                 // después
    }

    public Resource getFileAsResource(String fileName) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = uploadPath.resolve(fileName).normalize();

        // Validar que la ruta está dentro del directorio permitido (seguridad)
        if (!filePath.startsWith(uploadPath)) {
            throw new IOException("Invalid file path");
        }

        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new IOException("File not found or not readable: " + fileName);
        }
    }

    /**
     * Elimina un fichero del disco.
     * 
     * @param fileName Nombre único del fichero
     */
    public void deleteFile(String fileName) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = uploadPath.resolve(fileName).normalize();

        if (!filePath.startsWith(uploadPath)) {
            throw new IOException("Invalid file path");
        }

        Files.deleteIfExists(filePath);
    }

    /**
     * Obtiene la ruta física del fichero.
     * 
     * @param fileName Nombre único del fichero
     * @return Path del fichero
     */
    public Path getFilePath(String fileName) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = uploadPath.resolve(fileName).normalize();

        if (!filePath.startsWith(uploadPath)) {
            throw new IOException("Invalid file path");
        }

        return filePath;
    }
}
