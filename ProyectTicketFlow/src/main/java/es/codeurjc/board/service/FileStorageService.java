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
     * Guarda un fichero en disco y devuelve su nombre único (preservando extensión).
     * @param file Fichero a guardar
     * @return Nombre único del fichero guardado
     */
    public String storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Crear directorio si no existe
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        // Obtener extensión original
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // Generar nombre único con extensión original
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFileName);

        // Guardar fichero
        Files.copy(file.getInputStream(), filePath);

        return uniqueFileName;
    }

    /**
     * Obtiene un fichero del disco como recurso.
     * @param fileName Nombre único del fichero
     * @return Resource para servir el fichero
     */
    public Resource getFileAsResource(String fileName) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
        
        // Validar que la ruta está dentro del directorio permitido (seguridad)
        if (!filePath.toRealPath().startsWith(Paths.get(uploadDir).toRealPath())) {
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
     * @param fileName Nombre único del fichero
     */
    public void deleteFile(String fileName) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
        
        // Validar que la ruta está dentro del directorio permitido (seguridad)
        if (!filePath.toRealPath().startsWith(Paths.get(uploadDir).toRealPath())) {
            throw new IOException("Invalid file path");
        }

        Files.deleteIfExists(filePath);
    }

    /**
     * Obtiene la ruta física del fichero.
     * @param fileName Nombre único del fichero
     * @return Path del fichero
     */
    public Path getFilePath(String fileName) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
        
        if (!filePath.toRealPath().startsWith(Paths.get(uploadDir).toRealPath())) {
            throw new IOException("Invalid file path");
        }

        return filePath;
    }
}
