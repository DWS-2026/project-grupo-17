package es.codeurjc.board.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import es.codeurjc.board.model.Entrada;
import es.codeurjc.board.model.User;
import es.codeurjc.board.repositories.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.board.model.Evento;
import es.codeurjc.board.model.Discoteca;

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
}