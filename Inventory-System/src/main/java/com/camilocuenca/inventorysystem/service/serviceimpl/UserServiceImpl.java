package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.dto.jwt.TokenDTO;
import com.camilocuenca.inventorysystem.dto.user.LoginDTO;
import com.camilocuenca.inventorysystem.dto.user.UserRegisterDTO;
import com.camilocuenca.inventorysystem.model.Branch;
import com.camilocuenca.inventorysystem.Enums.Role;
import com.camilocuenca.inventorysystem.model.User;
import com.camilocuenca.inventorysystem.repository.BranchRepository;
import com.camilocuenca.inventorysystem.repository.UserRepository;
import com.camilocuenca.inventorysystem.config.JWTUtils;
import com.camilocuenca.inventorysystem.service.serviceInterface.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JWTUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final BranchRepository branchRepository;

    /**
     *
     * @param loginDTO El objeto LoginDTO que contiene el email y la contraseña del usuario.
     * @return
     * @throws Exception
     */
    @Override
    public TokenDTO login(LoginDTO loginDTO) throws Exception {
        // Buscar usuario por email
        User user = userRepository.findByEmail(loginDTO.email())
                .orElseThrow(() -> new Exception("Credenciales inválidas."));

        // Verificar contraseña usando BCrypt
        if (!passwordEncoder.matches(loginDTO.password(), user.getPassword())) {
            throw new Exception("Credenciales inválidas.");
        }

        // Construir claims mínimos
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole() != null ? user.getRole().name() : null);
        claims.put("userId", user.getId().toString());
        // Incluir branchId en el token si el usuario tiene una sucursal asignada
        if (user.getBranch() != null && user.getBranch().getId() != null) {
            claims.put("branchId", user.getBranch().getId().toString());
        }

        // Generar token
        String token = jwtUtils.generateToken(user.getEmail(), claims);

        return new TokenDTO(token);
    }

    /**
     *
     * @param registerDTO datos para crear el usuario
     * @return
     * @throws Exception
     */
    @Override
    public User register(UserRegisterDTO registerDTO) throws Exception {
        // Verificar que el email no exista
        if (userRepository.existsByEmail(registerDTO.email())) {
            throw new Exception("El email ya está registrado.");
        }

        // Crear entidad User
        User user = new User();
        user.setName(registerDTO.name());
        user.setEmail(registerDTO.email());
        user.setPassword(passwordEncoder.encode(registerDTO.password()));
        // Mapear role string a enum Role
        if (registerDTO.role() != null) {
            try {
                user.setRole(Role.valueOf(registerDTO.role()));
            } catch (IllegalArgumentException e) {
                throw new Exception("Role inválido. Valores permitidos: ADMIN, MANAGER, OPERATOR");
            }
        } else {
            user.setRole(Role.OPERATOR); // valor por defecto
        }

        // Asociar branch si se proporciona
        if (registerDTO.branchId() != null) {
            Branch branch = branchRepository.findById(registerDTO.branchId())
                    .orElseThrow(() -> new Exception("Sucursal no encontrada."));
            user.setBranch(branch);
        }

        // Registrar fecha de creación
        user.setCreatedAt(Instant.now());

        // Guardar y devolver
        return userRepository.save(user);
    }

    @Override
    public Page<com.camilocuenca.inventorysystem.dto.user.UserResponseDTO> listUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        var dtos = page.stream().map(u -> new com.camilocuenca.inventorysystem.dto.user.UserResponseDTO(
                u.getId(), u.getName(), u.getEmail(), u.getRole() != null ? u.getRole().name() : null,
                u.getBranch() != null ? u.getBranch().getId() : null, u.getCreatedAt()
        )).toList();
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    public com.camilocuenca.inventorysystem.dto.user.UserResponseDTO getUserById(UUID id) throws Exception {
        User u = userRepository.findById(id).orElseThrow(() -> new Exception("Usuario no encontrado"));
        return new com.camilocuenca.inventorysystem.dto.user.UserResponseDTO(u.getId(), u.getName(), u.getEmail(), u.getRole() != null ? u.getRole().name() : null, u.getBranch() != null ? u.getBranch().getId() : null, u.getCreatedAt());
    }

    @Override
    public com.camilocuenca.inventorysystem.dto.user.UserResponseDTO updateUser(com.camilocuenca.inventorysystem.dto.user.UserUpdateDTO dto) throws Exception {
        User u = userRepository.findById(dto.id()).orElseThrow(() -> new Exception("Usuario no encontrado"));
        u.setName(dto.name());
        if (!u.getEmail().equals(dto.email())) {
            if (userRepository.existsByEmail(dto.email())) throw new Exception("El email ya está registrado.");
            u.setEmail(dto.email());
        }
        if (dto.role() != null) {
            try { u.setRole(com.camilocuenca.inventorysystem.Enums.Role.valueOf(dto.role())); } catch (IllegalArgumentException e) { throw new Exception("Role inválido. Valores permitidos: ADMIN, MANAGER, OPERATOR"); }
        }
        if (dto.branchId() != null) {
            var b = branchRepository.findById(dto.branchId()).orElseThrow(() -> new Exception("Sucursal no encontrada."));
            u.setBranch(b);
        }
        User saved = userRepository.save(u);
        return new com.camilocuenca.inventorysystem.dto.user.UserResponseDTO(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole() != null ? saved.getRole().name() : null, saved.getBranch() != null ? saved.getBranch().getId() : null, saved.getCreatedAt());
    }

    @Override
    public void deleteUser(UUID id) throws Exception {
        User u = userRepository.findById(id).orElseThrow(() -> new Exception("Usuario no encontrado"));
        userRepository.delete(u);
    }
}
