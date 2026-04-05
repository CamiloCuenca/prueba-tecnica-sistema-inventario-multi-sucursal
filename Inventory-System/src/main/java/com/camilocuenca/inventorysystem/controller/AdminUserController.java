package com.camilocuenca.inventorysystem.controller;

import com.camilocuenca.inventorysystem.dto.user.UserRegisterDTO;
import com.camilocuenca.inventorysystem.model.User;
import com.camilocuenca.inventorysystem.service.serviceInterface.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegisterDTO registerDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        try {
            // opcional: puedes usar claims del JWT (si deseas registrar quién crea el usuario)
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

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listUsers(Pageable pageable) {
        try {
            var page = userService.listUsers(pageable);
            return ResponseEntity.ok(page);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable java.util.UUID id) {
        try {
            var dto = userService.getUserById(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable java.util.UUID id, @Valid @RequestBody com.camilocuenca.inventorysystem.dto.user.UserUpdateDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return ResponseEntity.badRequest().body(Map.of("errors", bindingResult.getFieldErrors().stream().map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList()));
        try {
            // Ensure path id matches body id
            if (!id.equals(dto.id())) return ResponseEntity.badRequest().body(Map.of("error", "ID en path y body no coinciden"));
            var updated = userService.updateUser(dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Error al actualizar usuario";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
        }
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable java.util.UUID id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
