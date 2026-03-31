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

import java.util.HashMap;
import java.util.Map;

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

        // Guardar y devolver
        return userRepository.save(user);
    }
}
