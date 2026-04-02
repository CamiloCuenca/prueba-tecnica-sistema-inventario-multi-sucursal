package com.camilocuenca.inventorysystem.config;

import com.camilocuenca.inventorysystem.Enums.Role;
import com.camilocuenca.inventorysystem.model.*;
import com.camilocuenca.inventorysystem.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedData(BranchRepository branchRepository,
                                     ProviderRepository providerRepository,
                                     ProductRepository productRepository,
                                     InventoryRepository inventoryRepository,
                                     UserRepository userRepository,
                                     InventoryTransactionRepository inventoryTransactionRepository,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            if (branchRepository.count() > 0) {
                System.out.println("DataSeeder: branches already exist, skipping seeding.");
                return;
            }

            // 1) Branches
            Branch armenia = new Branch();
            armenia.setName("Armenia");
            armenia.setAddress("Cra 14 #9-20, Armenia, Quindío");
            armenia.setLatitude(4.5342);
            armenia.setLongitude(-75.6812);
            armenia.setCreatedAt(Instant.now());

            Branch pereira = new Branch();
            pereira.setName("Pereira");
            pereira.setAddress("Av. 30 de Agosto #25-10, Pereira");
            pereira.setLatitude(4.8133);
            pereira.setLongitude(-75.6946);
            pereira.setCreatedAt(Instant.now());

            Branch manizales = new Branch();
            manizales.setName("Manizales");
            manizales.setAddress("Cl. 65 #23-12, Manizales");
            manizales.setLatitude(5.0689);
            manizales.setLongitude(-75.5172);
            manizales.setCreatedAt(Instant.now());

            Branch cali = new Branch();
            cali.setName("Cali");
            cali.setAddress("Av. 6N #23-45, Cali");
            cali.setLatitude(3.4516);
            cali.setLongitude(-76.5320);
            cali.setCreatedAt(Instant.now());

            branchRepository.saveAll(Arrays.asList(armenia, pereira, manizales, cali));

            // 2) Providers
            List<Provider> providers = new ArrayList<>();
            providers.add(createProvider("Distribuciones Andina S.A.", "contacto@andina.com"));
            providers.add(createProvider("Alimentos del Valle Ltda.", "ventas@alimentosvalle.co"));
            providers.add(createProvider("Quimicos y Productos SAS", "info@quimicosyproductos.co"));
            providers.add(createProvider("Electronica Central", "soporte@electronica-central.com"));
            providers.add(createProvider("Higiene y Limpieza Global", "contact@higieneglobal.co"));
            providerRepository.saveAll(providers);

            // 3) Products - generar ~50 productos distribuidos entre proveedores
            List<Product> allProducts = new ArrayList<>();
            String[] sampleNames = new String[]{
                    "Leche Entera 1L", "Arroz Largo Fino 1kg", "Azúcar Refinada 1kg", "Aceite vegetal 1L", "Harina de Trigo 1kg",
                    "Huevos (12)", "Cafe Molido 250g", "Pan Molde 500g", "Galletas Familia 200g", "Yogurt natural 125g",
                    "Jabón de baño 90g", "Detergente polvo 1kg", "Shampoo 400ml", "Pasta dental 90g", "Desodorante 150ml",
                    "Pilas AA (4)", "Bombillo LED 9W", "Cable USB-C 1m", "Cargador 5W", "Audífonos básicos",
                    "Cloro 1L", "Guantes de látex (50)", "Papel higiénico (4)", "Servilletas 100un", "Esponja multiuso",
                    "Arroz integral 1kg", "Quinoa 500g", "Aceite de oliva 500ml", "Miel 250g", "Avena 500g",
                    "Queso costeño 400g", "Pollo entero 1.5kg", "Carne molida 500g", "Salsa de tomate 400g", "Mayonesa 400g",
                    "Mermelada fresa 250g", "Té en bolsas 20un", "Chocolate 100g", "Leche deslactosada 1L", "Cereal 300g",
                    "Especias surtidas 50g", "Salsa de soya 250ml", "Atún en agua 170g", "Maíz para arepas 1kg", "Harina de maíz 1kg",
                    "Frijol rojo 1kg", "Lenteja 1kg", "Salsa BBQ 400g", "Crema chantilly 200g", "Gel antibacterial 250ml"
            };

            Random rnd = new Random(123);
            for (int i = 0; i < sampleNames.length; i++) {
                Product p = new Product();
                p.setName(sampleNames[i]);
                p.setSku("SKU-" + (1000 + i));
                p.setUnit(i % 5 == 0 ? "unit" : "kg");
                p.setCreatedAt(Instant.now());
                // asignar provider round-robin
                Provider prov = providers.get(i % providers.size());
                p.setProvider(prov);
                allProducts.add(p);
            }
            productRepository.saveAll(allProducts);

            // 4) Users: admin + managers + operators
            User admin = new User();
            admin.setName("Administrador Central");
            admin.setEmail("admin@empresa.local");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole(Role.ADMIN);
            admin.setCreatedAt(Instant.now());

            User managerArmenia = new User();
            managerArmenia.setName("Manager Armenia");
            managerArmenia.setEmail("manager.armenia@empresa.local");
            managerArmenia.setPassword(passwordEncoder.encode("123456"));
            managerArmenia.setRole(Role.MANAGER);
            managerArmenia.setBranch(armenia);
            managerArmenia.setCreatedAt(Instant.now());

            User managerPereira = new User();
            managerPereira.setName("Manager Pereira");
            managerPereira.setEmail("manager.pereira@empresa.local");
            managerPereira.setPassword(passwordEncoder.encode("123456"));
            managerPereira.setRole(Role.MANAGER);
            managerPereira.setBranch(pereira);
            managerPereira.setCreatedAt(Instant.now());

            User managerManizales = new User();
            managerManizales.setName("Manager Manizales");
            managerManizales.setEmail("manager.manizales@empresa.local");
            managerManizales.setPassword(passwordEncoder.encode("123456"));
            managerManizales.setRole(Role.MANAGER);
            managerManizales.setBranch(manizales);
            managerManizales.setCreatedAt(Instant.now());

            User managerCali = new User();
            managerCali.setName("Manager Cali");
            managerCali.setEmail("manager.cali@empresa.local");
            managerCali.setPassword(passwordEncoder.encode("123456"));
            managerCali.setRole(Role.MANAGER);
            managerCali.setBranch(cali);
            managerCali.setCreatedAt(Instant.now());

            User operatorArmenia = new User();
            operatorArmenia.setName("Operador Armenia");
            operatorArmenia.setEmail("op.armenia@empresa.local");
            operatorArmenia.setPassword(passwordEncoder.encode("123456"));
            operatorArmenia.setRole(Role.OPERATOR);
            operatorArmenia.setBranch(armenia);
            operatorArmenia.setCreatedAt(Instant.now());

            User operatorPereira = new User();
            operatorPereira.setName("Operador Pereira");
            operatorPereira.setEmail("op.pereira@empresa.local");
            operatorPereira.setPassword(passwordEncoder.encode("123456"));
            operatorPereira.setRole(Role.OPERATOR);
            operatorPereira.setBranch(pereira);
            operatorPereira.setCreatedAt(Instant.now());

            userRepository.saveAll(Arrays.asList(admin, managerArmenia, managerPereira, managerManizales, managerCali, operatorArmenia, operatorPereira));

            // 5) Inventories: asignar stocks por branch/product
            List<Inventory> inventories = new ArrayList<>();
            List<Product> productsSaved = productRepository.findAll();
            List<Branch> branches = branchRepository.findAll();

            for (Branch b : branches) {
                // para cada branch, añadir inventario para una selección de productos
                int limit = 20 + rnd.nextInt(10); // 20-29 productos por sucursal
                for (int i = 0; i < limit && i < productsSaved.size(); i++) {
                    Product p = productsSaved.get((i + Math.abs(b.getName().hashCode())) % productsSaved.size());
                    Inventory inv = new Inventory();
                    inv.setBranch(b);
                    inv.setProduct(p);
                    // cantidad aleatoria realista
                    BigDecimal qty = new BigDecimal(5 + rnd.nextInt(200));
                    inv.setQuantity(qty);
                    inv.setMinStock(new BigDecimal(2 + rnd.nextInt(10)));
                    inv.setSalePrice(new BigDecimal(5 + rnd.nextInt(100)).setScale(2, RoundingMode.HALF_UP));
                    inv.setAverageCost(new BigDecimal(2 + rnd.nextInt(50)).setScale(2, RoundingMode.HALF_UP));
                    inv.setUpdatedAt(Instant.now());
                    inventories.add(inv);
                }
            }
            inventoryRepository.saveAll(inventories);

            // 6) Crear algunos inventory transactions (movimientos) realistas
            List<InventoryTransaction> transactions = new ArrayList<>();
            List<User> users = userRepository.findAll();
            for (int i = 0; i < 30; i++) {
                Inventory inv = inventories.get(rnd.nextInt(inventories.size()));
                InventoryTransaction tx = new InventoryTransaction();
                tx.setBranch(inv.getBranch());
                tx.setProduct(inv.getProduct());
                tx.setUser(users.get(rnd.nextInt(users.size())));
                tx.setType(i % 2 == 0 ? "SALE" : "PURCHASE_RECEIPT");
                BigDecimal q = new BigDecimal(1 + rnd.nextInt(5));
                tx.setQuantity(q);
                tx.setReason(tx.getType().equals("SALE") ? "Venta POS" : "Recepción de compra");
                tx.setReferenceType(tx.getType().equals("SALE") ? "SALE" : "PURCHASE");
                tx.setReferenceId(UUID.randomUUID());
                tx.setCreatedAt(Instant.now().minusSeconds(rnd.nextInt(86400)));
                transactions.add(tx);
            }
            inventoryTransactionRepository.saveAll(transactions);

            System.out.println("DataSeeder: seeding complete.");
        };
    }

    private Provider createProvider(String name, String contact) {
        Provider p = new Provider();
        p.setName(name);
        p.setContactInfo(contact);
        p.setCreatedAt(Instant.now());
        return p;
    }
}

