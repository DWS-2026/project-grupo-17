package es.codeurjc.board.controller;

import java.io.IOException;
import java.security.Principal;
import java.sql.Blob;
import java.sql.SQLException;

import es.codeurjc.board.model.Discoteca;
import es.codeurjc.board.model.Image;
import es.codeurjc.board.model.User;
import es.codeurjc.board.service.DiscotecaService;
import es.codeurjc.board.service.ImageService;
import es.codeurjc.board.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

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
    private UserService userService;


    @GetMapping("/discotecas")
    public String showDiscotecas(Model model, HttpServletRequest request) {
        model.addAttribute("discotecas", discotecaService.findAll());
        model.addAttribute("admin", request.isUserInRole("ADMIN"));
        return "discotecas";
    }

    @GetMapping("/discotecas/create-discotecas")
    public String newDiscotecaForm() {
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

        Discoteca discoteca = discotecaService.findById(id);

        if (discoteca == null) {
            return "redirect:/error-403";
        }

        model.addAttribute("discoteca", discoteca);
        return "edit-discoteca";
    }

    @PostMapping("/discotecas/edit/{id}")
    public String editDiscotecaProcess(@PathVariable Long id,
                                       Discoteca discotecaForm,
                                       @RequestParam(required = false) boolean removeImage,
                                       @RequestParam("imageFile") MultipartFile imageFile)
            throws IOException, SQLException {

        Discoteca discoteca = discotecaService.findById(id);

        if (discoteca == null) {
            return "redirect:/error-403";
        }

        discoteca.setName(discotecaForm.getName());
        discoteca.setCalle(discotecaForm.getCalle());
        discoteca.setDescripcion(discotecaForm.getDescripcion());

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
    public String createDiscotecaProcess(Discoteca discoteca,
                                         @RequestParam("imageFile") MultipartFile imageFile,
                                         Principal principal) throws IOException {

        String email = principal.getName();
        User currentUser = userService.findByEmail(email).orElse(null);

        discoteca.setOwner(currentUser);

        if (!imageFile.isEmpty()) {
            Image img = imageService.createImage(imageFile.getInputStream());
            discoteca.setImage(img);
        }

        discotecaService.save(discoteca);

        return "redirect:/discotecas/" + discoteca.getId();
    }

    @GetMapping("/discotecas/{id}/image")
    @ResponseBody
    public ResponseEntity<byte[]> showImage(@PathVariable long id) throws SQLException, IOException {
        Discoteca d = discotecaService.findById(id);
        if (d == null || d.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        Blob blob = d.getImage().getImageFile();
        byte[] bytes = blob.getBytes(1, (int) blob.length());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(bytes);
    }

    @PostMapping("/discotecas/delete/{id}")
    public String deleteDiscoteca(@PathVariable long id) {

        Discoteca discoteca = discotecaService.findById(id);

        if (discoteca == null) {
            return "redirect:/error-403";
        }

        discotecaService.delete(id);
        return "redirect:/discotecas";
    }
}