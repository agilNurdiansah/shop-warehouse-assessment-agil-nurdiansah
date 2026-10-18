package com.geli.warehouse.config;

import com.geli.warehouse.model.entity.Inventory;
import com.geli.warehouse.model.entity.Item;
import com.geli.warehouse.model.entity.ItemVariant;
import com.geli.warehouse.model.entity.User;
import com.geli.warehouse.model.enums.ItemStatus;
import com.geli.warehouse.model.enums.Role;
import com.geli.warehouse.repository.ItemRepository;
import com.geli.warehouse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        seedUsers();
        seedCatalog();
        log.info("Sample database records initialized successfully.");
    }

    private void seedUsers() {
        User admin = User.builder()
                .username("admin")
                .email("admin@warehouse.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ROLE_ADMIN)
                .build();

        User staff = User.builder()
                .username("staff")
                .email("staff@warehouse.com")
                .password(passwordEncoder.encode("staff123"))
                .role(Role.ROLE_STAFF)
                .build();

        User tester = User.builder()
                .username("tester")
                .email("tester@warehouse.com")
                .password(passwordEncoder.encode("tester123"))
                .role(Role.ROLE_ADMIN)
                .build();

        User customer = User.builder()
                .username("customer")
                .email("customer@example.com")
                .password(passwordEncoder.encode("customer123"))
                .role(Role.ROLE_CUSTOMER)
                .build();

        userRepository.save(admin);
        userRepository.save(staff);
        userRepository.save(tester);
        userRepository.save(customer);
    }

    private void seedCatalog() {
        // Item 1: Classic T-Shirt
        Item tshirt = Item.builder()
                .sku("ITEM-TSHIRT")
                .name("Classic Cotton T-Shirt")
                .description("100% premium combed cotton breathable t-shirt")
                .basePrice(BigDecimal.valueOf(19.99))
                .status(ItemStatus.ACTIVE)
                .build();

        createVariant(tshirt, "TSHIRT-BLK-M", "Black - Medium", "M", "Black", BigDecimal.valueOf(19.99), 50, 10);
        createVariant(tshirt, "TSHIRT-BLK-L", "Black - Large", "L", "Black", BigDecimal.valueOf(19.99), 30, 10);
        createVariant(tshirt, "TSHIRT-WHT-M", "White - Medium", "M", "White", BigDecimal.valueOf(18.99), 3, 5); // Low stock
        createVariant(tshirt, "TSHIRT-WHT-XL", "White - XL", "XL", "White", BigDecimal.valueOf(21.99), 0, 5); // Out of stock

        itemRepository.save(tshirt);

        // Item 2: Running Shoes
        Item shoes = Item.builder()
                .sku("ITEM-SHOES")
                .name("Pro Performance Running Shoes")
                .description("Lightweight responsive cushioned running shoes")
                .basePrice(BigDecimal.valueOf(89.99))
                .status(ItemStatus.ACTIVE)
                .build();

        createVariant(shoes, "SHOES-GRY-42", "Grey - 42", "42", "Grey", BigDecimal.valueOf(89.99), 15, 5);
        createVariant(shoes, "SHOES-GRY-43", "Grey - 43", "43", "Grey", BigDecimal.valueOf(89.99), 0, 5); // Out of stock
        createVariant(shoes, "SHOES-BLU-42", "Blue - 42", "42", "Blue", BigDecimal.valueOf(94.99), 8, 5);

        itemRepository.save(shoes);

        // Item 3: Backpack
        Item backpack = Item.builder()
                .sku("ITEM-BACKPACK")
                .name("Urban Canvas Backpack")
                .description("Water-resistant everyday laptop backpack")
                .basePrice(BigDecimal.valueOf(45.00))
                .status(ItemStatus.ACTIVE)
                .build();

        createVariant(backpack, "BAG-NVY", "Navy Blue", "OneSize", "Navy", BigDecimal.valueOf(45.00), 20, 5);

        itemRepository.save(backpack);
    }

    private void createVariant(Item item, String sku, String name, String size, String color,
                               BigDecimal price, int stock, int threshold) {
        ItemVariant variant = ItemVariant.builder()
                .sku(sku)
                .variantName(name)
                .size(size)
                .color(color)
                .price(price)
                .build();

        Inventory inventory = Inventory.builder()
                .quantity(stock)
                .reservedQuantity(0)
                .reorderThreshold(threshold)
                .lastRestockedAt(stock > 0 ? Instant.now() : null)
                .build();

        variant.setInventory(inventory);
        item.addVariant(variant);
    }
}
