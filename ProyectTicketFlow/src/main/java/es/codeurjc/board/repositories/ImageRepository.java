package es.codeurjc.board.repositories;
import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjc.board.model.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

}