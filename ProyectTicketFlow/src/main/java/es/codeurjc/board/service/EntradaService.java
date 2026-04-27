package es.codeurjc.board.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.codeurjc.board.repositories.EntradaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.board.model.Entrada;
import es.codeurjc.board.model.Evento;
import es.codeurjc.board.model.User;

@Service
/**
 * Servicio de entradas:
 * encapsula operaciones CRUD y consultas por evento.
 */
public class EntradaService {

    @Autowired
    private EntradaRepository entradaRepository;

    @Autowired
    private UserService userService;

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

    // Valida los campos obligatorios de una entrada
    public String validarCamposEntrada(String name, String acceso, String incluye, Double precio) {
        if (isBlank(name) || isBlank(acceso) || isBlank(incluye) || precio == null || precio < 0) {
            return "Revisa los campos obligatorios y el precio";
        }
        return null;
    }

    // Crea una entrada con validación
    public void createEntradaWithValidation(String name, String acceso, String incluye, Double precio, Evento evento) {
        Entrada entrada = new Entrada();
        entrada.setName(name);
        entrada.setAcceso(acceso);
        entrada.setIncluye(incluye);
        entrada.setPrecio(precio);
        entrada.setEvento(evento);
        entradaRepository.save(entrada);
    }

    // Actualiza una entrada con validación
    public void updateEntradaWithValidation(long id, String name, String acceso, String incluye, Double precio, Evento evento) {
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

    // Compra una entrada para un usuario
    public String comprarEntrada(Long entradaId, User user) {
        Entrada entrada = findById(entradaId);
        
        if (entrada == null) {
            return "error_entrada_no_existe";
        }

        if (user != null) {
            List<Entrada> entradas = user.getEntradasCompradas();
            if (entradas == null) {
                entradas = new ArrayList<>();
            }

            boolean yaComprada = entradas.stream()
                    .anyMatch(e -> e.getId().equals(entrada.getId()));

            if (yaComprada) {
                return "error_ya_comprada";
            }

            entradas.add(entrada);
            user.setEntradasCompradas(entradas);
            userService.saveUser(user);
            
            return "success";
        }

        return "error_usuario_no_existe";
    }

    // Elimina una entrada y la retira de todos los usuarios que la tuvieran comprada
    public void deleteEntradaConLimpieza(long id) {
        Entrada entrada = findById(id);
        
        if (entrada != null) {
            Collection<User> users = userService.findAll();
            
            for (User user : users) {
                if (user.getEntradasCompradas() != null) {
                    user.getEntradasCompradas().remove(entrada);
                    userService.saveUser(user);
                }
            }
            
            delete(id);
        }
    }

    // Utilidad privada para validar campos de texto obligatorios
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}