package com.fullstack.productservice.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.stereotype.Component;

import com.fullstack.commonservice.bmad.nguoi3.command.product.CreateProductCommand;
import com.fullstack.productservice.query.entity.ProductView;
import com.fullstack.productservice.query.repository.ProductViewRepository;

@Component
public class ProductDataSeeder implements ApplicationRunner {

    private final ProductViewRepository repository;
    private final CommandGateway commandGateway;
    private final EventStore eventStore;

    public ProductDataSeeder(ProductViewRepository repository, CommandGateway commandGateway, EventStore eventStore) {
        this.repository = repository;
        this.commandGateway = commandGateway;
        this.eventStore = eventStore;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<ProductView> products = new ArrayList<>(List.of(
                product("bde7b1d3-1b3e-47ac-88dd-9574df739979", "Casio G-Shock GA-2100", "Casio", "Sport", 9000, "https://unsplash.com/photos/GHrKMXCb1gs/download?force=true&w=900"),
                product("3dd817bc-19a0-4904-bebf-be776258ba67", "Patek Philippe Nautilus 5711", "Patek Philippe", "Luxury", 10000, "https://unsplash.com/photos/VqAz2J71C3Y/download?force=true&w=900"),
                product("c9962b2e-c171-4da8-8e83-41b3e95c3850", "Seiko Presage Cocktail Time", "Seiko", "Dress", 7000, "https://unsplash.com/photos/9cddn0X5Mtc/download?force=true&w=900"),
                product("8dfd4bb5-440f-4196-b97f-40ed15078121", "Rolex Submariner Date", "Rolex", "Diver", 9000, "https://unsplash.com/photos/UNd3IPfV_7s/download?force=true&w=900"),
                product("f4d1c974-2e87-435a-b600-4c883f72450c", "Omega Speedmaster Professional", "Omega", "Chronograph", 8000, "https://unsplash.com/photos/OOSXECr0sUo/download?force=true&w=900"),
                product("52b6f738-1518-4779-a7cd-4f91d5993b64", "Seiko Prospex Diver", "Seiko", "Diver", 6000, "https://unsplash.com/photos/TtK9yVJx5tA/download?force=true&w=900"),
                product("c594f9c4-d138-40f2-a0be-8f6612b72eee", "Hublot Big Bang Unico", "Hublot", "Luxury", 10000, "https://unsplash.com/photos/GHrKMXCb1gs/download?force=true&w=900"),
                product("c293d597-c394-4d56-89cf-7ce85fbebb17", "Tag Heuer Carrera Calibre 16", "Tag Heuer", "Chronograph", 7500, "https://unsplash.com/photos/TtK9yVJx5tA/download?force=true&w=900"),
                product("c03ba0bf-43ad-4dc5-a5c1-64a54638093e", "Casio Edifice Chronograph", "Casio", "Chronograph", 4500, "https://unsplash.com/photos/OOSXECr0sUo/download?force=true&w=900")
        ));

        List<ProductView> newProducts = products.stream()
                .filter(product -> !repository.existsById(product.getId()))
                .toList();
        repository.saveAll(newProducts);

        // Seeded rows used to exist only in the read model, so update/delete
        // commands could not find their Axon aggregates.
        for (ProductView product : products) {
            if (eventStore.readEvents(product.getId()).hasNext()) {
                continue;
            }
            try {
                commandGateway.sendAndWait(new CreateProductCommand(
                        product.getId(),
                        product.getName(),
                        product.getBrand(),
                        product.getCategory(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getImageUrl()));
            } catch (RuntimeException ignored) {
                // Aggregate already exists in the event store; keep startup idempotent.
            }
        }
    }

    private static ProductView product(String id, String name, String brand, String category, long price, String imageUrl) {
        Instant now = Instant.now();
        ProductView product = new ProductView();
        product.setId(id);
        product.setName(name);
        product.setBrand(brand);
        product.setCategory(category);
        product.setDescription(name);
        product.setPrice(BigDecimal.valueOf(price));
        product.setImageUrl(imageUrl);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        return product;
    }
}
