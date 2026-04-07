package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.dto.branch.BranchCreateDto;
import com.camilocuenca.inventorysystem.dto.branch.BranchResponseDto;
import com.camilocuenca.inventorysystem.dto.branch.BranchUpdateDto;
import com.camilocuenca.inventorysystem.model.Branch;
import com.camilocuenca.inventorysystem.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchServiceImplTest {

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private BranchServiceImpl branchService;

    @Captor
    private ArgumentCaptor<Branch> branchCaptor;

    private UUID id;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
    }

    @Test
    void createBranch_success() {
        BranchCreateDto dto = new BranchCreateDto("Sucursal A", "Calle 1", 1.23, 4.56);

        Branch saved = new Branch();
        saved.setId(id);
        saved.setName(dto.name());
        saved.setAddress(dto.address());
        saved.setLatitude(dto.latitude());
        saved.setLongitude(dto.longitude());
        saved.setCreatedAt(Instant.now());

        when(branchRepository.save(any(Branch.class))).thenReturn(saved);

        BranchResponseDto res = branchService.createBranch(dto);

        assertNotNull(res);
        assertEquals(id, res.id());
        assertEquals(dto.name(), res.name());
        verify(branchRepository, times(1)).save(branchCaptor.capture());
        Branch captured = branchCaptor.getValue();
        assertEquals(dto.name(), captured.getName());
        assertNotNull(captured.getCreatedAt());
    }

    @Test
    void listBranches_returnsPage() {
        Branch b1 = new Branch();
        b1.setId(UUID.randomUUID());
        b1.setName("A");
        Branch b2 = new Branch();
        b2.setId(UUID.randomUUID());
        b2.setName("B");
        List<Branch> list = List.of(b1, b2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Branch> page = new PageImpl<>(list, pageable, list.size());

        when(branchRepository.findAll(pageable)).thenReturn(page);

        Page<BranchResponseDto> res = branchService.listBranches(pageable);

        assertNotNull(res);
        assertEquals(2, res.getContent().size());
        assertEquals(list.size(), res.getTotalElements());
        verify(branchRepository, times(1)).findAll(pageable);
    }

    @Test
    void getBranch_found() throws Exception {
        Branch b = new Branch();
        b.setId(id);
        b.setName("Sucursal X");
        b.setCreatedAt(Instant.now());

        when(branchRepository.findById(id)).thenReturn(Optional.of(b));

        BranchResponseDto dto = branchService.getBranch(id);

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals("Sucursal X", dto.name());
    }

    @Test
    void getBranch_notFound_throws() {
        when(branchRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> branchService.getBranch(id));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void updateBranch_success() {
        BranchUpdateDto dto = new BranchUpdateDto(id, "New name", "Dir", 2.0, 3.0);
        Branch existing = new Branch();
        existing.setId(id);
        existing.setName("Old");

        Branch saved = new Branch();
        saved.setId(id);
        saved.setName(dto.name());
        saved.setAddress(dto.address());
        saved.setLatitude(dto.latitude());
        saved.setLongitude(dto.longitude());
        saved.setCreatedAt(Instant.now());

        when(branchRepository.findById(id)).thenReturn(Optional.of(existing));
        when(branchRepository.save(existing)).thenReturn(saved);

        BranchResponseDto res = branchService.updateBranch(dto);

        assertNotNull(res);
        assertEquals(dto.name(), res.name());
        verify(branchRepository, times(1)).findById(id);
        verify(branchRepository, times(1)).save(existing);
    }

    @Test
    void updateBranch_notFound_throws() {
        BranchUpdateDto dto = new BranchUpdateDto(id, "New name", "Dir", 2.0, 3.0);
        when(branchRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> branchService.updateBranch(dto));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void deleteBranch_success() {
        Branch b = new Branch();
        b.setId(id);
        when(branchRepository.findById(id)).thenReturn(Optional.of(b));

        assertDoesNotThrow(() -> branchService.deleteBranch(id));
        verify(branchRepository, times(1)).delete(b);
    }

    @Test
    void deleteBranch_notFound_throws() {
        when(branchRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> branchService.deleteBranch(id));
        assertEquals(404, ex.getStatusCode().value());
    }
}

