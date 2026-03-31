package com.camilocuenca.inventorysystem.controller;

import com.camilocuenca.inventorysystem.dto.jwt.TokenDTO;
import com.camilocuenca.inventorysystem.dto.user.LoginDTO;
import com.camilocuenca.inventorysystem.service.serviceInterface.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        try {
            TokenDTO token = userService.login(loginDTO);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Error de autenticación";
            if (msg.toLowerCase().contains("credenciales")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg));
            }
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
    }
}
