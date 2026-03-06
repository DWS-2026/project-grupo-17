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

    // AÑADIMOS los parámetros 'calle' y 'descripcion' a la firma del método
    public void save(String name, MultipartFile image, String calle, String descripcion) throws IOException {
        Long id = nextId.getAndIncrement();
        
        // ACTUALIZAMOS el constructor para pasarle todos los datos
        Discoteca d = new Discoteca(id, name, image.getBytes(), calle, descripcion);
        
        discotecas.put(id, d);
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
        discotecas.remove(id);
    }
}