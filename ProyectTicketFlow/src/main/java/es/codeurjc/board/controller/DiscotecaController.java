package es.codeurjc.board.controller;

import java.io.IOException;
import java.security.Principal;
import java.sql.SQLException;

import es.codeurjc.board.model.Discoteca;
import es.codeurjc.board.model.User;
import es.codeurjc.board.service.DiscotecaService;
import es.codeurjc.board.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

/**
 * Controlador de discotecas:
 * permite listar, crear, editar, mostrar imagen y eliminar discotecas.
 */
@Controller
public class DiscotecaController {

    @Autowired
    private DiscotecaService discotecaService;

    @Autowired
    private UserService userService;

    @Autowired
    private es.codeurjc.board.service.FileStorageService fileStorageService;


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
                                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                       @RequestParam(required = false) boolean removeFlyer,
                                       @RequestParam(value = "flyerFile", required = false) MultipartFile flyerFile,
                                       Model model)
            throws IOException, SQLException {

        Discoteca discoteca = discotecaService.findById(id);

        if (discoteca == null) {
            return "redirect:/error-403";
        }

        String error = discotecaService.validarCamposDiscoteca(
            discotecaForm.getName(), 
            discotecaForm.getCalle(), 
            discotecaForm.getDescripcion()
        );

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("discoteca", discoteca);
            model.addAttribute("id", id);
            return "edit-discoteca";
        }

        discotecaService.actualizarDiscoteca(
                id,
                discotecaForm.getName(),
                discotecaForm.getCalle(),
                discotecaForm.getDescripcion(),
                null,
                imageFile,
                flyerFile,
                removeImage,
                removeFlyer
        );

        return "redirect:/discotecas/" + discoteca.getId();
    }

    @PostMapping("/discotecas/create-discotecas")
    // Crea una discoteca nueva asociandola al usuario autenticado como propietario.
    public String createDiscotecaProcess(Discoteca discoteca,
                                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                         @RequestParam(value = "flyerFile", required = false) MultipartFile flyerFile,
                                         Principal principal,
                                         Model model) throws IOException, SQLException {

        String error = discotecaService.validarCamposDiscoteca(
            discoteca.getName(), 
            discoteca.getCalle(), 
            discoteca.getDescripcion()
        );

        if (error != null) {
            model.addAttribute("error", error);
            return "create-discotecas";
        }

        String email = principal.getName();
        User currentUser = userService.findByEmail(email).orElse(null);

        Discoteca nuevaDiscoteca = discotecaService.crearDiscoteca(
                discoteca.getName(),
                discoteca.getCalle(),
                discoteca.getDescripcion(),
                currentUser.getId(),
                imageFile,
                flyerFile
        );
        return "redirect:/discotecas/" + nuevaDiscoteca.getId();
    }

    @GetMapping("/discotecas/{id}/image")
    // Devuelve los bytes de la imagen de una discoteca para pintarla en el front.
    public ResponseEntity<byte[]> showImage(@PathVariable long id) throws SQLException, IOException {
        Discoteca d = discotecaService.findById(id);
        if (d == null || d.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        // Utiliza el método del servicio que soporta leer desde disco o BD
        byte[] bytes = discotecaService.getClubImage(id);
        
        if (bytes == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(bytes);
    }

    @GetMapping("/discotecas/{id}/flyer")
    public ResponseEntity<org.springframework.core.io.Resource> getClubFlyer(@PathVariable long id) {
        Discoteca d = discotecaService.findById(id);
        if (d == null || d.getFlyer() == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/discotecas/" + id)
                    .build();
        }
        try {
            org.springframework.core.io.Resource resource = fileStorageService.getFileAsResource(d.getFlyer());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            // ignore
        }
        return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/discotecas/" + id)
                .build();
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
}