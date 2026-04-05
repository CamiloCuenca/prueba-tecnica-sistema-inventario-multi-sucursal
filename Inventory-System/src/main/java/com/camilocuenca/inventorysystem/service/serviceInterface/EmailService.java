package com.camilocuenca.inventorysystem.service.serviceInterface;

import com.camilocuenca.inventorysystem.dto.email.EmailDTO;

public interface EmailService {
    void sendMail(EmailDTO emailDTO) throws Exception;
}

