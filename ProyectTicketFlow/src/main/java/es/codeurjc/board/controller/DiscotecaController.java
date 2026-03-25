package es.codeurjc.board.controller;

import java.io.IOException;
import java.security.Principal;
import java.sql.Blob;
import java.sql.SQLException;

import es.codeurjc.board.model.Discoteca;
import es.codeurjc.board.model.Image;
import es.codeurjc.board.model.User;
import es.codeurjc.board.repositories.DiscotecaRepository;
import es.codeurjc.board.service.DiscotecaService;
import es.codeurjc.board.service.ImageService;
import es.codeurjc.board.service.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import es.codeurjc.board.service.UserService;
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

    @Autowired
    private UserService userService;

    @ModelAttribute
	public void addAttributes(Model model, HttpServletRequest request) {

		Principal principal = request.getUserPrincipal();

		if (principal != null) {

			model.addAttribute("logged", true);
			model.addAttribute("email", principal.getName());
			model.addAttribute("admin", request.isUserInRole("ADMIN"));

		} else {
			model.addAttribute("logged", false);
		}
	}

    @GetMapping("/discotecas")
    public String showDiscotecas(Model model) {
        model.addAttribute("discotecas", discotecaService.findAll());
        model.addAttribute("isAdmin", userSession.isAdmin());
        return "discotecas";
    }

    @GetMapping("/discotecas/create-discotecas")
    public String newDiscotecaForm(Model model) {
        if (!userSession.isAdmin()) {
            return "redirect:/error-403";
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
        Discoteca discoteca = discotecaService.findById(id);
        
        if (discoteca == null) {
            return "redirect:/error-403";
        }

        // Validar que sea el propietario o administrador
        if (!userSession.isAdmin() && !isOwner(discoteca)) {
            return "redirect:/error-403";
        }

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

        Discoteca discoteca = discotecaService.findById(id);

        if (discoteca == null) {
            return "redirect:/error-403";
        }

        // Validar que sea el propietario o administrador
        if (!userSession.isAdmin() && !isOwner(discoteca)) {
            return "redirect:/error-403";
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
            return "redirect:/error-403";
        }

        // Asignar el propietario actual
        User currentUser = userService.findById(userSession.getUserId());
        discoteca.setOwner(currentUser);

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
        Discoteca discoteca = discotecaService.findById(id);

        if (discoteca == null) {
            return "redirect:/error-403";
        }

        // Validar que sea el propietario o administrador
        if (!userSession.isAdmin() && !isOwner(discoteca)) {
            return "redirect:/error-403";
        }

        discotecaService.delete(id);
        return "redirect:/discotecas";
    }

    /**
     * Verifica si el usuario actual es el propietario de la discoteca
     */
    private boolean isOwner(Discoteca discoteca) {
        Long currentUserId = userSession.getUserId();
        return discoteca.getOwner() != null && discoteca.getOwner().getId().equals(currentUserId);
    }

}