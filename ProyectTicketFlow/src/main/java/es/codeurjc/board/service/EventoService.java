package es.codeurjc.board.service;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

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
            return "Revisa los campos: nombre, descripcion y edad requerida";
        }
        return null;
    }

    public void createEventoWithImage(Evento evento, MultipartFile imageFile, Discoteca discoteca)
            throws IOException, SQLException {

        evento.setDiscoteca(discoteca);

        if (imageFile != null && !imageFile.isEmpty()) {
            Image img = imageService.createImageFromFile(imageFile);
            evento.setImage(img);
        }

        save(evento);
    }

    public void updateEventoWithImage(Long id, Evento eventoForm, MultipartFile image,
                                      Discoteca nuevaDiscoteca)
            throws IOException, SQLException {

        Evento evento = findById(id);

        if (evento != null) {
            evento.setName(eventoForm.getName());
            evento.setDescripcion(eventoForm.getDescripcion());
            evento.setEdadRequerida(eventoForm.getEdadRequerida());

            if (nuevaDiscoteca != null) {
                evento.setDiscoteca(nuevaDiscoteca);
            }

            if (image != null && !image.isEmpty()) {
                if (evento.getImage() != null) {
                    imageService.replaceImageFile(evento.getImage().getId(), image);
                } else {
                    Image img = imageService.createImageFromFile(image);
                    evento.setImage(img);
                }
            }

            save(evento);
        }
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

        Evento evento = new Evento();

        evento.setName(eventoDTO.getName());
        evento.setDescripcion(eventoDTO.getDescription());
        evento.setEdadRequerida(
                eventoDTO.getRequiredAge() != null ? eventoDTO.getRequiredAge() : 18
        );

        if (eventoDTO.getDiscotecaId() != null) {
            es.codeurjc.board.model.Discoteca discoteca = discotecaRepository.findById(eventoDTO.getDiscotecaId())
                    .orElseThrow(() -> new IllegalArgumentException("La discoteca con ID " + eventoDTO.getDiscotecaId() + " no existe"));
            evento.setDiscoteca(discoteca);
        }

        eventoRepository.save(evento);

        return toDTO(evento);
    }

    public Optional<EventoDTO> updateEvent(Long id, EventoDTO eventoDTO) {

        return eventoRepository.findById(id).map(evento -> {

            if (eventoDTO.getName() != null) {
                evento.setName(eventoDTO.getName());
            }

            if (eventoDTO.getDescription() != null) {
                evento.setDescripcion(eventoDTO.getDescription());
            }

            if (eventoDTO.getRequiredAge() != null) {
                evento.setEdadRequerida(eventoDTO.getRequiredAge());
            }

            if (eventoDTO.getDiscotecaId() != null) {
                es.codeurjc.board.model.Discoteca discoteca = discotecaRepository.findById(eventoDTO.getDiscotecaId())
                        .orElseThrow(() -> new IllegalArgumentException("La discoteca con ID " + eventoDTO.getDiscotecaId() + " no existe"));
                evento.setDiscoteca(discoteca);
            }

            eventoRepository.save(evento);

            return toDTO(evento);
        });
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