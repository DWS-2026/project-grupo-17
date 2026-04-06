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
}
