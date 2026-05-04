package es.codeurjc.board.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.board.model.Image;
import es.codeurjc.board.repositories.ImageRepository;

@Service
/**
 * Servicio de imagenes:
 * encapsula la creacion, lectura, reemplazo y borrado de ficheros de imagen.
 * Soporta almacenamiento en disco y en BD para compatibilidad.
 */
public class ImageService {

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private FileStorageService fileStorageService;

	// Obtiene la entidad Image por id.
	public Image getImage(long id) {
		return imageRepository.findById(id).orElseThrow();
	}

	// Crea y persiste una imagen nueva leyendo todos los bytes del InputStream.
	public Image createImage(InputStream inputStream) throws IOException {

		Image image = new Image();

		try {
			image.setImageFile(new SerialBlob(inputStream.readAllBytes()));
		} catch (Exception e) {
			throw new IOException("Failed to create image", e);
		}

		imageRepository.save(image);

		return image;
	}

	/**
	 * Crea una nueva imagen a partir de un MultipartFile guardándola en disco.
	 * @param multipartFile Fichero subido
	 * @return Entidad Image con referencia al fichero en disco
	 */
	public Image createImageFromFile(MultipartFile multipartFile) throws IOException {
		if (multipartFile == null || multipartFile.isEmpty()) {
			return null;
		}

		// Guardar fichero en disco
		String uniqueFileName = fileStorageService.storeFile(multipartFile);
		
		// Crear entidad Image con referencia al fichero
		Image image = new Image(uniqueFileName, multipartFile.getOriginalFilename());
		imageRepository.save(image);

		return image;
	}

	// Devuelve un recurso de solo lectura con el contenido binario de la imagen.
	public Resource getImageFile(long id) throws SQLException, IOException {

		Image image = imageRepository.findById(id).orElseThrow();

		// Si tiene fileName (almacenado en disco), devolver desde disco
		if (image.getFileName() != null && !image.getFileName().isEmpty()) {
			return fileStorageService.getFileAsResource(image.getFileName());
		}
		// Si no, intentar devolver desde BD (backwards compatibility)
		else if (image.getImageFile() != null) {
			return new InputStreamResource(image.getImageFile().getBinaryStream());
		} else {
			throw new RuntimeException("Image file not found");
		}
	}

	// Reemplaza el contenido de una imagen ya existente.
	public Image replaceImageFile(long id, InputStream inputStream) throws IOException {

		Image image = imageRepository.findById(id).orElseThrow();

		try {
			image.setImageFile(new SerialBlob(inputStream.readAllBytes()));
		} catch (Exception e) {
			throw new IOException("Failed to create image", e);
		}

		imageRepository.save(image);

		return image;
	}

	/**
	 * Reemplaza la imagen de una entidad con un nuevo fichero.
	 * @param id ID de la imagen
	 * @param multipartFile Nuevo fichero
	 * @return Entidad Image actualizada
	 */
	public Image replaceImageFile(long id, MultipartFile multipartFile) throws IOException {
		Image image = imageRepository.findById(id).orElseThrow();

		// Eliminar fichero anterior si existe
		if (image.getFileName() != null && !image.getFileName().isEmpty()) {
			try {
				fileStorageService.deleteFile(image.getFileName());
			} catch (IOException e) {
				// Log pero continuar
				System.err.println("Error deleting old file: " + e.getMessage());
			}
		}

		// Guardar nuevo fichero
		String uniqueFileName = fileStorageService.storeFile(multipartFile);
		image.setFileName(uniqueFileName);
		image.setOriginalFileName(multipartFile.getOriginalFilename());

		imageRepository.save(image);

		return image;
	}

	// Borra una imagen de BD y devuelve la entidad eliminada.
	public Image deleteImage(long id) {
		Image image = imageRepository.findById(id).orElseThrow();
		
		// Eliminar fichero del disco si existe
		if (image.getFileName() != null && !image.getFileName().isEmpty()) {
			try {
				fileStorageService.deleteFile(image.getFileName());
			} catch (IOException e) {
				System.err.println("Error deleting file: " + e.getMessage());
			}
		}
		
		imageRepository.deleteById(id);
		return image;
	}
}