package com.camilocuenca.inventorysystem.controller;

import com.camilocuenca.inventorysystem.dto.user.UserRegisterDTO;
import com.camilocuenca.inventorysystem.model.User;
import com.camilocuenca.inventorysystem.service.serviceInterface.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegisterDTO registerDTO, BindingResult bindingResult, Authentication authentication) {
        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        try {
            // opcional: puedes usar 'authentication' para registrar quién crea el usuario
            User created = userService.register(registerDTO);

            Map<String, Object> resp = new HashMap<>();
            resp.put("id", created.getId());
            resp.put("name", created.getName());
            resp.put("email", created.getEmail());
            resp.put("role", created.getRole() != null ? created.getRole().name() : null);
            resp.put("branchId", created.getBranch() != null ? created.getBranch().getId() : null);

            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Error al crear usuario";
            if (msg.toLowerCase().contains("ya está registrado") || msg.toLowerCase().contains("ya esta registrado")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", msg));
            }
            if (msg.toLowerCase().contains("sucursal no encontrada") || msg.toLowerCase().contains("branch")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", msg));
            }
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
    }
}
