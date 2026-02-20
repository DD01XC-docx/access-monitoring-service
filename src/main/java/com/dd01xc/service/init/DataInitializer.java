package com.dd01xc.service.init;

import com.dd01xc.service.model.User;
import com.dd01xc.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    
    //User-Repo;
    @Autowired
    private UserRepository userRepository;
    
    //Secure-pass;
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("DI Started");
        

        //MOK-ADMIN logg
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            admin.setStatus("ACTIVE");
            
            
            userRepository.save(admin);
        }
    }
}