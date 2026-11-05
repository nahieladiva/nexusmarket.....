package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.mappers.InventoryEntityMapper;
import application.adapters.out.persistence.mysql.repositories.InventoryJpaRepository;
import application.domain.models.Inventory;
import application.domain.ports.out.InventoryRepository;
import application.domain.valueobjects.InventoryId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.WarehouseId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

/**
 * Output adapter that implements {@link InventoryRepository} on MySQL.
 */
@Component
public class InventoryPersistenceAdapter implements InventoryRepository {

    private final InventoryJpaRepository jpaRepository;
    private final InventoryEntityMapper mapper;

    public InventoryPersistenceAdapter(InventoryJpaRepository jpaRepository,
                                       InventoryEntityMapper mapper) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository is required");
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
    }

    @Override
    public Inventory save(Inventory inventory) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(inventory)));
    }

    @Override
    public Optional<Inventory> findById(InventoryId id) {
        return jpaRepository.findById(id.toString()).map(mapper::toDomain);
    }

    @Override
    public Optional<Inventory> findByProductIdAndWarehouseId(ProductId productId,
                                                            WarehouseId warehouseId) {
        return jpaRepository
            .findByProductIdAndWarehouseId(productId.toString(), warehouseId.toString())
            .map(mapper::toDomain);
    }

    @Override
    public List<Inventory> findByWarehouseId(WarehouseId warehouseId) {
        return jpaRepository.findByWarehouseId(warehouseId.toString()).stream()
            .map(mapper::toDomain).toList();
    }

    @Override
    public List<Inventory> findByProductId(ProductId productId) {
        return jpaRepository.findByProductId(productId.toString()).stream()
            .map(mapper::toDomain).toList();
    }

    @Override
    public List<Inventory> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}