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

    public Collection<Discoteca> findAll() {
        return discotecaRepository.findAll();
    }

    public Discoteca findById(long id) {
        return discotecaRepository.findById(id).orElse(null);
    }

    public void save(Discoteca discoteca) {
        discotecaRepository.save(discoteca);
    }

    public void delete(long id) {
        discotecaRepository.deleteById(id);
    }
}