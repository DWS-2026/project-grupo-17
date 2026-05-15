package es.codeurjc.board.service;

import es.codeurjc.board.dto.DiscotecaDTO;
import es.codeurjc.board.model.Discoteca;
import es.codeurjc.board.model.Entrada;
import es.codeurjc.board.model.Evento;
import es.codeurjc.board.model.Image;
import es.codeurjc.board.model.User;
import es.codeurjc.board.repositories.DiscotecaRepository;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class DiscotecaService {

    @Autowired
    private EventoService eventoService;

    @Autowired
    private EntradaService entradaService;

    @Autowired
    private UserService userService;

    @Autowired
    private DiscotecaRepository discotecaRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private FileStorageService fileStorageService;

    public Collection<Discoteca> findAll() {
        return discotecaRepository.findAll();
    }

    public Discoteca findById(long id) {
        return discotecaRepository.findById(id).orElse(null);
    }

    public void save(Discoteca discoteca) {
        discotecaRepository.save(discoteca);
    }

    public void delete(Long id) {

        Discoteca discoteca = findById(id);

        if (discoteca == null) return;

        List<Evento> eventos = discoteca.getEventos();

        for (Evento evento : eventos) {

            Collection<Entrada> entradas =
                    entradaService.findByEvento(evento.getId());

            for (Entrada entrada : entradas) {

                for (User user : userService.findAll()) {

                    if (user.getEntradasCompradas() != null &&
                            user.getEntradasCompradas().contains(entrada)) {

                        user.getEntradasCompradas().remove(entrada);
                        userService.saveUser(user);
                    }
                }

                entradaService.delete(entrada.getId());
            }

            eventoService.delete(evento.getId());
        }

        discotecaRepository.deleteById(id);
    }

    public String validarCamposDiscoteca(String name,
                                         String calle,
                                         String descripcion) {

        if (isBlank(name) || isBlank(calle) || isBlank(descripcion)) {
            return "Los campos nombre, descripcion y calle deben estar rellenos";
        }

        return null;
    }

    public Discoteca crearDiscoteca(
            String name,
            String street,
            String description,
            Long ownerId,
            MultipartFile imageFile,
            MultipartFile flyerFile
    ) throws IOException, SQLException {

        String error =
                validarCamposDiscoteca(name, street, description);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        Discoteca discoteca = new Discoteca();

        discoteca.setName(name);
        discoteca.setCalle(street);
        discoteca.setDescripcion(sanitizarTextoEnriquecido(description));

        if (ownerId != null) {
            User owner = userService.findById(ownerId);
            discoteca.setOwner(owner);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            Image img = imageService.createImageFromFile(imageFile);
            discoteca.setImage(img);
        }

        if (flyerFile != null && !flyerFile.isEmpty()) {
            String flyerName =
                    fileStorageService.storeFile(flyerFile);
            discoteca.setFlyer(flyerName);
        }

        return discotecaRepository.save(discoteca);
    }

    public Optional<Discoteca> actualizarDiscoteca(
            Long id,
            String name,
            String street,
            String description,
            Long ownerId,
            MultipartFile imageFile,
            MultipartFile flyerFile,
            boolean removeImage,
            boolean removeFlyer
    ) throws IOException, SQLException {

        Discoteca discoteca = findById(id);

        if (discoteca == null) {
            return Optional.empty();
        }

        String error =
                validarCamposDiscoteca(name, street, description);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        discoteca.setName(name);
        discoteca.setCalle(street);
        discoteca.setDescripcion(sanitizarTextoEnriquecido(description));

        if (ownerId != null) {
            User owner = userService.findById(ownerId);
            discoteca.setOwner(owner);
        }

        // Imagen
        if (removeImage) {

            if (discoteca.getImage() != null) {
                imageService.deleteImage(
                        discoteca.getImage().getId()
                );
            }

            discoteca.setImage(null);

        } else if (imageFile != null &&
                !imageFile.isEmpty()) {

            if (discoteca.getImage() != null) {

                imageService.replaceImageFile(
                        discoteca.getImage().getId(),
                        imageFile
                );

            } else {

                Image img =
                        imageService.createImageFromFile(
                                imageFile
                        );

                discoteca.setImage(img);
            }
        }

        // Flyer
        if (removeFlyer) {

            if (discoteca.getFlyer() != null) {
                fileStorageService.deleteFile(
                        discoteca.getFlyer()
                );
            }

            discoteca.setFlyer(null);

        } else if (flyerFile != null &&
                !flyerFile.isEmpty()) {

            if (discoteca.getFlyer() != null) {
                fileStorageService.deleteFile(
                        discoteca.getFlyer()
                );
            }

            String flyerName =
                    fileStorageService.storeFile(
                            flyerFile
                    );

            discoteca.setFlyer(flyerName);
        }

        save(discoteca);

        return Optional.of(discoteca);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String sanitizarTextoEnriquecido(String texto) {
        return texto != null ? Jsoup.clean(texto, Safelist.relaxed()) : null;
    }

    // ================= REST API =================

    public Page<DiscotecaDTO> findAllClubs(Pageable pageable) {
        return discotecaRepository.findAll(pageable).map(this::toDTO);
    }

    public Optional<DiscotecaDTO> findClubById(Long id) {
        return discotecaRepository.findById(id).map(this::toDTO);
    }

    public DiscotecaDTO createClub(DiscotecaDTO dto) {

        try {

            Discoteca discoteca = crearDiscoteca(
                    dto.getName(),
                    dto.getStreet(),
                    dto.getDescription(),
                    dto.getOwnerId(),
                    null,
                    null
            );

            return toDTO(discoteca);

        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public DiscotecaDTO createClubWithImageAndFlyer(
            DiscotecaDTO dto,
            MultipartFile imageFile,
            MultipartFile flyerFile
    ) throws IOException, SQLException {

        Discoteca discoteca = crearDiscoteca(
                dto.getName(),
                dto.getStreet(),
                dto.getDescription(),
                dto.getOwnerId(),
                imageFile,
                flyerFile
        );

        return toDTO(discoteca);
    }

    public Optional<DiscotecaDTO> updateClubWithImageAndFlyer(Long id,
                                                              String name,
                                                              String street,
                                                              String description,
                                                              Long ownerId,
                                                              MultipartFile imageFile,
                                                              MultipartFile flyerFile,
                                                              boolean removeImage,
                                                              boolean removeFlyer) throws IOException, SQLException {

        return actualizarDiscoteca(
                id,
                name,
                street,
                description,
                ownerId,
                imageFile,
                flyerFile,
                removeImage,
                removeFlyer
        ).map(this::toDTO);
    }

    public Optional<DiscotecaDTO> updateClub(Long id, DiscotecaDTO dto) {

        try {
            return actualizarDiscoteca(
                    id,
                    dto.getName(),
                    dto.getStreet(),
                    dto.getDescription(),
                    dto.getOwnerId(),
                    null,
                    null,
                    false,
                    false
            ).map(this::toDTO);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public boolean deleteClub(Long id) {

        if (discotecaRepository.existsById(id)) {
            delete(id);
            return true;
        }

        return false;
    }

    public byte[] getClubImage(Long id) throws SQLException, IOException {

        Discoteca discoteca = findById(id);

        if (discoteca == null || discoteca.getImage() == null) {
            return null;
        }

        Image image = discoteca.getImage();
        
        // Si está en disco, leerla desde ahí
        if (image.getFileName() != null && !image.getFileName().isEmpty()) {
            try {
                org.springframework.core.io.Resource resource = 
                    fileStorageService.getFileAsResource(image.getFileName());
                return resource.getInputStream().readAllBytes();
            } catch (IOException e) {
                System.err.println("Error reading file from disk: " + e.getMessage());
                // Continuar con blob si existe
            }
        }
        
        // Si está en BD (backwards compatibility)
        if (image.getImageFile() != null) {
            Blob blob = image.getImageFile();
            return blob.getBytes(1, (int) blob.length());
        }

        return null;
    }

    private DiscotecaDTO toDTO(Discoteca discoteca) {

        DiscotecaDTO dto = new DiscotecaDTO();

        dto.setId(discoteca.getId());
        dto.setName(discoteca.getName());
        dto.setStreet(discoteca.getCalle());
        dto.setDescription(discoteca.getDescripcion());

        if (discoteca.getImage() != null) {
            dto.setImageURL("/api/v1/clubs/" + discoteca.getId() + "/image");
        }

        if (discoteca.getOwner() != null) {
            dto.setOwnerId(discoteca.getOwner().getId());
        }

        if (discoteca.getFlyer() != null) {
            dto.setFlyerFileName(discoteca.getFlyer());
        }

        return dto;
    }
}