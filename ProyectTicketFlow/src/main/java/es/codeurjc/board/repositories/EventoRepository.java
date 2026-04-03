package es.codeurjc.board.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import es.codeurjc.board.model.Evento;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findTop3By();

}