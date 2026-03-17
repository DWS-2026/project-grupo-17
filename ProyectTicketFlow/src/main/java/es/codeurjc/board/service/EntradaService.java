package es.codeurjc.board.service;

import java.util.Collection;

import es.codeurjc.board.repositories.EntradaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
                .filter(e -> e.getEvento() != null &&
                        e.getEvento().getId().equals(eventoId))
                .toList();
    }

    public void save(String name, String acceso, String incluye,
                     Double precio, Evento evento) {

        Entrada entrada = new Entrada();

        entrada.setName(name);
        entrada.setAcceso(acceso);
        entrada.setIncluye(incluye);
        entrada.setPrecio(precio);
        entrada.setEvento(evento);

        entradaRepository.save(entrada);
    }

    public void update(long id, String name, String acceso,
                       String incluye, Double precio, Evento evento) {

        Entrada entrada = entradaRepository.findById(id).orElse(null);

        if (entrada != null) {
            entrada.setName(name);
            entrada.setAcceso(acceso);
            entrada.setIncluye(incluye);
            entrada.setPrecio(precio);
            entrada.setEvento(evento);

            entradaRepository.save(entrada);
        }
    }

    public void delete(long id) {
        entradaRepository.deleteById(id);
    }
}