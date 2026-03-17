package es.codeurjc.board.service;

import java.io.IOException;
import java.util.Collection;

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

    public void save(Evento evento) {
        eventoRepository.save(evento);
    }
    public void delete(long id) {
        eventoRepository.deleteById(id);
    }
}