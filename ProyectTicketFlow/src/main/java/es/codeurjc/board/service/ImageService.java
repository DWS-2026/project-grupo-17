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
 * Image service:
 * encapsulates the creation, reading, replacement and deletion of image files.
 * Supports storage on disk and in DB for compatibility.
 */
public class ImageService {

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private FileStorageService fileStorageService;

	// Gets the Image entity by id.
	public Image getImage(long id) {
		return imageRepository.findById(id).orElseThrow();
	}

	// Creates and persists a new image by reading all bytes from the InputStream.
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
	 * Creates a new image from a MultipartFile saving it to disk.
	 * @param multipartFile Uploaded file
	 * @return Image entity with reference to the file on disk
	 */
	public Image createImageFromFile(MultipartFile multipartFile) throws IOException {
		if (multipartFile == null || multipartFile.isEmpty()) {
			return null;
		}

		// Save file to disk
		String uniqueFileName = fileStorageService.storeFile(multipartFile);

		// Create Image entity with reference to the file
		Image image = new Image(uniqueFileName, multipartFile.getOriginalFilename());
		imageRepository.save(image);

		return image;
	}

	// Returns a read-only resource with the binary content of the image.
	public Resource getImageFile(long id) throws SQLException, IOException {

		Image image = imageRepository.findById(id).orElseThrow();

		// If it has a fileName (stored on disk), return from disk
		if (image.getFileName() != null && !image.getFileName().isEmpty()) {
			return fileStorageService.getFileAsResource(image.getFileName());
		}
		// Otherwise, try to return from DB (backwards compatibility)
		else if (image.getImageFile() != null) {
			return new InputStreamResource(image.getImageFile().getBinaryStream());
		} else {
			throw new RuntimeException("Image file not found");
		}
	}

	// Replaces the content of an existing image.
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
	 * Replaces the image of an entity with a new file.
	 * @param id Image ID
	 * @param multipartFile New file
	 * @return Updated Image entity
	 */
	public Image replaceImageFile(long id, MultipartFile multipartFile) throws IOException {
		Image image = imageRepository.findById(id).orElseThrow();

		// Delete previous file if it exists
		if (image.getFileName() != null && !image.getFileName().isEmpty()) {
			try {
				fileStorageService.deleteFile(image.getFileName());
			} catch (IOException e) {
				// Log but continue
				System.err.println("Error deleting old file: " + e.getMessage());
			}
		}

		// Save new file
		String uniqueFileName = fileStorageService.storeFile(multipartFile);
		image.setFileName(uniqueFileName);
		image.setOriginalFileName(multipartFile.getOriginalFilename());

		imageRepository.save(image);

		return image;
	}

	// Deletes an image from DB and returns the deleted entity.
	public Image deleteImage(long id) {
		Image image = imageRepository.findById(id).orElseThrow();

		// Delete file from disk if it exists
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