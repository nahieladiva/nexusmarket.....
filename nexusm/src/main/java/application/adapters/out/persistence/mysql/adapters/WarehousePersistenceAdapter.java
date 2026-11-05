package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.mappers.WarehouseEntityMapper;
import application.adapters.out.persistence.mysql.repositories.WarehouseJpaRepository;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepository;
import application.domain.valueobjects.WarehouseId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

/**
 * Output adapter that implements {@link WarehouseRepository} on MySQL.
 */
@Component
public class WarehousePersistenceAdapter implements WarehouseRepository {

    private final WarehouseJpaRepository jpaRepository;
    private final WarehouseEntityMapper mapper;

    public WarehousePersistenceAdapter(WarehouseJpaRepository jpaRepository,
                                       WarehouseEntityMapper mapper) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository is required");
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
    }

    @Override
    public Warehouse save(Warehouse warehouse) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(warehouse)));
    }

    @Override
    public Optional<Warehouse> findById(WarehouseId id) {
        return jpaRepository.findById(id.toString()).map(mapper::toDomain);
    }

    @Override
    public List<Warehouse> findByNameContaining(String keyword) {
        return jpaRepository.findByNameContainingIgnoreCase(keyword).stream()
            .map(mapper::toDomain).toList();
    }

    @Override
    public List<Warehouse> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}