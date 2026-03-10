package es.codeurjc.board.service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import es.codeurjc.board.repositories.DiscotecaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.board.model.Discoteca;

@Service
public class DiscotecaService {


    @Autowired
    private DiscotecaRepository discotecaRepository;

    private Map<Long, Discoteca> discotecas = new HashMap<>();
    private AtomicLong nextId = new AtomicLong(1);

    public Collection<Discoteca> findAll() {
        return discotecaRepository.findAll();
    }

    public Discoteca findById(long id) {
        return discotecaRepository.findById(id).orElse(null);
    }

    // AÑADIMOS los parámetros 'calle' y 'descripcion' a la firma del método
    public void save(String name, MultipartFile image, String calle, String descripcion) throws IOException {
        Discoteca d = new Discoteca();
        d.setName(name);
        d.setImage(image.getBytes());
        d.setCalle(calle);
        d.setDescripcion(descripcion);


        discotecaRepository.save(d);
    }

    public void update(long id, String name, MultipartFile image, String calle, String descripcion) throws IOException {

        Discoteca d = discotecas.get(id);

        if (d != null) {
            d.setName(name);
            d.setCalle(calle);
            d.setDescripcion(descripcion);

            if (!image.isEmpty()) {
                d.setImage(image.getBytes());
            }
        }
    }

    public void delete(long id) {
        discotecaRepository.deleteById(id);
    }
}