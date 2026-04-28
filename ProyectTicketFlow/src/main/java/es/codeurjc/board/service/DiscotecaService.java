package es.codeurjc.board.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import es.codeurjc.board.model.Entrada;
import es.codeurjc.board.model.Evento;
import es.codeurjc.board.model.User;
import es.codeurjc.board.model.Image;
import es.codeurjc.board.repositories.DiscotecaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.board.model.Discoteca;
import es.codeurjc.board.dto.DiscotecaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
/**
 * Servicio de discotecas:
 * gestiona consultas, guardado y borrado completo de discoteca con su grafo de datos.
 */
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

    // Devuelve todas las discotecas.
    public Collection<Discoteca> findAll() {
        return discotecaRepository.findAll();
    }

    // Busca discoteca por id; si no existe devuelve null.
    public Discoteca findById(long id) {
        return discotecaRepository.findById(id).orElse(null);
    }

    // Guarda o actualiza una discoteca.
    public void save(Discoteca discoteca) {
        discotecaRepository.save(discoteca);
    }

    // Elimina discoteca limpiando antes eventos, entradas y compras asociadas.
    public void delete(Long id) {

        Discoteca discoteca = findById(id);
        if (discoteca == null) return;

        // 1. Obtener eventos
        List<Evento> eventos = discoteca.getEventos();

        for (Evento evento : eventos) {

            // 2. Obtener entradas del evento
            Collection<Entrada> entradas = entradaService.findByEvento(evento.getId());

            for (Entrada entrada : entradas) {

                // 3. Quitar entradas de usuarios
                for (User user : userService.findAll()) {

                    if (user.getEntradasCompradas() != null &&
                            user.getEntradasCompradas().contains(entrada)) {

                        user.getEntradasCompradas().remove(entrada);
                        userService.saveUser(user);
                    }
                }

                // 4. Borrar entrada
                entradaService.delete(entrada.getId());
            }

            // 5. Borrar evento
            eventoService.delete(evento.getId());
        }

        // 6. Borrar discoteca
        discotecaRepository.deleteById(id);
    }

    // Valida los campos obligatorios de una discoteca
    public String validarCamposDiscoteca(String name, String calle, String descripcion) {
        if (isBlank(name) || isBlank(calle) || isBlank(descripcion)) {
            return "Todos los campos obligatorios deben estar rellenos";
        }
        return null;
    }

    // Crea una discoteca con imagen asociada al propietario
    public void createDiscotecaWithImage(Discoteca discoteca, MultipartFile imageFile, User owner) throws IOException, SQLException {
        discoteca.setOwner(owner);

        if (imageFile != null && !imageFile.isEmpty()) {
            Image img = imageService.createImage(imageFile.getInputStream());
            discoteca.setImage(img);
        }

        save(discoteca);
    }

    // Edita una discoteca existente con manejo de imagen
    public void editDiscotecaWithImage(Long id, Discoteca discotecaForm, boolean removeImage, MultipartFile imageFile) throws IOException, SQLException {
        Discoteca discoteca = findById(id);

        if (discoteca != null) {
            discoteca.setName(discotecaForm.getName());
            discoteca.setCalle(discotecaForm.getCalle());
            discoteca.setDescripcion(discotecaForm.getDescripcion());

            if (removeImage) {
                discoteca.setImage(null);
            } else if (imageFile != null && !imageFile.isEmpty()) {
                Image img = imageService.createImage(imageFile.getInputStream());
                discoteca.setImage(img);
            }

            save(discoteca);
        }
    }

    // Utilidad privada para validar campos de texto obligatorios
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // REST API methods
    public Page<DiscotecaDTO> findAllClubs(Pageable pageable) {
        return discotecaRepository.findAll(pageable).map(this::toDTO);
    }

    public Optional<DiscotecaDTO> findClubById(Long id) {
        return discotecaRepository.findById(id).map(this::toDTO);
    }

    public DiscotecaDTO createClub(DiscotecaDTO dto) {
        Discoteca discoteca = new Discoteca();
        discoteca.setName(dto.getName());
        discoteca.setCalle(dto.getStreet());
        discoteca.setDescripcion(dto.getDescription());
        if (dto.getOwnerId() != null) {
            discoteca.setOwner(userService.findById(dto.getOwnerId()));
        }
        discotecaRepository.save(discoteca);
        return toDTO(discoteca);
    }

    public Optional<DiscotecaDTO> updateClub(Long id, DiscotecaDTO dto) {
        return discotecaRepository.findById(id).map(discoteca -> {
            discoteca.setName(dto.getName());
            discoteca.setCalle(dto.getStreet());
            discoteca.setDescripcion(dto.getDescription());
            discotecaRepository.save(discoteca);
            return toDTO(discoteca);
        });
    }

    public boolean deleteClub(Long id) {
        if (discotecaRepository.existsById(id)) {
            delete(id); // Use existing delete to clean up references
            return true;
        }
        return false;
    }


    private DiscotecaDTO toDTO(Discoteca discoteca) {
        DiscotecaDTO dto = new DiscotecaDTO();
        dto.setId(discoteca.getId());
        dto.setName(discoteca.getName());
        dto.setStreet(discoteca.getCalle());
        dto.setDescription(discoteca.getDescripcion());
        if (discoteca.getOwner() != null) dto.setOwnerId(discoteca.getOwner().getId());
        return dto;
    }
}