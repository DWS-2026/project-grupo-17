package es.codeurjc.board.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import es.codeurjc.board.model.Image;
import es.codeurjc.board.repositories.ImageRepository;

@Service
/**
 * Servicio de imagenes:
 * encapsula la creacion, lectura, reemplazo y borrado de blobs de imagen.
 */
public class ImageService {

	@Autowired
	private ImageRepository imageRepository;

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

	// Devuelve un recurso de solo lectura con el contenido binario de la imagen.
	public Resource getImageFile(long id) throws SQLException {

		Image image = imageRepository.findById(id).orElseThrow();

		if (image.getImageFile() != null) {
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

	// Borra una imagen de BD y devuelve la entidad eliminada.
	public Image deleteImage(long id) {
		Image image = imageRepository.findById(id).orElseThrow();
		imageRepository.deleteById(id);
		return image;
	}
}