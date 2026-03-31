package com.camilocuenca.inventorysystem.util;

import com.camilocuenca.inventorysystem.model.User;
import com.camilocuenca.inventorysystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordMigrationRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.migrate-plaintext-passwords:false}")
    private boolean migrate;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!migrate) {
            log.info("Password migration disabled (app.migrate-plaintext-passwords=false)");
            return;
        }

        log.info("Password migration enabled: scanning users to hash plaintext passwords...");

        List<User> users = userRepository.findAll();
        int updated = 0;
        for (User user : users) {
            String pwd = user.getPassword();
            if (pwd == null || pwd.isBlank()) continue;

            if (!isBCryptHash(pwd)) {
                String hashed = passwordEncoder.encode(pwd);
                user.setPassword(hashed);
                userRepository.save(user);
                updated++;
                log.debug("Hashed password for user id={}", user.getId());
            }
        }

        log.info("Password migration finished. Users processed={}, updated={}", users.size(), updated);
        log.warn("Remember to set app.migrate-plaintext-passwords=false after verifying migration to avoid reprocessing.");
    }

    private boolean isBCryptHash(String pwd) {
        return pwd.startsWith("$2a$") || pwd.startsWith("$2b$") || pwd.startsWith("$2y$");
    }
}

