package com.camilocuenca.inventorysystem.dto.email;

import java.util.Objects;

/**
 * DTO simple para envío de correos.
 * Usamos nombres igual que en el fragmento que proporcionaste: recipient(), issue(), body().
 */
public record EmailDTO(String recipient, String issue, String body) {
    public EmailDTO {
        Objects.requireNonNull(recipient, "recipient es obligatorio");
        Objects.requireNonNull(issue, "issue (subject) es obligatorio");
        Objects.requireNonNull(body, "body es obligatorio");
    }
}

