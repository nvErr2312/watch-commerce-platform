package com.fullstack.inventoryservice.config;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.fullstack.inventoryservice.command.model.InventoryItem;
import com.fullstack.inventoryservice.command.repository.InventoryItemRepository;

/**
 * Demo stock seed, idempotent per productId.
 *
 * The Flyway migrations under db/migration stopped running when the service
 * moved to H2 with hibernate ddl-auto=update (Flyway was dropped from the
 * pom), so a fresh database starts with an empty inventory_items table.
 * This seeder restores the same 9 rows the old V4 migration provided, keyed
 * by the fixed product UUIDs that product-service's ProductDataSeeder also
 * uses - keeping the catalog/stock join intact on any fresh environment.
 *
 * Existing rows are never overwritten, so quantities changed by real
 * reservations survive restarts.
 */
@Component
public class InventoryDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InventoryDataSeeder.class);

    private final InventoryItemRepository repository;

    public InventoryDataSeeder(InventoryItemRepository repository) {
        this.repository = repository;
    }

    private static final Map<UUID, Integer> DEMO_STOCK = Map.of(
            UUID.fromString("bde7b1d3-1b3e-47ac-88dd-9574df739979"), 40,  // Casio G-Shock GA-2100
            UUID.fromString("3dd817bc-19a0-4904-bebf-be776258ba67"), 1,   // Patek Philippe Nautilus 5711
            UUID.fromString("c9962b2e-c171-4da8-8e83-41b3e95c3850"), 15,  // Seiko Presage Cocktail Time
            UUID.fromString("8dfd4bb5-440f-4196-b97f-40ed15078121"), 5,   // Rolex Submariner Date
            UUID.fromString("f4d1c974-2e87-435a-b600-4c883f72450c"), 10,  // Omega Speedmaster Professional
            UUID.fromString("52b6f738-1518-4779-a7cd-4f91d5993b64"), 20,  // Seiko Prospex Diver
            UUID.fromString("c594f9c4-d138-40f2-a0be-8f6612b72eee"), 2,   // Hublot Big Bang Unico
            UUID.fromString("c293d597-c394-4d56-89cf-7ce85fbebb17"), 8,   // Tag Heuer Carrera Calibre 16
            UUID.fromString("c03ba0bf-43ad-4dc5-a5c1-64a54638093e"), 50); // Casio Edifice Chronograph

    @Override
    public void run(ApplicationArguments args) {
        int created = 0;

        for (Map.Entry<UUID, Integer> entry : DEMO_STOCK.entrySet()) {
            if (repository.existsById(entry.getKey())) {
                continue;
            }
            InventoryItem item = new InventoryItem();
            item.setProductId(entry.getKey());
            item.setAvailableQuantity(entry.getValue());
            repository.save(item);
            created++;
        }

        log.info("Inventory demo seed: {} created, {} already existed", created, DEMO_STOCK.size() - created);
    }
}
