package com.camilocuenca.inventorysystem.service;

import com.camilocuenca.inventorysystem.config.JWTUtils;
import com.camilocuenca.inventorysystem.dto.user.LoginDTO;
import com.camilocuenca.inventorysystem.dto.user.UserRegisterDTO;
import com.camilocuenca.inventorysystem.dto.user.UserUpdateDTO;
import com.camilocuenca.inventorysystem.Enums.Role;
import com.camilocuenca.inventorysystem.model.Branch;
import com.camilocuenca.inventorysystem.model.User;
import com.camilocuenca.inventorysystem.repository.BranchRepository;
import com.camilocuenca.inventorysystem.repository.UserRepository;
import com.camilocuenca.inventorysystem.service.serviceimpl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    JWTUtils jwtUtils;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    BranchRepository branchRepository;

    @InjectMocks
    UserServiceImpl userService;

    UUID userId;
    User existingUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Juan");
        existingUser.setEmail("juan@example.com");
        existingUser.setPassword("encodedpwd");
        existingUser.setRole(Role.MANAGER);
        existingUser.setCreatedAt(Instant.now());
    }

    @Test
    void login_successful_returnsToken() throws Exception {
        LoginDTO loginDTO = new LoginDTO("juan@example.com", "secret");
        when(userRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("secret", "encodedpwd")).thenReturn(true);
        when(jwtUtils.generateToken(eq("juan@example.com"), anyMap())).thenReturn("token123");

        var token = userService.login(loginDTO);

        assertNotNull(token);
        assertEquals("token123", token.token());
        verify(userRepository).findByEmail("juan@example.com");
        verify(jwtUtils).generateToken(eq("juan@example.com"), anyMap());
    }

    @Test
    void login_invalidPassword_throwsException() {
        LoginDTO loginDTO = new LoginDTO("juan@example.com", "wrong");
        when(userRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong", "encodedpwd")).thenReturn(false);

        assertThrows(Exception.class, () -> userService.login(loginDTO));
    }

    @Test
    void register_successful_returnsUser() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO("Miguel", "miguel@example.com", "123456", "OPERATOR", null);
        when(userRepository.existsByEmail("miguel@example.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var created = userService.register(dto);

        assertNotNull(created);
        assertEquals("Miguel", created.getName());
        assertEquals("miguel@example.com", created.getEmail());
        assertEquals("encoded123", created.getPassword());
        assertEquals(Role.OPERATOR, created.getRole());
        assertNotNull(created.getCreatedAt());
    }

    @Test
    void register_duplicateEmail_throwsException() {
        UserRegisterDTO dto = new UserRegisterDTO("Miguel", "miguel@example.com", "123456", null, null);
        when(userRepository.existsByEmail("miguel@example.com")).thenReturn(true);

        assertThrows(Exception.class, () -> userService.register(dto));
    }

    @Test
    void listUsers_mapsToDTOs() {
        User another = new User();
        another.setId(UUID.randomUUID());
        another.setName("Ana");
        another.setEmail("ana@example.com");
        another.setRole(Role.OPERATOR);
        another.setCreatedAt(Instant.now());

        when(userRepository.findAll(PageRequest.of(0,2))).thenReturn(new PageImpl<>(List.of(existingUser, another)));

        var page = userService.listUsers(PageRequest.of(0,2));

        assertEquals(2, page.getContent().size());
        assertEquals("Juan", page.getContent().get(0).name());
    }

    @Test
    void getUserById_notFound_throws() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> userService.getUserById(userId));
    }

    @Test
    void updateUser_changesFields() throws Exception {
        UUID id = userId;
        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(branchRepository.findById(any())).thenReturn(Optional.of(new Branch()));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        var dto = new UserUpdateDTO(id, "Juan Updated", "new@example.com", "ADMIN", UUID.randomUUID());
        var resp = userService.updateUser(dto);

        assertEquals("Juan Updated", resp.name());
        assertEquals("new@example.com", resp.email());
        assertEquals("ADMIN", resp.role());
    }

    @Test
    void deleteUser_notFound_throws() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> userService.deleteUser(userId));
    }
}
