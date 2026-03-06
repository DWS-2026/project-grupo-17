package es.codeurjc.board.service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.board.model.Evento;

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

    public void save(String name, MultipartFile image) throws IOException {

        Long id = nextId.getAndIncrement();
        Evento evento = new Evento(id, name, image.getBytes());

        eventos.put(id, evento);
    }

    public void update(long id, String name, MultipartFile image) throws IOException {

        Evento evento = eventos.get(id);

        if (evento != null) {

            evento.setName(name);

            if (image != null && !image.isEmpty()) {
                evento.setImage(image.getBytes());
            }
        }
    }

    public void delete(long id) {
        eventos.remove(id);
    }
}