package es.codeurjc.board.service;

import es.codeurjc.board.dto.EntradaDTO;
import es.codeurjc.board.model.Entrada;
import es.codeurjc.board.model.Evento;
import es.codeurjc.board.model.User;
import es.codeurjc.board.repositories.EntradaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class EntradaService {

    @Autowired
    private EntradaRepository entradaRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private es.codeurjc.board.repositories.EventoRepository eventoRepository;

    public Collection<Entrada> findAll() {
        return entradaRepository.findAll();
    }

    public Entrada findById(long id) {
        return entradaRepository.findById(id).orElse(null);
    }

    public Collection<Entrada> findByEvento(Long eventoId) {
        return entradaRepository.findAll()
                .stream()
                .filter(e -> e.getEvento() != null &&
                        e.getEvento().getId().equals(eventoId))
                .toList();
    }

    public Entrada crearEntrada(String name, String acceso, String incluye,
                                Double precio, Long eventoId) {

        String error = validarCamposEntrada(name, acceso, incluye, precio);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("El evento con ID " + eventoId + " no existe"));

        Entrada entrada = new Entrada();
        entrada.setName(name);
        entrada.setAcceso(acceso);
        entrada.setIncluye(incluye);
        entrada.setPrecio(precio);
        entrada.setEvento(evento);

        return entradaRepository.save(entrada);
    }

    public Optional<Entrada> actualizarEntrada(Long id, String name, String acceso,
                                               String incluye, Double precio, Long eventoId) {

        return entradaRepository.findById(id).map(entrada -> {

            if (name != null) {
                entrada.setName(name);
            }

            if (acceso != null) {
                entrada.setAcceso(acceso);
            }

            if (incluye != null) {
                entrada.setIncluye(incluye);
            }

            if (precio != null) {
                entrada.setPrecio(precio);
            }

            if (eventoId != null) {
                Evento evento = eventoRepository.findById(eventoId)
                        .orElseThrow(() -> new IllegalArgumentException("El evento con ID " + eventoId + " no existe"));
                entrada.setEvento(evento);
            }

            String error = validarCamposEntrada(
                    entrada.getName(),
                    entrada.getAcceso(),
                    entrada.getIncluye(),
                    entrada.getPrecio()
            );

            if (error != null) {
                throw new IllegalArgumentException(error);
            }

            return entradaRepository.save(entrada);
        });
    }

    public void delete(long id) {
        entradaRepository.deleteById(id);
    }

    public String validarCamposEntrada(String name, String acceso, String incluye, Double precio) {
        if (isBlank(name) || isBlank(acceso) || isBlank(incluye) || precio == null || precio < 0) {
            return "Revisa los campos obligatorios y el precio";
        }
        return null;
    }


    public String comprarEntrada(Long entradaId, User user) {

        Entrada entrada = findById(entradaId);

        if (entrada == null) {
            return "error_entrada_no_existe";
        }

        if (user != null) {

            List<Entrada> entradas = user.getEntradasCompradas();

            if (entradas == null) {
                entradas = new ArrayList<>();
            }

            boolean yaComprada = entradas.stream()
                    .anyMatch(e -> e.getId().equals(entrada.getId()));

            if (yaComprada) {
                return "error_ya_comprada";
            }

            entradas.add(entrada);
            user.setEntradasCompradas(entradas);
            userService.saveUser(user);

            return "success";
        }

        return "error_usuario_no_existe";
    }

    public void deleteEntradaConLimpieza(long id) {

        Entrada entrada = findById(id);

        if (entrada != null) {

            Collection<User> users = userService.findAll();

            for (User user : users) {
                if (user.getEntradasCompradas() != null) {
                    user.getEntradasCompradas().remove(entrada);
                    userService.saveUser(user);
                }
            }

            delete(id);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // ================= REST API =================

    public Page<EntradaDTO> findAllTickets(Pageable pageable) {
        return entradaRepository.findAll(pageable).map(this::toDTO);
    }

    public Optional<EntradaDTO> findTicketById(Long id) {
        return entradaRepository.findById(id).map(this::toDTO);
    }

    public EntradaDTO createTicket(EntradaDTO dto) {

        Entrada entrada = crearEntrada(
                dto.getName(),
                dto.getAccessType(),
                dto.getIncludes(),
                dto.getPrice(),
                dto.getEventId()
        );

        return toDTO(entrada);
    }

    public Optional<EntradaDTO> updateTicket(Long id, EntradaDTO dto) {

        return actualizarEntrada(
                id,
                dto.getName(),
                dto.getAccessType(),
                dto.getIncludes(),
                dto.getPrice(),
                dto.getEventId()
        ).map(this::toDTO);
    }

    public boolean deleteTicket(Long id) {
        if (entradaRepository.existsById(id)) {
            deleteEntradaConLimpieza(id);
            return true;
        }
        return false;
    }

    public List<EntradaDTO> findTicketsByUser(Long userId) {
        User user = userService.findById(userId);
        if (user == null) return List.of();
        return user.getEntradasCompradas().stream()
                .map(this::toDTO)
                .toList();
    }

    private EntradaDTO toDTO(Entrada entrada) {

        EntradaDTO dto = new EntradaDTO();

        dto.setId(entrada.getId());
        dto.setName(entrada.getName());
        dto.setAccessType(entrada.getAcceso());
        dto.setIncludes(entrada.getIncluye());
        dto.setPrice(entrada.getPrecio());

        if (entrada.getEvento() != null) {
            dto.setEventId(entrada.getEvento().getId());
        }

        return dto;
    }
}