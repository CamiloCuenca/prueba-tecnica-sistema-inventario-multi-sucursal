package com.camilocuenca.inventorysystem.controller;

import com.camilocuenca.inventorysystem.dto.branch.BranchCreateDto;
import com.camilocuenca.inventorysystem.dto.branch.BranchResponseDto;
import com.camilocuenca.inventorysystem.dto.branch.BranchUpdateDto;
import com.camilocuenca.inventorysystem.service.serviceInterface.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService branchService;

    @Autowired
    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createBranch(@Valid @RequestBody BranchCreateDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return ResponseEntity.badRequest().body(Map.of("errors", bindingResult.getFieldErrors().stream().map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList()));
        try {
            BranchResponseDto created = branchService.createBranch(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> listBranches(Pageable pageable) {
        Page<BranchResponseDto> page = branchService.listBranches(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> getBranch(@PathVariable UUID id) {
        try {
            BranchResponseDto dto = branchService.getBranch(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBranch(@PathVariable UUID id, @Valid @RequestBody BranchUpdateDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return ResponseEntity.badRequest().body(Map.of("errors", bindingResult.getFieldErrors().stream().map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList()));
        try {
            if (!id.equals(dto.id())) return ResponseEntity.badRequest().body(Map.of("error", "ID en path y body no coinciden"));
            BranchResponseDto updated = branchService.updateBranch(dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBranch(@PathVariable UUID id) {
        try {
            branchService.deleteBranch(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
