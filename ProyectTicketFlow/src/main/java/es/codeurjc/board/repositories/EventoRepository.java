package es.codeurjc.board.repositories;

import es.codeurjc.board.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, Long> {

}
