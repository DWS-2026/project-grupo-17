package es.codeurjc.board.service;

import java.util.Collection;

import es.codeurjc.board.repositories.EntradaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.board.model.Entrada;
import es.codeurjc.board.model.Evento;

@Service
/**
 * Servicio de entradas:
 * encapsula operaciones CRUD y consultas por evento.
 */
public class EntradaService {

    @Autowired
    private EntradaRepository entradaRepository;

    // Devuelve todas las entradas almacenadas.
    public Collection<Entrada> findAll() {
        return entradaRepository.findAll();
    }

    // Busca entrada por id; si no existe devuelve null.
    public Entrada findById(long id) {
        return entradaRepository.findById(id).orElse(null);
    }

    // Devuelve las entradas asociadas a un evento concreto.
    public Collection<Entrada> findByEvento(Long eventoId) {
        return entradaRepository.findAll()
                .stream()
                .filter(e -> e.getEvento() != null &&
                        e.getEvento().getId().equals(eventoId))
                .toList();
    }

    // Crea una nueva entrada vinculada al evento indicado.
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

    // Actualiza una entrada existente; si el id no existe no realiza cambios.
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

    // Elimina una entrada por id.
    public void delete(long id) {
        entradaRepository.deleteById(id);
    }
}