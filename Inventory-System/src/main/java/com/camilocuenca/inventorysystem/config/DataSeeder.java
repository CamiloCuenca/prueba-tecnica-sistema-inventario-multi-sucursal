package com.camilocuenca.inventorysystem.config;

import com.camilocuenca.inventorysystem.Enums.PurchaseStatus;
import com.camilocuenca.inventorysystem.Enums.Role;
import com.camilocuenca.inventorysystem.Enums.TransferStatus;
import com.camilocuenca.inventorysystem.model.*;
import com.camilocuenca.inventorysystem.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
                                      com.camilocuenca.inventorysystem.repository.ProductPriceRepository productPriceRepository,
                                      SaleRepository saleRepository,
                                      SaleDetailRepository saleDetailRepository,
                                      PurchaseRepository purchaseRepository,
                                      PurchaseDetailRepository purchaseDetailRepository,
                                      TransferRepository transferRepository,
                                      TransferDetailRepository transferDetailRepository,
                                      PasswordEncoder passwordEncoder,
                                      JdbcTemplate jdbcTemplate) {
        return args -> {
            // usar un advisory lock de Postgres para evitar que múltiples instancias compitan por el seed
            // lockId puede ser cualquier long, se recomienda elegirlo fijo para la aplicación
            final long LOCK_ID = 0x9A7B6C5D4E3F21L; // ejemplo de clave de lock

            Boolean gotLock = false;
            try {
                // Intentar adquirir el lock sin bloquear (no esperamos indefinidamente)
                try {
                    gotLock = jdbcTemplate.queryForObject("SELECT pg_try_advisory_lock(?)", Boolean.class, LOCK_ID);
                } catch (Exception ex) {
                    System.out.println("DataSeeder: no se pudo consultar pg_try_advisory_lock — asegurarse que la BD es PostgreSQL. Excepción: " + ex.getMessage());
                    // Si no es Postgres o hay problema, no continuar para evitar races
                    return;
                }

                if (gotLock == null || !gotLock) {
                    System.out.println("DataSeeder: otra instancia está ejecutando el seeder. Saltando seeding en esta instancia.");
                    return;
                }

                // Revalidar que no haya branches antes de comenzar (al haber adquirido el lock, la comprobación es segura)
                if (branchRepository.count() > 0) {
                    System.out.println("DataSeeder: branches ya existen, saltando seeding.");
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
                    // asociar como conjunto (producto puede tener múltiples providers)
                    java.util.Set<Provider> provSet = new java.util.HashSet<>();
                    provSet.add(prov);
                    p.setProviders(provSet);
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

                // 5.1) Crear ventas en el mes pasado y el antepasado para tener histórico
                List<User> users = userRepository.findAll();
                List<com.camilocuenca.inventorysystem.model.Sale> salesToSave = new ArrayList<>();
                List<com.camilocuenca.inventorysystem.model.SaleDetail> saleDetailsToSave = new ArrayList<>();
                List<InventoryTransaction> saleTransactions = new ArrayList<>();

                LocalDate now = LocalDate.now();
                LocalDate currentMonth = now; // variable mantenida por claridad; puede eliminarse si se desea
                LocalDate lastMonth = now.minusMonths(1);
                LocalDate prevMonth = now.minusMonths(2);

                for (Branch b : branches) {
                    // usuarios de esa sucursal para asignar ventas (si no hay, usar cualquier usuario)
                    List<User> branchUsers = new ArrayList<>();
                    for (User u : users) {
                        if (u.getBranch() != null && u.getBranch().getId() != null && u.getBranch().getId().equals(b.getId())) {
                            branchUsers.add(u);
                        }
                    }
                    if (branchUsers.isEmpty()) branchUsers.addAll(users);

                    // generar ventas para el mes actual, mes pasado y antepasado (para cubrir la ventana por defecto)
                    int salesCurrent = 10 + rnd.nextInt(11); // 10-20
                    int salesLast = 8 + rnd.nextInt(8);
                    int salesPrev = 5 + rnd.nextInt(8);

                    // helper para generar ventas en un mes (3 pasadas)
                    for (int pass = 0; pass < 3; pass++) {
                        int count = pass == 0 ? salesCurrent : (pass == 1 ? salesLast : salesPrev);
                        LocalDate targetMonth = pass == 0 ? currentMonth : (pass == 1 ? lastMonth : prevMonth);
                        for (int sIdx = 0; sIdx < count; sIdx++) {
                            com.camilocuenca.inventorysystem.model.Sale sale = new com.camilocuenca.inventorysystem.model.Sale();
                            sale.setBranch(b);
                            sale.setUser(branchUsers.get(rnd.nextInt(branchUsers.size())));

                            // fecha aleatoria dentro del mes objetivo
                            int day = 1 + rnd.nextInt(targetMonth.lengthOfMonth());
                            LocalDate dt = LocalDate.of(targetMonth.getYear(), targetMonth.getMonth(), Math.min(day, targetMonth.lengthOfMonth()));
                            Instant saleInstant = dt.atStartOfDay(ZoneId.systemDefault()).plusSeconds(rnd.nextInt(86400)).toInstant();
                            sale.setCreatedAt(saleInstant);

                            BigDecimal saleTotal = BigDecimal.ZERO;
                            int itemsCount = 1 + rnd.nextInt(4);
                            for (int it = 0; it < itemsCount; it++) {
                                // seleccionar producto aleatorio entre los que la sucursal tiene en inventario
                                List<Inventory> invsForBranch = new ArrayList<>();
                                for (Inventory inv : inventories) if (inv.getBranch().getId().equals(b.getId())) invsForBranch.add(inv);
                                if (invsForBranch.isEmpty()) break;
                                Inventory chosenInv = invsForBranch.get(rnd.nextInt(invsForBranch.size()));
                                Product prod = chosenInv.getProduct();

                                // cantidad vendida: entre 1 y 5, pero no más que el stock actual
                                int qtyInt = 1 + rnd.nextInt(5);
                                BigDecimal currentStock = chosenInv.getQuantity() != null ? chosenInv.getQuantity() : BigDecimal.ZERO;
                                BigDecimal soldQty = BigDecimal.valueOf(Math.min(qtyInt, Math.max(1, currentStock.intValue())));

                                BigDecimal price = chosenInv.getSalePrice() != null ? chosenInv.getSalePrice() : new BigDecimal("10.00");

                                com.camilocuenca.inventorysystem.model.SaleDetail sd = new com.camilocuenca.inventorysystem.model.SaleDetail();
                                sd.setSale(sale);
                                sd.setProduct(prod);
                                sd.setQuantity(soldQty);
                                sd.setPrice(price);

                                BigDecimal lineTotal = price.multiply(soldQty);
                                saleTotal = saleTotal.add(lineTotal);

                                saleDetailsToSave.add(sd);

                                // crear transaction asociada a la venta para trazabilidad
                                InventoryTransaction tx = new InventoryTransaction();
                                tx.setBranch(b);
                                tx.setProduct(prod);
                                tx.setUser(sale.getUser());
                                tx.setType("SALE");
                                tx.setQuantity(soldQty);
                                tx.setReason("Venta POS");
                                tx.setReferenceType("SALE");
                                // referenceId lo setearemos después de guardar sale (temporalmente null)
                                tx.setReferenceId(null);
                                tx.setCreatedAt(saleInstant);
                                saleTransactions.add(tx);

                                // ajustar inventario en memoria para evitar ventas mayores al stock en siguientes loops
                                BigDecimal existingQty = chosenInv.getQuantity() != null ? chosenInv.getQuantity() : BigDecimal.ZERO;
                                BigDecimal newQty = existingQty.subtract(soldQty);
                                if (newQty.compareTo(BigDecimal.ZERO) < 0) newQty = BigDecimal.ZERO;
                                chosenInv.setQuantity(newQty);
                            }

                            sale.setTotal(saleTotal.setScale(2, RoundingMode.HALF_UP));
                            salesToSave.add(sale);
                        }
                    }
                }

                // guardar ventas y detalles, y luego actualizar transactions con referenceId
                saleRepository.saveAll(salesToSave);

                // Ahora que las ventas tienen IDs, asignar sale a cada saleDetail y referenceId a los transactions
                int txIdx = 0;

                for (com.camilocuenca.inventorysystem.model.Sale sale : salesToSave) {
                    // encontrar detalles que referencian a esta venta (los añadimos en orden: los detalles fueron creados con sale object referenciado antes de persistir)
                    // dado que setSale(sale) usó la instancia, Hibernate no conoce la id hasta haber guardado la venta; aquí buscamos detalles cuya sale == sale (mismo objeto)
                    for (com.camilocuenca.inventorysystem.model.SaleDetail sd : saleDetailsToSave) {
                        if (sd.getSale() == sale) {
                            sd.setSale(sale); // ahora sale tiene id
                        }
                    }
                    // asignar referenceId en las transacciones correspondientes al mismo orden de creación
                    // asumimos que por cada detalle creado, en saleTransactions hay una transaction en el mismo orden
                    // Buscamos N transactions igual al número de detalles pertenecientes a esta venta
                    int detailsForThisSale = 0;
                    for (com.camilocuenca.inventorysystem.model.SaleDetail sd : saleDetailsToSave) if (sd.getSale() == sale) detailsForThisSale++;
                    int assigned = 0;
                    for (int k = txIdx; k < saleTransactions.size() && assigned < detailsForThisSale; k++) {
                        InventoryTransaction tx = saleTransactions.get(k);
                        tx.setReferenceId(sale.getId());
                        assigned++;
                    }
                    txIdx += assigned;
                }

                saleDetailRepository.saveAll(saleDetailsToSave);
                inventoryTransactionRepository.saveAll(saleTransactions);

                // persistir inventarios actualizados (descontados por las ventas)
                inventoryRepository.saveAll(inventories);

                // 5.2) Crear compras (recepciones) para reponer stock en algunas sucursales
                List<Purchase> purchasesToSave = new ArrayList<>();
                List<PurchaseDetail> purchaseDetailsToSave = new ArrayList<>();
                List<InventoryTransaction> purchaseTxs = new ArrayList<>();
                for (Branch b : branches) {
                    // crear entre 3 y 6 compras recientes
                    int pc = 3 + rnd.nextInt(4);
                    for (int i = 0; i < pc; i++) {
                        Purchase p = new Purchase();
                        p.setBranch(b);
                        p.setProvider(providers.get(rnd.nextInt(providers.size())));
                        p.setUser(users.get(rnd.nextInt(users.size())));
                        p.setStatus(PurchaseStatus.RECEIVED);
                        LocalDate dt = LocalDate.now().minusDays(1 + rnd.nextInt(30));
                        p.setCreatedAt(dt.atStartOfDay(ZoneId.systemDefault()).toInstant());
                        BigDecimal subtotal = BigDecimal.ZERO;

                        int lines = 2 + rnd.nextInt(4);
                        for (int L = 0; L < lines; L++) {
                            Product prod = productsSaved.get(rnd.nextInt(productsSaved.size()));
                            BigDecimal qty = new BigDecimal(5 + rnd.nextInt(50));
                            BigDecimal price = new BigDecimal(5 + rnd.nextInt(50)).setScale(2, RoundingMode.HALF_UP);
                            PurchaseDetail pd = new PurchaseDetail();
                            pd.setPurchase(p);
                            pd.setProduct(prod);
                            pd.setQuantity(qty);
                            pd.setPrice(price);
                            pd.setReceivedQuantity(qty);
                            subtotal = subtotal.add(price.multiply(qty));
                            purchaseDetailsToSave.add(pd);

                            // aumentar inventario en la sucursal
                            boolean found = false;
                            for (Inventory inv : inventories) {
                                if (inv.getBranch().getId().equals(b.getId()) && inv.getProduct().getId().equals(prod.getId())) {
                                    inv.setQuantity(inv.getQuantity().add(qty));
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                Inventory inv = new Inventory();
                                inv.setBranch(b);
                                inv.setProduct(prod);
                                inv.setQuantity(qty);
                                inv.setMinStock(new BigDecimal(2));
                                inv.setSalePrice(price.multiply(new BigDecimal("1.3")).setScale(2, RoundingMode.HALF_UP));
                                inv.setAverageCost(price);
                                inv.setUpdatedAt(Instant.now());
                                inventories.add(inv);
                            }

                            // crear inventory transaction para la recepción
                            InventoryTransaction ptx = new InventoryTransaction();
                            ptx.setBranch(b);
                            ptx.setProduct(prod);
                            ptx.setUser(p.getUser());
                            ptx.setType("PURCHASE_RECEIPT");
                            ptx.setQuantity(qty);
                            ptx.setReason("Recepción de compra");
                            ptx.setReferenceType("PURCHASE");
                            ptx.setReferenceId(null); // se seteará después de persistir purchase
                            ptx.setCreatedAt(p.getCreatedAt());
                            purchaseTxs.add(ptx);
                        }
                        p.setSubtotal(subtotal);
                        p.setTax(subtotal.multiply(new BigDecimal("0.19")).setScale(2, RoundingMode.HALF_UP));
                        p.setTotal(p.getSubtotal().add(p.getTax()));
                        purchasesToSave.add(p);
                    }
                }

                // persistir purchases y detalles
                if (!purchasesToSave.isEmpty()) {
                    purchaseRepository.saveAll(purchasesToSave);

                    // asignar purchase ids a details y purchaseTxs
                    int pTxIdx = 0;
                    for (Purchase p : purchasesToSave) {
                        for (PurchaseDetail pd : purchaseDetailsToSave) {
                            if (pd.getPurchase() == p) pd.setPurchase(p);
                        }
                        // contar detalles de esta compra
                        int detailsForPurchase = 0;
                        for (PurchaseDetail pd : purchaseDetailsToSave) if (pd.getPurchase() == p) detailsForPurchase++;
                        int assigned = 0;
                        for (int k = pTxIdx; k < purchaseTxs.size() && assigned < detailsForPurchase; k++) {
                            purchaseTxs.get(k).setReferenceId(p.getId());
                            assigned++;
                        }
                        pTxIdx += assigned;
                    }

                    purchaseDetailRepository.saveAll(purchaseDetailsToSave);
                    inventoryTransactionRepository.saveAll(purchaseTxs);
                }

                // 5.3) Crear transfers entre sucursales
                List<Transfer> transfersToSave = new ArrayList<>();
                List<TransferDetail> transferDetailsToSave = new ArrayList<>();
                List<InventoryTransaction> transferTxs = new ArrayList<>();
                TransferStatus[] statuses = new TransferStatus[]{TransferStatus.PREPARING, TransferStatus.IN_TRANSIT, TransferStatus.RECEIVED};
                for (int t = 0; t < 10; t++) {
                    Branch origin = branches.get(rnd.nextInt(branches.size()));
                    Branch dest = branches.get(rnd.nextInt(branches.size()));
                    if (origin.getId().equals(dest.getId())) continue;
                    Transfer tr = new Transfer();
                    tr.setOriginBranch(origin);
                    tr.setDestinationBranch(dest);
                    tr.setCreatedBy(users.get(rnd.nextInt(users.size())));
                    tr.setApprovedBy(users.get(rnd.nextInt(users.size())));
                    TransferStatus status = statuses[rnd.nextInt(statuses.length)];
                    tr.setStatus(status);
                    tr.setCreatedAt(Instant.now().minusSeconds(rnd.nextInt(86400*20)));

                    int lines = 1 + rnd.nextInt(4);
                    for (int L = 0; L < lines; L++) {
                        // escoger producto del origin
                        List<Inventory> invsForOrigin = new ArrayList<>();
                        for (Inventory inv : inventories) if (inv.getBranch().getId().equals(origin.getId())) invsForOrigin.add(inv);
                        if (invsForOrigin.isEmpty()) break;
                        Inventory chosen = invsForOrigin.get(rnd.nextInt(invsForOrigin.size()));
                        BigDecimal qty = new BigDecimal(1 + rnd.nextInt(10));
                        if (chosen.getQuantity().compareTo(qty) < 0) qty = chosen.getQuantity();
                        if (qty.compareTo(BigDecimal.ZERO) <= 0) continue;
                        TransferDetail td = new TransferDetail();
                        td.setTransfer(tr);
                        td.setProduct(chosen.getProduct());
                        td.setQuantity(qty);
                        transferDetailsToSave.add(td);

                        // si status es EN_TRANSIT o PREPARING, descontar del origin (reservado)
                        if (status == TransferStatus.IN_TRANSIT || status == TransferStatus.PREPARING) {
                            chosen.setQuantity(chosen.getQuantity().subtract(qty));
                            if (chosen.getQuantity().compareTo(BigDecimal.ZERO) < 0) chosen.setQuantity(BigDecimal.ZERO);
                        }
                        // si status es RECEIVED, aumentar inventario del destino
                        if (status == TransferStatus.RECEIVED) {
                            boolean found = false;
                            for (Inventory inv : inventories) {
                                if (inv.getBranch().getId().equals(dest.getId()) && inv.getProduct().getId().equals(chosen.getProduct().getId())) {
                                    inv.setQuantity(inv.getQuantity().add(qty));
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                Inventory inv = new Inventory();
                                inv.setBranch(dest);
                                inv.setProduct(chosen.getProduct());
                                inv.setQuantity(qty);
                                inv.setMinStock(new BigDecimal(2));
                                inv.setSalePrice(chosen.getSalePrice());
                                inv.setAverageCost(chosen.getAverageCost());
                                inv.setUpdatedAt(Instant.now());
                                inventories.add(inv);
                            }
                        }

                        // crear transactions para transfer: OUT from origin
                        InventoryTransaction txOut = new InventoryTransaction();
                        txOut.setBranch(origin);
                        txOut.setProduct(chosen.getProduct());
                        txOut.setUser(tr.getCreatedBy());
                        txOut.setType("TRANSFER_OUT");
                        txOut.setQuantity(qty);
                        txOut.setReason("Transferencia a " + dest.getName());
                        txOut.setReferenceType("TRANSFER");
                        txOut.setReferenceId(null);
                        txOut.setCreatedAt(tr.getCreatedAt());
                        transferTxs.add(txOut);

                        // si ya recibido, crear IN tx for destination
                        if (status == TransferStatus.RECEIVED) {
                            InventoryTransaction txIn = new InventoryTransaction();
                            txIn.setBranch(dest);
                            txIn.setProduct(chosen.getProduct());
                            txIn.setUser(tr.getApprovedBy());
                            txIn.setType("TRANSFER_IN");
                            txIn.setQuantity(qty);
                            txIn.setReason("Recepción desde " + origin.getName());
                            txIn.setReferenceType("TRANSFER");
                            txIn.setReferenceId(null);
                            txIn.setCreatedAt(tr.getCreatedAt());
                            transferTxs.add(txIn);
                        }
                    }
                    transfersToSave.add(tr);
                }

                // persistir transfers y detalles
                if (!transfersToSave.isEmpty()) {
                    transferRepository.saveAll(transfersToSave);

                    // asignar transfer ids a details y transferTxs
                    int trTxIdx = 0;
                    for (Transfer tr : transfersToSave) {
                        for (TransferDetail td : transferDetailsToSave) {
                            if (td.getTransfer() == tr) td.setTransfer(tr);
                        }
                        int detailsForTransfer = 0;
                        for (TransferDetail td : transferDetailsToSave) if (td.getTransfer() == tr) detailsForTransfer++;
                        int assigned = 0;
                        for (int k = trTxIdx; k < transferTxs.size() && assigned < detailsForTransfer; k++) {
                            transferTxs.get(k).setReferenceId(tr.getId());
                            assigned++;
                        }
                        trTxIdx += assigned;
                    }

                    transferDetailRepository.saveAll(transferDetailsToSave);
                    inventoryTransactionRepository.saveAll(transferTxs);
                }

                // persistir inventarios actualizados y movimientos extra
                inventoryRepository.saveAll(inventories);

                // 6) Crear algunos inventory transactions (movimientos) realistas adicionales
                List<InventoryTransaction> transactions = new ArrayList<>();
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

                // 7) Precios de referencia por producto (Minorista, Mayorista, Promoción)
                List<Product> savedProducts = productRepository.findAll();
                List<com.camilocuenca.inventorysystem.model.ProductPrice> pricesToSave = new ArrayList<>();
                for (Product prod : savedProducts) {
                    com.camilocuenca.inventorysystem.model.ProductPrice p1 = new com.camilocuenca.inventorysystem.model.ProductPrice();
                    p1.setProduct(prod);
                    p1.setLabel("Minorista");
                    p1.setPrice(new BigDecimal( (10 + Math.abs(prod.getName().hashCode()) % 90) ).setScale(2, RoundingMode.HALF_UP));
                    p1.setCurrency("COP");
                    p1.setCreatedAt(Instant.now());
                    pricesToSave.add(p1);

                    com.camilocuenca.inventorysystem.model.ProductPrice p2 = new com.camilocuenca.inventorysystem.model.ProductPrice();
                    p2.setProduct(prod);
                    p2.setLabel("Mayorista");
                    p2.setPrice(new BigDecimal( (8 + Math.abs(prod.getSku().hashCode()) % 70) ).setScale(2, RoundingMode.HALF_UP));
                    p2.setCurrency("COP");
                    p2.setCreatedAt(Instant.now());
                    pricesToSave.add(p2);

                    // posibilidad de precio promo en algunos productos
                    if (Math.abs(prod.getName().hashCode()) % 5 == 0) {
                        com.camilocuenca.inventorysystem.model.ProductPrice p3 = new com.camilocuenca.inventorysystem.model.ProductPrice();
                        p3.setProduct(prod);
                        p3.setLabel("Promoción");
                        p3.setPrice(p1.getPrice().multiply(new BigDecimal("0.85")).setScale(2, RoundingMode.HALF_UP));
                        p3.setCurrency("COP");
                        p3.setCreatedAt(Instant.now());
                        pricesToSave.add(p3);
                    }
                }
                productPriceRepository.saveAll(pricesToSave);

                System.out.println("DataSeeder: seeding complete.");
            } finally {
                // liberar el advisory lock
                try {
                    jdbcTemplate.update("SELECT pg_advisory_unlock(?)", LOCK_ID);
                } catch (Exception ex) {
                    System.out.println("DataSeeder: no se pudo liberar pg_advisory_unlock: " + ex.getMessage());
                }
            }
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

