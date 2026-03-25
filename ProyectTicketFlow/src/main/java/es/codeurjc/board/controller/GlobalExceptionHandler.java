package es.codeurjc.board.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        model.addAttribute("error", e.getMessage());
        model.addAttribute("stackTrace", e.getStackTrace());
        //e.printStackTrace(); // Para ver en consola
        return "error";
    }
}
