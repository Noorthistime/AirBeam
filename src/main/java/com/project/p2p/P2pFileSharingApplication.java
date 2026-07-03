package com.project.p2p;

import com.project.p2p.model.UserAccount;
import com.project.p2p.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class P2pFileSharingApplication {

    public static void main(String[] args) {
        SpringApplication.run(P2pFileSharingApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(UserAccountRepository userRepo) {
        return args -> {
            if (userRepo.findById("ID-1").isEmpty()) {
                UserAccount admin = new UserAccount();
                admin.setUserId("ID-1");
                admin.setDisplayName("Noor");
                admin.setPassword("Noor");
                admin.setAdmin(true);
                admin.setCreatedAt(LocalDateTime.now());
                userRepo.save(admin);
            }
        };
    }
}
