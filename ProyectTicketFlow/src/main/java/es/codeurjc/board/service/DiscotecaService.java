package es.codeurjc.board.service;

import es.codeurjc.board.dto.DiscotecaDTO;
import es.codeurjc.board.model.Discoteca;
import es.codeurjc.board.model.Entrada;
import es.codeurjc.board.model.Evento;
import es.codeurjc.board.model.Image;
import es.codeurjc.board.model.User;
import es.codeurjc.board.repositories.DiscotecaRepository;
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

    public void createDiscotecaWithImage(Discoteca discoteca,
                                         MultipartFile imageFile,
                                         User owner)
            throws IOException, SQLException {

        discoteca.setOwner(owner);

        if (imageFile != null && !imageFile.isEmpty()) {
            Image img = imageService.createImage(imageFile.getInputStream());
            discoteca.setImage(img);
        }

        save(discoteca);
    }

    public void editDiscotecaWithImage(Long id,
                                       Discoteca discotecaForm,
                                       boolean removeImage,
                                       MultipartFile imageFile)
            throws IOException, SQLException {

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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // ================= REST API =================

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

            if (dto.getName() != null) {
                discoteca.setName(dto.getName());
            }

            if (dto.getStreet() != null) {
                discoteca.setCalle(dto.getStreet());
            }

            if (dto.getDescription() != null) {
                discoteca.setDescripcion(dto.getDescription());
            }

            if (dto.getOwnerId() != null) {
                User owner = userService.findById(dto.getOwnerId());
                discoteca.setOwner(owner);
            }

            discotecaRepository.save(discoteca);

            return toDTO(discoteca);
        });
    }

    public boolean deleteClub(Long id) {

        if (discotecaRepository.existsById(id)) {
            delete(id);
            return true;
        }

        return false;
    }

    public byte[] getClubImage(Long id) throws SQLException {

        Discoteca discoteca = findById(id);

        if (discoteca == null || discoteca.getImage() == null) {
            return null;
        }

        Blob blob = discoteca.getImage().getImageFile();

        return blob.getBytes(1, (int) blob.length());
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

        return dto;
    }
}