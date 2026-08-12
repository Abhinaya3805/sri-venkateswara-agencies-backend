package com.srivenkateswara.agencies.initializer;

import com.srivenkateswara.agencies.entity.*;
import com.srivenkateswara.agencies.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing Sri Venkateswara Agencies database seed data...");

        // 1. Initialize Roles
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(ERole.ROLE_USER).build()));

        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(ERole.ROLE_ADMIN).build()));

        // 2. Initialize Default Admin
        if (!userRepository.existsByEmail("admin@srivenkateswara.com")) {
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(userRole);
            adminRoles.add(adminRole);

            User admin = User.builder()
                    .fullName("Sri Venkateswara Admin")
                    .email("admin@srivenkateswara.com")
                    .mobileNumber("7013317565")
                    .password(passwordEncoder.encode("Admin@123"))
                    .enabled(true)
                    .roles(adminRoles)
                    .build();
            userRepository.save(admin);
            log.info("Default Admin created: admin@srivenkateswara.com / Admin@123");
        }

        // 3. Initialize Default Sample User
        if (!userRepository.existsByEmail("customer@srivenkateswara.com")) {
            Set<Role> userRoles = new HashSet<>();
            userRoles.add(userRole);

            User customer = User.builder()
                    .fullName("Wholesale Customer")
                    .email("customer@srivenkateswara.com")
                    .mobileNumber("9876543210")
                    .password(passwordEncoder.encode("User@123"))
                    .enabled(true)
                    .roles(userRoles)
                    .build();
            userRepository.save(customer);
            log.info("Default Customer created: customer@srivenkateswara.com / User@123");
        }

        // 4. Initialize Categories
        Category coolDrinks = createCategoryIfNotFound("Cool Drinks", "Refreshing carbonated beverages and soft drinks", "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=800&q=80");
        Category juices = createCategoryIfNotFound("Juices", "100% pure fruit juices and nectars", "https://images.unsplash.com/photo-1613478223719-2ab802602423?auto=format&fit=crop&w=800&q=80");
        Category water = createCategoryIfNotFound("Water", "Pure packaged drinking water & mineral water bottles", "https://images.unsplash.com/photo-1548839140-29a749e1bc4e?auto=format&fit=crop&w=800&q=80");
        Category energyDrinks = createCategoryIfNotFound("Energy Drinks", "High-energy endurance and stimulant beverages", "https://images.unsplash.com/photo-1622543925917-763c34d1a86e?auto=format&fit=crop&w=800&q=80");
        Category soda = createCategoryIfNotFound("Soda", "Sparkling club soda and flavored carbonated water", "https://images.unsplash.com/photo-1527960471264-932f39eb5846?auto=format&fit=crop&w=800&q=80");
        Category sportsDrinks = createCategoryIfNotFound("Sports Drinks", "Isotonic electrolyte hydration beverages", "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=800&q=80");

        // 5. Initialize Products
        if (productRepository.count() == 0) {
            // Cool Drinks
            createProduct("Coca-Cola Original", "Coca-Cola", "Classic refreshing dark cola soft drink with natural flavors", new BigDecimal("40.00"), "750 ml Bottle", 150, "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=800&q=80", true, coolDrinks);
            createProduct("Thums Up Charged", "Thums Up", "Strong spicy carbonated cola drink with thunderous taste", new BigDecimal("40.00"), "750 ml Bottle", 200, "https://images.unsplash.com/photo-1581006852262-e4307cf6283a?auto=format&fit=crop&w=800&q=80", true, coolDrinks);
            createProduct("Sprite Clear Lemon Lime", "Sprite", "Crisp, clean lemon-lime flavored soft drink", new BigDecimal("40.00"), "750 ml Bottle", 180, "https://images.unsplash.com/photo-1625772299848-391b6a87d7b3?auto=format&fit=crop&w=800&q=80", true, coolDrinks);
            createProduct("Fanta Orange Soda", "Fanta", "Vibrant, bubbly orange flavored soft drink", new BigDecimal("38.00"), "750 ml Bottle", 120, "https://images.unsplash.com/photo-1624517452488-04869289c4ca?auto=format&fit=crop&w=800&q=80", false, coolDrinks);
            createProduct("Mountain Dew Neon Citrus", "Mountain Dew", "High-energy citrus flavored carbonated beverage", new BigDecimal("42.00"), "750 ml Bottle", 100, "https://images.unsplash.com/photo-1527960471264-932f39eb5846?auto=format&fit=crop&w=800&q=80", true, coolDrinks);

            // Juices
            createProduct("Maaza Mango Drink", "Maaza", "Rich, authentic Alphonso mango pulp juice drink", new BigDecimal("50.00"), "1.2 L Bottle", 150, "https://images.unsplash.com/photo-1546173159-315724a31696?auto=format&fit=crop&w=800&q=80", true, juices);
            createProduct("Slice Mango Nectar", "Slice", "Thick and juicy mango nectar crafted from handpicked mangoes", new BigDecimal("48.00"), "1.2 L Bottle", 110, "https://images.unsplash.com/photo-1546173159-315724a31696?auto=format&fit=crop&w=800&q=80", false, juices);
            createProduct("Real Fruit Power Pomegranate Juice", "Real", "100% natural pomegranate juice loaded with anti-oxidants", new BigDecimal("120.00"), "1 L Tetra Pack", 80, "https://images.unsplash.com/photo-1613478223719-2ab802602423?auto=format&fit=crop&w=800&q=80", true, juices);
            createProduct("Tropicana 100% Orange Juice", "Tropicana", "Pure squeezed orange juice rich in Vitamin C with no added sugar", new BigDecimal("130.00"), "1 L Tetra Pack", 90, "https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?auto=format&fit=crop&w=800&q=80", true, juices);
            createProduct("Fresh Apple Juice", "Real", "Crisp, sweet 100% pure Himalayan apple juice", new BigDecimal("115.00"), "1 L Tetra Pack", 75, "https://images.unsplash.com/photo-1576673442511-7e39b6545c87?auto=format&fit=crop&w=800&q=80", false, juices);

            // Water
            createProduct("Bisleri Packaged Water", "Bisleri", "Purified packaged drinking water with essential minerals", new BigDecimal("20.00"), "1 L Bottle", 500, "https://images.unsplash.com/photo-1548839140-29a749e1bc4e?auto=format&fit=crop&w=800&q=80", true, water);
            createProduct("Kinley Water Bottle", "Kinley", "Clean mineralized drinking water using reverse osmosis technology", new BigDecimal("20.00"), "1 L Bottle", 450, "https://images.unsplash.com/photo-1548839140-29a749e1bc4e?auto=format&fit=crop&w=800&q=80", false, water);
            createProduct("Aquafina Mineral Water", "Aquafina", "Pure, multi-step purified drinking water", new BigDecimal("20.00"), "1 L Bottle", 400, "https://images.unsplash.com/photo-1548839140-29a749e1bc4e?auto=format&fit=crop&w=800&q=80", false, water);

            // Energy Drinks
            createProduct("Red Bull Energy Drink", "Red Bull", "Vitalizes body and mind with taurine and B-group vitamins", new BigDecimal("125.00"), "250 ml Can", 250, "https://images.unsplash.com/photo-1622543925917-763c34d1a86e?auto=format&fit=crop&w=800&q=80", true, energyDrinks);
            createProduct("Monster Energy Green", "Monster", "Unleash the beast with powerful ginseng & energy blend", new BigDecimal("110.00"), "350 ml Can", 180, "https://images.unsplash.com/photo-1622543925917-763c34d1a86e?auto=format&fit=crop&w=800&q=80", true, energyDrinks);

            // Soda & Sports Drinks
            createProduct("Kinley Club Soda", "Kinley", "Extra bubbly carbonated water for mixing and standalone refreshment", new BigDecimal("20.00"), "750 ml Bottle", 200, "https://images.unsplash.com/photo-1527960471264-932f39eb5846?auto=format&fit=crop&w=800&q=80", false, soda);
            createProduct("Gatorade Blue Bolt Sports Drink", "Gatorade", "Isotonic electrolyte hydration drink for active sports performance", new BigDecimal("55.00"), "500 ml Bottle", 120, "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=800&q=80", true, sportsDrinks);

            log.info("Successfully seeded 16 sample beverage products into Sri Venkateswara Agencies database!");
        }
    }

    private Category createCategoryIfNotFound(String name, String description, String imageUrl) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .description(description)
                        .imageUrl(imageUrl)
                        .active(true)
                        .build()));
    }

    private void createProduct(String name, String brand, String description, BigDecimal price, String packSize, int stock, String imageUrl, boolean featured, Category category) {
        Product product = Product.builder()
                .name(name)
                .brand(brand)
                .description(description)
                .price(price)
                .packSize(packSize)
                .stock(stock)
                .imageUrl(imageUrl)
                .active(true)
                .featured(featured)
                .category(category)
                .build();
        productRepository.save(product);
    }
}
