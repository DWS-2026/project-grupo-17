package es.codeurjc.board.service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import es.codeurjc.board.model.Entrada;
import es.codeurjc.board.model.Evento;
import es.codeurjc.board.model.User;
import es.codeurjc.board.repositories.DiscotecaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.board.model.Discoteca;

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

        // 🔹 1. obtener eventos
        List<Evento> eventos = discoteca.getEventos();

        for (Evento evento : eventos) {

            // 🔹 2. obtener entradas del evento
            Collection<Entrada> entradas = entradaService.findByEvento(evento.getId());

            for (Entrada entrada : entradas) {

                // 🔹 3. quitar entradas de usuarios
                for (User user : userService.findAll()) {

                    if (user.getEntradasCompradas() != null &&
                            user.getEntradasCompradas().contains(entrada)) {

                        user.getEntradasCompradas().remove(entrada);
                        userService.saveUser(user);
                    }
                }

                // 🔹 4. borrar entrada
                entradaService.delete(entrada.getId());
            }

            // 🔹 5. borrar evento
            eventoService.delete(evento.getId());
        }

        // 🔹 6. borrar discoteca
        discotecaRepository.deleteById(id);
    }
}