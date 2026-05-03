package com.autocomplete.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Page controller for Thymeleaf views.
 *
 * @author Sai Venkat
 */
@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/analytics")
    public String analytics() {
        return "analytics";
    }
}
