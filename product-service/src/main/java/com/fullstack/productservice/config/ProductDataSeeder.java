package com.fullstack.productservice.config;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.fullstack.commonservice.bmad.nguoi3.command.product.CreateProductCommand;

/**
 * Demo catalog seed, idempotent per fixed productId.
 *
 * Product data only lives in Axon's event store (product-service's own read
 * model is H2 in-memory and rebuilds by replay). If the event store is ever
 * reset - e.g. docker-compose volume renamed/recreated - the catalog was
 * previously unrecoverable without re-running the original manual curl
 * commands. This seeder reproduces the same 9 products with the same fixed
 * UUIDs every startup, so inventory rows and any other data referencing
 * these productIds keep resolving correctly after a reset.
 *
 * Safe to run on every startup: CreateProductCommand targets a fixed
 * aggregate identifier per product, so re-sending it against an aggregate
 * that already has events fails with a conflict, which is caught and
 * skipped below rather than crashing startup.
 */
@Component
public class ProductDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductDataSeeder.class);

    private final CommandGateway commandGateway;

    public ProductDataSeeder(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    private static final List<CreateProductCommand> DEMO_PRODUCTS = List.of(
            new CreateProductCommand("bde7b1d3-1b3e-47ac-88dd-9574df739979", "G-Shock GA-2100", "Casio", "Entry",
                    "Shock resistant, water resistant, modern Casioak design.",
                    new BigDecimal("2750000"),
                    "https://images.unsplash.com/photo-1533139502658-0198f920d8e8?w=800&q=80"),
            new CreateProductCommand("3dd817bc-19a0-4904-bebf-be776258ba67", "Nautilus 5711", "Patek Philippe", "Luxury",
                    "Iconic luxury sports watch, limited production.",
                    new BigDecimal("4600000000"),
                    "https://images.unsplash.com/photo-1509048191080-d2984bad6ae5?w=800&q=80"),
            new CreateProductCommand("c9962b2e-c171-4da8-8e83-41b3e95c3850", "Presage Cocktail Time", "Seiko", "Mid-range",
                    "Enamel dial inspired by Japanese cocktail culture.",
                    new BigDecimal("13000000"),
                    "https://images.unsplash.com/photo-1434056886845-dac89ffe9b56?w=800&q=80"),
            new CreateProductCommand("8dfd4bb5-440f-4196-b97f-40ed15078121", "Submariner Date", "Rolex", "Luxury",
                    "Iconic dive watch, stainless steel, water resistant to 300m.",
                    new BigDecimal("310000000"),
                    "https://images.unsplash.com/photo-1523170335258-f5ed11844a49?w=800&q=80"),
            new CreateProductCommand("f4d1c974-2e87-435a-b600-4c883f72450c", "Speedmaster Professional", "Omega", "Luxury",
                    "The Moonwatch. Manual-wind chronograph movement.",
                    new BigDecimal("170000000"),
                    "https://images.unsplash.com/photo-1547996160-81dfa63595aa?w=800&q=80"),
            new CreateProductCommand("52b6f738-1518-4779-a7cd-4f91d5993b64", "Prospex Diver", "Seiko", "Mid-range",
                    "Durable dive watch with sporty design and great value.",
                    new BigDecimal("11000000"),
                    "https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=800&q=80"),
            new CreateProductCommand("c594f9c4-d138-40f2-a0be-8f6612b72eee", "Big Bang Unico", "Hublot", "Luxury",
                    "Bold sporty design, sapphire case, exposed movement.",
                    new BigDecimal("550000000"),
                    "https://images.unsplash.com/photo-1614164185128-e4ec99c436d7?w=800&q=80"),
            new CreateProductCommand("c293d597-c394-4d56-89cf-7ce85fbebb17", "Carrera Calibre 16", "Tag Heuer", "Mid-range",
                    "Sporty chronograph inspired by vintage motor racing.",
                    new BigDecimal("80000000"),
                    "https://images.unsplash.com/photo-1587836374828-4dbafa94cf0e?w=800&q=80"),
            new CreateProductCommand("c03ba0bf-43ad-4dc5-a5c1-64a54638093e", "Edifice Chronograph", "Casio", "Entry",
                    "Sporty chronograph, steel band, affordable price.",
                    new BigDecimal("3500000"),
                    "https://images.unsplash.com/photo-1495856458515-0637185db551?w=800&q=80"));

    @Override
    public void run(ApplicationArguments args) {
        int created = 0;
        int skipped = 0;

        for (CreateProductCommand command : DEMO_PRODUCTS) {
            try {
                commandGateway.send(command).get();
                created++;
            } catch (ExecutionException e) {
                // Aggregate already has events for this productId - already seeded.
                skipped++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Product seeding interrupted", e);
                return;
            }
        }

        log.info("Product demo seed: {} created, {} already existed", created, skipped);
    }
}
