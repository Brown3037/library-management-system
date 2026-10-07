package com.example.library.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiInfoController {

    @GetMapping
    public Map<String, Object> info() {
        return Map.of(
                "name", "Library Management API",
                "status", "running",
                "resources", List.of("/api/books", "/api/readers", "/api/loans"),
                "databaseConsole", "/h2-console"
        );
    }
}
