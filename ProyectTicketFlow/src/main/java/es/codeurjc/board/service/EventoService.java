package es.codeurjc.board.service;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import es.codeurjc.board.dto.EventoDTO;
import es.codeurjc.board.model.Discoteca;
import es.codeurjc.board.model.Entrada;
import es.codeurjc.board.model.Evento;
import es.codeurjc.board.model.Image;
import es.codeurjc.board.model.User;
import es.codeurjc.board.repositories.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private EntradaService entradaService;

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private es.codeurjc.board.repositories.DiscotecaRepository discotecaRepository;

    public Collection<Evento> findAll() {
        return eventoRepository.findAll();
    }

    public Evento findById(long id) {
        return eventoRepository.findById(id).orElse(null);
    }

    public Collection<Evento> findByDiscoteca(Long discotecaId) {
        return eventoRepository.findAll()
                .stream()
                .filter(evento -> evento.getDiscoteca() != null &&
                        evento.getDiscoteca().getId().equals(discotecaId))
                .toList();
    }

    public void save(Evento evento) {
        eventoRepository.save(evento);
    }

    public void delete(Long id) {

        Evento evento = findById(id);

        if (evento == null) return;

        Collection<Entrada> entradas = entradaService.findByEvento(id);

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

        eventoRepository.deleteById(id);
    }

    public List<Evento> findFirst3() {
        return eventoRepository.findTop3By();
    }

    public String validarCamposEvento(String name, String descripcion, Integer edadRequerida) {
        if (isBlank(name) || isBlank(descripcion) || edadRequerida == null || edadRequerida < 0) {
            return "Please check the fields: name, description and required age";
        }
        return null;
    }

    public Evento crearEvento(
            String name,
            String descripcion,
            Integer edadRequerida,
            Long discotecaId,
            MultipartFile imageFile
    ) throws IOException, SQLException {

        String error = validarCamposEvento(
                name,
                descripcion,
                edadRequerida
        );

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        Discoteca discoteca = discotecaRepository.findById(discotecaId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "The club with ID " + discotecaId + " does not exist"
                        )
                );

        Evento evento = new Evento();

        evento.setName(name);
        evento.setDescripcion(descripcion);
        evento.setEdadRequerida(edadRequerida);
        evento.setDiscoteca(discoteca);

        if (imageFile != null && !imageFile.isEmpty()) {
            Image img = imageService.createImageFromFile(imageFile);
            evento.setImage(img);
        }

        return eventoRepository.save(evento);
    }

    public Optional<Evento> actualizarEvento(
            Long id,
            String name,
            String descripcion,
            Integer edadRequerida,
            Long discotecaId,
            MultipartFile imageFile
    ) throws IOException, SQLException {

        Evento evento = findById(id);

        if (evento == null) {
            return Optional.empty();
        }

        if (name != null) {
            evento.setName(name);
        }

        if (descripcion != null) {
            evento.setDescripcion(descripcion);
        }

        if (edadRequerida != null) {
            evento.setEdadRequerida(edadRequerida);
        }

        String error = validarCamposEvento(
                evento.getName(),
                evento.getDescripcion(),
                evento.getEdadRequerida()
        );

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        if (discotecaId != null) {
            Discoteca discoteca = discotecaRepository.findById(discotecaId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "The club with ID " + discotecaId + " does not exist"
                            )
                    );

            evento.setDiscoteca(discoteca);
        }

        if (imageFile != null && !imageFile.isEmpty()) {

            if (evento.getImage() != null) {

                imageService.replaceImageFile(
                        evento.getImage().getId(),
                        imageFile
                );

            } else {

                Image img =
                        imageService.createImageFromFile(imageFile);

                evento.setImage(img);
            }
        }

        return Optional.of(eventoRepository.save(evento));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // ================= REST API =================

    public Page<EventoDTO> findAllEvents(Pageable pageable) {
        return eventoRepository.findAll(pageable).map(this::toDTO);
    }

    public Optional<EventoDTO> findEventById(Long id) {
        return eventoRepository.findById(id).map(this::toDTO);
    }

    public EventoDTO createEvent(EventoDTO eventoDTO) {

        try {

            Evento evento = crearEvento(
                    eventoDTO.getName(),
                    eventoDTO.getDescription(),
                    eventoDTO.getRequiredAge(),
                    eventoDTO.getDiscotecaId(),
                    null
            );

            return toDTO(evento);

        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public Optional<EventoDTO> updateEvent(Long id,
                                           EventoDTO eventoDTO) {

        try {

            return actualizarEvento(
                    id,
                    eventoDTO.getName(),
                    eventoDTO.getDescription(),
                    eventoDTO.getRequiredAge(),
                    eventoDTO.getDiscotecaId(),
                    null
            ).map(this::toDTO);

        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public boolean deleteEvent(Long id) {
        if (eventoRepository.existsById(id)) {
            delete(id);
            return true;
        }
        return false;
    }

    public byte[] getEventImage(Long id) throws SQLException, IOException {

        Evento evento = findById(id);

        if (evento == null || evento.getImage() == null) {
            return null;
        }

        Image image = evento.getImage();
        
        // If stored on disk, read from there
        if (image.getFileName() != null && !image.getFileName().isEmpty()) {
            try {
                org.springframework.core.io.Resource resource =
                    fileStorageService.getFileAsResource(image.getFileName());
                return resource.getInputStream().readAllBytes();
            } catch (IOException e) {
                System.err.println("Error reading file from disk: " + e.getMessage());
                // Continue with blob if it exists
            }
        }

        // If stored in DB (backwards compatibility)
        if (image.getImageFile() != null) {
            Blob blob = image.getImageFile();
            return blob.getBytes(1, (int) blob.length());
        }

        return null;
    }

    private EventoDTO toDTO(Evento evento) {

        EventoDTO dto = new EventoDTO();

        dto.setId(evento.getId());
        dto.setName(evento.getName());
        dto.setDescription(evento.getDescripcion());
        dto.setRequiredAge(evento.getEdadRequerida());

        if (evento.getDiscoteca() != null) {
            dto.setDiscotecaId(evento.getDiscoteca().getId());
        }

        return dto;
    }
}