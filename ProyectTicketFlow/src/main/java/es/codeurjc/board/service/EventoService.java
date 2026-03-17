package es.codeurjc.board.service;

import java.io.IOException;
import java.util.Collection;

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

    public void save(String name, Discoteca discoteca, String descripcion,
                     MultipartFile image, Integer edadRequerida) throws IOException {

        Evento evento = new Evento();

        evento.setName(name);
        evento.setDiscoteca(discoteca);
        evento.setDescripcion(descripcion);
        evento.setEdadRequerida(edadRequerida);

        if (image != null && !image.isEmpty()) {
            evento.setImage(image.getBytes());
        }

        eventoRepository.save(evento);
    }

    public void save(String name, Discoteca discoteca, String descripcion,
                     MultipartFile image, Integer edadRequerida, User owner) throws IOException {

        Evento evento = new Evento();

        evento.setName(name);
        evento.setDiscoteca(discoteca);
        evento.setDescripcion(descripcion);
        evento.setEdadRequerida(edadRequerida);
        evento.setOwner(owner);

        if (image != null && !image.isEmpty()) {
            evento.setImage(image.getBytes());
        }

        eventoRepository.save(evento);
    }

    public void update(long id, String name, Discoteca discoteca,
                       String descripcion, MultipartFile image,
                       Integer edadRequerida) throws IOException {

        Evento evento = eventoRepository.findById(id).orElse(null);

        if (evento != null) {

            evento.setName(name);
            evento.setDiscoteca(discoteca);
            evento.setDescripcion(descripcion);
            evento.setEdadRequerida(edadRequerida);

            if (image != null && !image.isEmpty()) {
                evento.setImage(image.getBytes());
            }

            eventoRepository.save(evento);
        }
    }

    public void delete(long id) {
        eventoRepository.deleteById(id);
    }
}