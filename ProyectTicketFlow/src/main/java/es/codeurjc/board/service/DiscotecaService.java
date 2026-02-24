package es.codeurjc.board.service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.board.model.Discoteca;

@Service
public class DiscotecaService {

    private Map<Long, Discoteca> discotecas = new HashMap<>();
    private AtomicLong nextId = new AtomicLong(1);

    public Collection<Discoteca> findAll() {
        return discotecas.values();
    }

    public Discoteca findById(long id) {
        return discotecas.get(id);
    }

    public void save(String name, MultipartFile image) throws IOException {
        Long id = nextId.getAndIncrement();
        Discoteca d = new Discoteca(id, name, image.getBytes());
        discotecas.put(id, d);
    }

    public void delete(long id) {
        discotecas.remove(id);
    }
}