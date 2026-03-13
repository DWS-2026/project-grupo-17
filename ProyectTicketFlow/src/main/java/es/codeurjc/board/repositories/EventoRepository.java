package es.codeurjc.board.repositories;

import es.codeurjc.board.model.Discoteca;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Discoteca, Long> {

}
