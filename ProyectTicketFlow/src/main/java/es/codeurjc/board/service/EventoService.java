package es.codeurjc.board.service;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

import es.codeurjc.board.model.Entrada;
import es.codeurjc.board.model.User;
import es.codeurjc.board.repositories.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.board.model.Evento;
import es.codeurjc.board.model.Discoteca;
import es.codeurjc.board.model.Image;
import es.codeurjc.board.dto.EventoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Service
/**
 * Servicio de eventos:
 * ofrece consultas, guardado y borrado seguro eliminando relaciones con entradas.
 */
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private EntradaService entradaService;

    @Autowired
    private UserService userService;

    @Autowired
    private es.codeurjc.board.repositories.DiscotecaRepository discotecaRepository;

    // Devuelve todos los eventos.
    public Collection<Evento> findAll() {
        return eventoRepository.findAll();
    }

    // Busca evento por id; si no existe devuelve null.
    public Evento findById(long id) {
        return eventoRepository.findById(id).orElse(null);
    }

    // Filtra eventos por discoteca.
    public Collection<Evento> findByDiscoteca(Long discotecaId) {

        return eventoRepository.findAll()
                .stream()
                .filter(evento -> evento.getDiscoteca() != null &&
                        evento.getDiscoteca().getId().equals(discotecaId))
                .toList();
    }

    // Guarda o actualiza un evento.
    public void save(Evento evento) {
        eventoRepository.save(evento);
    }


    // Elimina evento y limpia primero entradas asociadas y sus referencias en usuarios.
    public void delete(Long id) {

        Evento evento = findById(id);

        if (evento == null) return;

        // 1. Obtener entradas del evento
        Collection<Entrada> entradas = entradaService.findByEvento(id);

        // 2. Quitar entradas de usuarios
        for (Entrada entrada : entradas) {

            for (User user : userService.findAll()) {

                if (user.getEntradasCompradas() != null &&
                        user.getEntradasCompradas().contains(entrada)) {

                    user.getEntradasCompradas().remove(entrada);
                    userService.saveUser(user);
                }
            }

            // 3. Borrar entrada
            entradaService.delete(entrada.getId());
        }

        // 4. Borrar evento
        eventoRepository.deleteById(id);
    }

    // Obtiene tres eventos para la portada.
    public List<Evento> findFirst3() {
        return eventoRepository.findTop3By();
    }

    // Valida los campos obligatorios de un evento
    public String validarCamposEvento(String name, String descripcion, Integer edadRequerida) {
        if (isBlank(name) || isBlank(descripcion) || edadRequerida == null || edadRequerida < 0) {
            return "Revisa los campos: nombre, descripcion y edad requerida";
        }
        return null;
    }

    // Crea un evento con imagen vinculado a una discoteca
    public void createEventoWithImage(Evento evento, MultipartFile imageFile, Discoteca discoteca) throws IOException, SQLException {
        evento.setDiscoteca(discoteca);

        if (imageFile != null && !imageFile.isEmpty()) {
            byte[] bytes = imageFile.getBytes();
            Blob blob = new SerialBlob(bytes);
            Image img = new Image(blob);
            evento.setImage(img);
        }

        save(evento);
    }

    // Actualiza un evento existente con manejo de imagen
    public void updateEventoWithImage(Long id, Evento eventoForm, MultipartFile image, Discoteca nuevaDiscoteca) throws IOException, SQLException {
        Evento evento = findById(id);

        if (evento != null) {
            evento.setName(eventoForm.getName());
            evento.setDescripcion(eventoForm.getDescripcion());
            evento.setEdadRequerida(eventoForm.getEdadRequerida());

            if (nuevaDiscoteca != null) {
                evento.setDiscoteca(nuevaDiscoteca);
            }

            if (image != null && !image.isEmpty()) {
                byte[] bytes = image.getInputStream().readAllBytes();
                Blob blob = new SerialBlob(bytes);
                Image img = new Image(blob);
                evento.setImage(img);
            }

            save(evento);
        }
    }

    // Utilidad privada para validar campos de texto obligatorios
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // REST API methods
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
        evento.setEdadRequerida(eventoDTO.getRequiredAge() != null ? eventoDTO.getRequiredAge() : 18);
        if (eventoDTO.getDiscotecaId() != null) {
            discotecaRepository.findById(eventoDTO.getDiscotecaId()).ifPresent(evento::setDiscoteca);
        }
        eventoRepository.save(evento);
        return toDTO(evento);
    }

    public Optional<EventoDTO> updateEvent(Long id, EventoDTO eventoDTO) {
        return eventoRepository.findById(id).map(evento -> {
            evento.setName(eventoDTO.getName());
            evento.setDescripcion(eventoDTO.getDescription());
            if (eventoDTO.getRequiredAge() != null) evento.setEdadRequerida(eventoDTO.getRequiredAge());
            eventoRepository.save(evento);
            return toDTO(evento);
        });
    }

    public boolean deleteEvent(Long id) {
        if (eventoRepository.existsById(id)) {
            delete(id); // Use existing delete to clear references
            return true;
        }
        return false;
    }

    public byte[] getEventImage(Long id) throws SQLException {

        Evento evento = findById(id);

        if (evento == null || evento.getImage() == null) {
            return null;
        }

        Blob blob = evento.getImage().getImageFile();

        return blob.getBytes(1, (int) blob.length());
    }

    private EventoDTO toDTO(Evento evento) {
        EventoDTO dto = new EventoDTO();
        dto.setId(evento.getId());
        dto.setName(evento.getName());
        dto.setDescription(evento.getDescripcion());
        dto.setRequiredAge(evento.getEdadRequerida());
        if (evento.getDiscoteca() != null) dto.setDiscotecaId(evento.getDiscoteca().getId());
        return dto;
    }
}