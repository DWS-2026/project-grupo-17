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
/**
 * Controlador de discotecas:
 * permite listar, crear, editar, mostrar imagen y eliminar discotecas.
 */
public class DiscotecaController {

    @Autowired
    private DiscotecaService discotecaService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserService userService;


    @GetMapping("/discotecas")
    // Lista todas las discotecas y marca en el modelo si el usuario actual es admin.
    public String showDiscotecas(Model model, HttpServletRequest request) {
        model.addAttribute("discotecas", discotecaService.findAll());
        model.addAttribute("admin", request.isUserInRole("ADMIN"));
        return "discotecas";
    }

    @GetMapping("/discotecas/create-discotecas")
    // Muestra el formulario para crear una nueva discoteca.
    public String newDiscotecaForm() {
        return "create-discotecas";
    }

    @GetMapping("/discotecas/{id}")
    // Muestra el detalle de una discoteca concreta.
    public String detailsDiscoteca(@PathVariable long id, Model model) {
        Discoteca discoteca = discotecaService.findById(id);
        model.addAttribute("discoteca", discoteca);
        return "detalles-discoteca";
    }

    @GetMapping("/discotecas/edit-discoteca/{id}")
    // Carga el formulario de edicion de discoteca.
    public String editDiscotecaForm(@PathVariable long id, Model model) {

        Discoteca discoteca = discotecaService.findById(id);

        if (discoteca == null) {
            return "redirect:/error-403";
        }

        model.addAttribute("discoteca", discoteca);
        return "edit-discoteca";
    }

    @PostMapping("/discotecas/edit/{id}")
    // Actualiza una discoteca existente y, opcionalmente, su imagen.
    public String editDiscotecaProcess(@PathVariable Long id,
                                       Discoteca discotecaForm,
                                       @RequestParam(required = false) boolean removeImage,
                                       @RequestParam("imageFile") MultipartFile imageFile,
                                       Model model)
            throws IOException, SQLException {

        Discoteca discoteca = discotecaService.findById(id);

        if (discoteca == null) {
            return "redirect:/error-403";
        }

        if (isBlank(discotecaForm.getName()) || isBlank(discotecaForm.getCalle()) || isBlank(discotecaForm.getDescripcion())) {
            model.addAttribute("error", "Todos los campos obligatorios deben estar rellenos");
            model.addAttribute("discoteca", discoteca);
            model.addAttribute("id", id);
            return "edit-discoteca";
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
    // Crea una discoteca nueva asociandola al usuario autenticado como propietario.
    public String createDiscotecaProcess(Discoteca discoteca,
                                         @RequestParam("imageFile") MultipartFile imageFile,
                                         Principal principal,
                                         Model model) throws IOException {

        if (isBlank(discoteca.getName()) || isBlank(discoteca.getCalle()) || isBlank(discoteca.getDescripcion())) {
            model.addAttribute("error", "Todos los campos obligatorios deben estar rellenos");
            return "create-discotecas";
        }

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
    // Devuelve los bytes de la imagen de una discoteca para pintarla en el front.
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
    // Elimina una discoteca por id.
    public String deleteDiscoteca(@PathVariable long id) {

        Discoteca discoteca = discotecaService.findById(id);

        if (discoteca == null) {
            return "redirect:/error-403";
        }

        discotecaService.delete(id);
        return "redirect:/discotecas";
    }

    // Utilidad local para validar campos de texto obligatorios.
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}