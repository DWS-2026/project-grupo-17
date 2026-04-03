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
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private EntradaService entradaService;

    @Autowired
    private UserService userService;

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

        // 🔹 1. obtener entradas del evento
        Collection<Entrada> entradas = entradaService.findByEvento(id);

        // 🔹 2. quitar entradas de usuarios
        for (Entrada entrada : entradas) {

            for (User user : userService.findAll()) {

                if (user.getEntradasCompradas() != null &&
                        user.getEntradasCompradas().contains(entrada)) {

                    user.getEntradasCompradas().remove(entrada);
                    userService.saveUser(user);
                }
            }

            // 🔹 3. borrar entrada
            entradaService.delete(entrada.getId());
        }

        // 🔹 4. borrar evento
        eventoRepository.deleteById(id);
    }
    public List<Evento> findFirst3() {
        return eventoRepository.findTop3By();
    }
}