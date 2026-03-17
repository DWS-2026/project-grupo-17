package es.codeurjc.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    @GetMapping("/error-403")
    public String accessDenied() {
        return "error-403";
    }

    @GetMapping("/error-404")
    public String notFound() {
        return "404";
    }

    @GetMapping("/error-500")
    public String internalError() {
        return "500";
    }
}
