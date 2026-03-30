package com.securecart.backend.config;

import com.securecart.backend.entity.Category;
import com.securecart.backend.entity.Role;
import com.securecart.backend.entity.User;
import com.securecart.backend.enums.RoleName;
import com.securecart.backend.repository.CategoryRepository;
import com.securecart.backend.repository.RoleRepository;
import com.securecart.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_ADMIN).build()));
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_USER).build()));

        List<String> defaultCategories = List.of(
                "Electronics",
                "Mobiles",
                "Laptops",
                "Toys",
                "Clothing",
                "Home Appliances",
                "Books",
                "Furniture",
                "Sports",
                "Groceries"
        );

        defaultCategories.forEach(categoryName -> {
            if (!categoryRepository.existsByNameIgnoreCase(categoryName)) {
                categoryRepository.save(Category.builder().name(categoryName).enabled(true).build());
            }
        });

        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@securecart.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .enabled(true)
                    .roles(Set.of(adminRole, userRole))
                    .build();
            userRepository.save(admin);
        }
    }
}
