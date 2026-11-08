package com.dd01xc.service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

//React Router paths: direct open or refresh must get index.html, not 404
@Controller
public class SpaController {

    public static final String[] SPA_ROUTES = {"/login", "/signup", "/dashboard", "/profile"};

    @GetMapping({"/login", "/signup", "/dashboard", "/profile"})
    public String index() {
        return "forward:/index.html";
    }
}
