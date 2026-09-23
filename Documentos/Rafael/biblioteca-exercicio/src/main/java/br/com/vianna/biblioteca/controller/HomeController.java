package br.com.vianna.biblioteca.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class HomeController { @GetMapping("/") String inicio() { return "redirect:/livros"; } }
