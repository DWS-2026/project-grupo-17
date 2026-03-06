package es.codeurjc.board.service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.board.model.Evento;
import es.codeurjc.board.model.Discoteca; // <-- IMPORTANTE: Añadimos la importación

@Service
public class EventoService {

    private Map<Long, Evento> eventos = new HashMap<>();
    private AtomicLong nextId = new AtomicLong(1);

    public Collection<Evento> findAll() {
        return eventos.values();
    }

    public Evento findById(long id) {
        return eventos.get(id);
    }

    // --- ACTUALIZADO: Añadidos discoteca, descripcion y edadRequerida ---
    public void save(String name, Discoteca discoteca, String descripcion, MultipartFile image, Integer edadRequerida) throws IOException {

        Long id = nextId.getAndIncrement();
        
        // Usamos el nuevo constructor con todos los datos
        Evento evento = new Evento(id, name, discoteca, descripcion, image.getBytes(), edadRequerida);

        eventos.put(id, evento);
    }

    // --- ACTUALIZADO: Añadidos discoteca, descripcion y edadRequerida ---
    public void update(long id, String name, Discoteca discoteca, String descripcion, MultipartFile image, Integer edadRequerida) throws IOException {

        Evento evento = eventos.get(id);

        if (evento != null) {

            // Actualizamos todos los campos
            evento.setName(name);
            evento.setDiscoteca(discoteca);
            evento.setDescripcion(descripcion);
            evento.setEdadRequerida(edadRequerida);

            // La imagen solo se actualiza si el usuario sube una nueva
            if (image != null && !image.isEmpty()) {
                evento.setImage(image.getBytes());
            }
        }
    }

    public void delete(long id) {
        eventos.remove(id);
    }
}