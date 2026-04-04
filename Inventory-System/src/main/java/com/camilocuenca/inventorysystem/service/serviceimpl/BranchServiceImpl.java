package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.dto.branch.BranchCreateDto;
import com.camilocuenca.inventorysystem.dto.branch.BranchResponseDto;
import com.camilocuenca.inventorysystem.dto.branch.BranchUpdateDto;
import com.camilocuenca.inventorysystem.model.Branch;
import com.camilocuenca.inventorysystem.repository.BranchRepository;
import com.camilocuenca.inventorysystem.service.serviceInterface.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    @Autowired
    public BranchServiceImpl(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @Override
    @Transactional
    public BranchResponseDto createBranch(BranchCreateDto dto) {
        Branch b = new Branch();
        b.setName(dto.name());
        b.setAddress(dto.address());
        b.setLatitude(dto.latitude());
        b.setLongitude(dto.longitude());
        b.setCreatedAt(Instant.now());
        Branch saved = branchRepository.save(b);
        return toDto(saved);
    }

    @Override
    public Page<BranchResponseDto> listBranches(Pageable pageable) {
        Page<Branch> page = branchRepository.findAll(pageable);
        List<BranchResponseDto> dtos = page.stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    public BranchResponseDto getBranch(UUID id) throws Exception {
        Branch b = branchRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal no encontrada"));
        return toDto(b);
    }

    @Override
    @Transactional
    public BranchResponseDto updateBranch(BranchUpdateDto dto) {
        Branch b = branchRepository.findById(dto.id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal no encontrada"));
        b.setName(dto.name());
        b.setAddress(dto.address());
        b.setLatitude(dto.latitude());
        b.setLongitude(dto.longitude());
        Branch saved = branchRepository.save(b);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteBranch(UUID id) {
        Branch b = branchRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal no encontrada"));
        branchRepository.delete(b);
    }

    private BranchResponseDto toDto(Branch b) {
        return new BranchResponseDto(b.getId(), b.getName(), b.getAddress(), b.getLatitude(), b.getLongitude(), b.getCreatedAt());
    }
}
