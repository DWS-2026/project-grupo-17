package es.codeurjc.board.service;

import java.io.IOException;
import java.util.Collection;

import es.codeurjc.board.repositories.EntradaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.board.model.Entrada;
import es.codeurjc.board.model.Evento;

@Service
public class EntradaService {

    @Autowired
    private EntradaRepository entradaRepository;

    public Collection<Entrada> findAll() {
        return entradaRepository.findAll();
    }

    public Entrada findById(long id) {
        return entradaRepository.findById(id).orElse(null);
    }

    public Collection<Entrada> findByEvento(Long eventoId) {

        return entradaRepository.findAll()
                .stream()
                .filter(entrada -> entrada.getEvento() != null &&
                        entrada.getEvento().getId().equals(eventoId))
                .toList();
    }

    public void save(String name, Evento evento, String descripcion,
                     Double precio, Integer edadRequerida, MultipartFile image) throws IOException {

        Entrada entrada = new Entrada();

        entrada.setName(name);
        entrada.setEvento(evento);
        entrada.setDescripcion(descripcion);
        entrada.setPrecio(precio);
        entrada.setEdadRequerida(edadRequerida);

        if (image != null && !image.isEmpty()) {
            entrada.setImage(image.getBytes());
        }

        entradaRepository.save(entrada);
    }

    public void update(long id, String name, Evento evento,
                       String descripcion, Double precio, Integer edadRequerida, MultipartFile image) throws IOException {

        Entrada entrada = entradaRepository.findById(id).orElse(null);

        if (entrada != null) {

            entrada.setName(name);
            entrada.setEvento(evento);
            entrada.setDescripcion(descripcion);
            entrada.setPrecio(precio);
            entrada.setEdadRequerida(edadRequerida);

            if (image != null && !image.isEmpty()) {
                entrada.setImage(image.getBytes());
            }

            entradaRepository.save(entrada);
        }
    }

    public void delete(long id) {
        entradaRepository.deleteById(id);
    }
}