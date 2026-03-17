package es.codeurjc.board.controller;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;

import es.codeurjc.board.model.Discoteca;
import es.codeurjc.board.model.Image;
import es.codeurjc.board.repositories.DiscotecaRepository;
import es.codeurjc.board.service.DiscotecaService;
import es.codeurjc.board.service.ImageService;
import es.codeurjc.board.service.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;


@Controller
public class DiscotecaController {

    @Autowired
    private DiscotecaService discotecaService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private DiscotecaRepository discotecaRepository;

    @Autowired
    private UserSession userSession;

    @GetMapping("/discotecas")
    public String showDiscotecas(Model model) {
        model.addAttribute("discotecas", discotecaService.findAll());
        model.addAttribute("isAdmin", userSession.isAdmin());
        return "discotecas";
    }

    @GetMapping("/discotecas/create-discotecas")
    public String newDiscotecaForm(Model model) {
        if (!userSession.isAdmin()) {
            model.addAttribute("error", "Solo los administradores pueden crear discotecas");
            return "redirect:/discotecas";
        }
        return "create-discotecas";
    }

    @GetMapping("/discotecas/{id}")
    public String detailsDiscoteca(@PathVariable long id, Model model) {
        Discoteca discoteca = discotecaService.findById(id);
        model.addAttribute("discoteca", discoteca);
        return "detalles-discoteca";
    }

    @GetMapping("/discotecas/edit-discoteca/{id}")
    public String editDiscotecaForm(@PathVariable long id, Model model) {
        if (!userSession.isAdmin()) {
            model.addAttribute("error", "Solo los administradores pueden editar discotecas");
            return "redirect:/discotecas";
        }
        Discoteca discoteca = discotecaService.findById(id);
        model.addAttribute("discoteca", discoteca);
        return "edit-discoteca";
    }

    @PostMapping("/discotecas/edit/{id}")
    public String editDiscotecaProcess(Model model,
                                       @PathVariable Long id,
                                       Discoteca discotecaForm,
                                       @RequestParam(required = false) boolean removeImage,
                                       @RequestParam("imageFile") MultipartFile imageFile)
            throws IOException, SQLException {

        if (!userSession.isAdmin()) {
            model.addAttribute("error", "Solo los administradores pueden editar discotecas");
            return "redirect:/discotecas";
        }

        Discoteca discoteca = discotecaService.findById(id);

        if (discoteca == null) {
            return "redirect:/discotecas";
        }

        // Actualizar campos
        discoteca.setName(discotecaForm.getName());
        discoteca.setCalle(discotecaForm.getCalle());
        discoteca.setDescripcion(discotecaForm.getDescripcion());

        // Imagen
        if (removeImage) {
            discoteca.setImage(null);
        } else if (!imageFile.isEmpty()) {
            Image img = imageService.createImage(imageFile.getInputStream());
            discoteca.setImage(img);
        }

        discotecaService.save(discoteca);

        return "redirect:/discotecas/" + discoteca.getId();
    }

    @PostMapping("/discotecas/create-discotecas")
    public String createDiscotecaProcess(Model model,
                                         Discoteca discoteca,
                                         @RequestParam("imageFile") MultipartFile imageFile) throws IOException {

        if (!userSession.isAdmin()) {
            model.addAttribute("error", "Solo los administradores pueden crear discotecas");
            return "redirect:/discotecas";
        }

        if (!imageFile.isEmpty()) {
            Image img = imageService.createImage(imageFile.getInputStream());
            discoteca.setImage(img);
        }

        discotecaService.save(discoteca);

        model.addAttribute("discotecaId", discoteca.getId());
        return "redirect:/discotecas/" + discoteca.getId();
    }

    @GetMapping("/discotecas/{id}/image")
    @ResponseBody
    public ResponseEntity<byte[]> showImage(@PathVariable long id) throws SQLException, IOException {
        Discoteca d = discotecaService.findById(id);
        if (d == null || d.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        // Obtener los bytes del Blob
        Blob blob = d.getImage().getImageFile();
        int blobLength = (int) blob.length();
        byte[] bytes = blob.getBytes(1, blobLength);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(bytes);
    }

    @PostMapping("/discotecas/delete/{id}")
    public String deleteDiscoteca(@PathVariable long id, Model model) {
        if (!userSession.isAdmin()) {
            model.addAttribute("error", "Solo los administradores pueden eliminar discotecas");
            return "redirect:/discotecas";
        }
        discotecaService.delete(id);
        return "redirect:/discotecas";
    }

}