package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.dto.jwt.TokenDTO;
import com.camilocuenca.inventorysystem.dto.user.LoginDTO;
import com.camilocuenca.inventorysystem.dto.user.UserRegisterDTO;
import com.camilocuenca.inventorysystem.model.User;

public interface UserService {

    /**
     * Inicia sesión en el sistema.
     *
     * @param loginDTO El objeto LoginDTO que contiene el email y la contraseña del usuario.
     * @return Un objeto TokenDTO que contiene el token de autenticación.
     * @throws Exception Si las credenciales son incorrectas o si hay un error durante el inicio de sesión.
     */
    TokenDTO login(LoginDTO loginDTO) throws Exception;

    /**
     * Registra un nuevo usuario (acción realizada por un administrador).
     *
     * @param registerDTO datos para crear el usuario
     * @return el usuario creado
     * @throws Exception si ya existe el email u ocurre un error
     */
    User register(UserRegisterDTO registerDTO) throws Exception;
}
