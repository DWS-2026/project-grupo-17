package es.codeurjc.board.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status == null) {
            return "error";
        }

        int statusCode = Integer.parseInt(status.toString());
        if (statusCode == 403) {
            return "error-403";
        }
        if (statusCode == 404) {
            return "404";
        }
        if (statusCode == 500) {
            return "500";
        }
        return "error";
    }

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
