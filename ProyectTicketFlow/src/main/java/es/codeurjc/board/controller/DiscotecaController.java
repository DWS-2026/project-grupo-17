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
 * Club controller:
 * allows listing, creating, editing, displaying images and deleting clubs.
 */
@Controller
public class DiscotecaController {

    @Autowired
    private DiscotecaService discotecaService;

    @Autowired
    private UserService userService;


    @GetMapping("/discotecas")
    // Lists all clubs and marks in the model whether the current user is an admin.
    public String showDiscotecas(Model model, HttpServletRequest request) {
        model.addAttribute("discotecas", discotecaService.findAll());
        model.addAttribute("admin", request.isUserInRole("ADMIN"));
        return "discotecas";
    }

    @GetMapping("/discotecas/create-discotecas")
    // Displays the form to create a new club.
    public String newDiscotecaForm() {
        return "create-discotecas";
    }

    @GetMapping("/discotecas/{id}")
    // Displays the detail of a specific club.
    public String detailsDiscoteca(@PathVariable long id, Model model) {
        Discoteca discoteca = discotecaService.findById(id);
        model.addAttribute("discoteca", discoteca);
        return "detalles-discoteca";
    }

    @GetMapping("/discotecas/edit-discoteca/{id}")
    // Loads the club edit form.
    public String editDiscotecaForm(@PathVariable long id, Model model) {

        Discoteca discoteca = discotecaService.findById(id);

        if (discoteca == null) {
            return "redirect:/error-403";
        }

        model.addAttribute("discoteca", discoteca);
        return "edit-discoteca";
    }

    @PostMapping("/discotecas/edit/{id}")
    // Updates an existing club and, optionally, its image.
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
    // Creates a new club associating the authenticated user as its owner.
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
    // Returns the bytes of a club's image to render it on the front end.
    public ResponseEntity<byte[]> showImage(@PathVariable long id) throws SQLException, IOException {
        Discoteca d = discotecaService.findById(id);
        if (d == null || d.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        // Uses the service method that supports reading from disk or DB
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
        return discotecaService.getClubFlyerResource(id)
                .map(resource -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .<org.springframework.core.io.Resource>body(resource))
                .orElse(ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                        .header(HttpHeaders.LOCATION, "/discotecas/" + id)
                        .build());
    }

    @PostMapping("/discotecas/delete/{id}")
    // Deletes a club by id.
    public String deleteDiscoteca(@PathVariable long id) {

        Discoteca discoteca = discotecaService.findById(id);

        if (discoteca == null) {
            return "redirect:/error-403";
        }

        discotecaService.delete(id);
        return "redirect:/discotecas";
    }
}