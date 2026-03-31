package com.camilocuenca.inventorysystem.util;

import com.camilocuenca.inventorysystem.Enums.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

public class AuthUtil {

    public static UUID getUserId(Authentication auth) {
        if (auth == null) return null;
        if (auth instanceof UsernamePasswordAuthenticationToken token) {
            Object cred = token.getCredentials();
            if (cred != null) {
                try {
                    return UUID.fromString(cred.toString());
                } catch (IllegalArgumentException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    public static UUID getBranchId(Authentication auth) {
        if (auth == null) return null;
        if (auth instanceof UsernamePasswordAuthenticationToken token) {
            Object details = token.getDetails();
            if (details != null) {
                try {
                    return UUID.fromString(details.toString());
                } catch (IllegalArgumentException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    public static boolean hasRole(Authentication auth, Role role) {
        if (auth == null || role == null) return false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (ga.getAuthority() != null && ga.getAuthority().equals("ROLE_" + role.name())) return true;
        }
        return false;
    }

    public static void requireCreatePurchasePermission(Authentication auth, UUID targetBranchId) {
        if (auth == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");

        boolean isAdmin = hasRole(auth, Role.ADMIN);
        boolean isManager = hasRole(auth, Role.MANAGER);
        boolean isOperator = hasRole(auth, Role.OPERATOR);

        if (isAdmin || isManager) return; // permitido para cualquier sucursal

        if (isOperator) {
            UUID userBranch = getBranchId(auth);
            if (userBranch == null || targetBranchId == null || !userBranch.equals(targetBranchId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operador solo puede crear compras en su propia sucursal");
            }
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado a crear compras");
    }
}
